package fe.banco_digital.service;

import fe.banco_digital.dto.GenerarTokenRetiroSolicitudDTO;
import fe.banco_digital.dto.TokenRetiroEstadoDTO;
import fe.banco_digital.dto.TokenRetiroRespuestaDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.MovimientoRepository;
import fe.banco_digital.repository.TokenRetiroRepository;
import fe.banco_digital.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class TokenRetiroServiceImpl implements TokenRetiroService {

    private final CuentaRepository cuentaRepository;
    private final TokenRetiroRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoRepository movimientoRepository;

    public TokenRetiroServiceImpl(CuentaRepository cuentaRepository,
                                   TokenRetiroRepository tokenRepository,
                                   UsuarioRepository usuarioRepository,
                                   MovimientoRepository movimientoRepository) {
        this.cuentaRepository    = cuentaRepository;
        this.tokenRepository     = tokenRepository;
        this.usuarioRepository   = usuarioRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    @Transactional
    public TokenRetiroRespuestaDTO generarToken(GenerarTokenRetiroSolicitudDTO solicitud, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        Long idCliente = usuario.getCliente().getIdCliente();
        Cuenta cuenta = cuentaRepository
                .findByIdCuentaAndCliente_IdCliente(solicitud.getIdCuenta(), idCliente)
                .orElseThrow(() -> new AccesoNoAutorizadoException(
                        "La cuenta no pertenece al cliente autenticado"));

        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new OperacionNoPermitidaException("La cuenta no está activa");
        }
        if (solicitud.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OperacionNoPermitidaException("El monto debe ser mayor a cero");
        }

        // Expirar tokens anteriores activos de esta cuenta y devolver su saldo
        tokenRepository.findByCuenta_IdCuentaAndEstado(cuenta.getIdCuenta(), EstadoToken.ACTIVO)
                .forEach(tokenPrevio -> {
                    cuenta.setSaldo(cuenta.getSaldo().add(tokenPrevio.getMonto()));
                    tokenPrevio.setEstado(EstadoToken.EXPIRADO);
                    tokenRepository.save(tokenPrevio);
                });

        if (cuenta.getSaldo().compareTo(solicitud.getMonto()) < 0) {
            throw new OperacionNoPermitidaException("Saldo insuficiente para el retiro solicitado");
        }

        // Descontar el saldo al generar el token (se devuelve si expira)
        cuenta.setSaldo(cuenta.getSaldo().subtract(solicitud.getMonto()));
        cuentaRepository.save(cuenta);

        String codigo = generarCodigo6Digitos();

        TokenRetiro token = new TokenRetiro();
        token.setCodigo(codigo);
        token.setMonto(solicitud.getMonto());
        token.setEstado(EstadoToken.ACTIVO);
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));
        token.setCuenta(cuenta);

        TokenRetiro guardado = tokenRepository.save(token);
        long segundos = Duration.between(LocalDateTime.now(), guardado.getFechaExpiracion()).getSeconds();
        return new TokenRetiroRespuestaDTO(guardado.getCodigo(), guardado.getMonto(), segundos);
    }

    @Override
    @Transactional
    public void usarToken(String codigo, BigDecimal monto) {
        TokenRetiro token = tokenRepository.findByCodigo(codigo)
                .orElseThrow(() -> new OperacionNoPermitidaException("Código de retiro no existe o ya fue utilizado"));

        if (token.getEstado() != EstadoToken.ACTIVO) {
            throw new OperacionNoPermitidaException("El token ya fue utilizado o expiró");
        }
        if (token.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new OperacionNoPermitidaException("El token ha expirado");
        }
        if (token.getMonto().compareTo(monto) != 0) {
            throw new OperacionNoPermitidaException("El monto enviado no coincide con el monto del token");
        }

        token.setEstado(EstadoToken.USADO);
        tokenRepository.save(token);

        // Registrar movimiento de retiro en el historial
        Movimiento mov = new Movimiento();
        mov.setCuenta(token.getCuenta());
        mov.setTipo(TipoMovimiento.RETIRO);
        mov.setMonto(token.getMonto());
        mov.setEstado(EstadoMovimiento.EXITOSO);
        mov.setFecha(LocalDateTime.now());
        movimientoRepository.save(mov);
    }

    @Override
    @Transactional
    public void expirarTokens() {
        tokenRepository.findAll().forEach(token -> {
            if (token.getEstado() == EstadoToken.ACTIVO &&
                token.getFechaExpiracion().isBefore(LocalDateTime.now())) {

                Cuenta cuenta = token.getCuenta();
                // devolver el saldo descontado al generar el token
                cuenta.setSaldo(cuenta.getSaldo().add(token.getMonto()));
                token.setEstado(EstadoToken.EXPIRADO);

                cuentaRepository.save(cuenta);
                tokenRepository.save(token);
            }
        });
    }

    @Override
    public TokenRetiroEstadoDTO consultarEstado(String codigo) {
        TokenRetiro token = tokenRepository.findByCodigo(codigo)
                .orElseThrow(() -> new OperacionNoPermitidaException("Token no existe"));
        long segundos = token.getFechaExpiracion().isAfter(LocalDateTime.now())
                ? Duration.between(LocalDateTime.now(), token.getFechaExpiracion()).getSeconds()
                : 0;
        return new TokenRetiroEstadoDTO(token.getEstado().name(), segundos);
    }

    private String generarCodigo6Digitos() {
        Random random = new Random();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }
}
