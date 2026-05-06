package fe.banco_digital.service;

import fe.banco_digital.dto.GenerarTokenRetiroSolicitudDTO;
import fe.banco_digital.dto.TokenRetiroEstadoDTO;
import fe.banco_digital.dto.TokenRetiroRespuestaDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.RetiroRechazadoException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.TokenRetiroRepository;
import fe.banco_digital.repository.TransaccionRepository;
import fe.banco_digital.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.security.SecureRandom;

@Service
public class TokenRetiroServiceImpl implements TokenRetiroService {

    private static final SecureRandom secureRandom = new SecureRandom();

    private final CuentaRepository cuentaRepository;
    private final TokenRetiroRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransaccionRepository transaccionRepository;

    public TokenRetiroServiceImpl(CuentaRepository cuentaRepository,
                                   TokenRetiroRepository tokenRepository,
                                   UsuarioRepository usuarioRepository,
                                   TransaccionRepository transaccionRepository) {
        this.cuentaRepository      = cuentaRepository;
        this.tokenRepository       = tokenRepository;
        this.usuarioRepository     = usuarioRepository;
        this.transaccionRepository = transaccionRepository;
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
            throw new RetiroRechazadoException("La cuenta no está activa");
        }
        if (solicitud.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RetiroRechazadoException("El monto debe ser mayor a cero");
        }
        if (cuenta.getSaldo().compareTo(solicitud.getMonto()) < 0) {
            throw new RetiroRechazadoException("Saldo insuficiente para el retiro solicitado");
        }

        String codigo = generarCodigo6Digitos();

        // saldo = disponible actual; se descuenta al reservar
        cuenta.setSaldo(cuenta.getSaldo().subtract(solicitud.getMonto()));
        cuenta.setSaldoDisponible(cuenta.getSaldo());
        cuenta.setSaldoReservado(cuenta.getSaldoReservado().add(solicitud.getMonto()));
        cuentaRepository.save(cuenta);

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
                .orElseThrow(() -> new RetiroRechazadoException("Código de retiro no existe o ya fue utilizado"));

        if (token.getEstado() != EstadoToken.ACTIVO) {
            throw new RetiroRechazadoException("El token ya fue utilizado o expiró");
        }
        if (token.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new RetiroRechazadoException("El token ha expirado");
        }
        if (token.getMonto().compareTo(monto) != 0) {
            throw new RetiroRechazadoException("El monto enviado no coincide con el monto del token");
        }

        Cuenta cuenta = token.getCuenta();
        // saldo ya fue descontado en generarToken; solo liberar la reserva
        cuenta.setSaldoReservado(cuenta.getSaldoReservado().subtract(token.getMonto()));
        token.setEstado(EstadoToken.USADO);

        cuentaRepository.save(cuenta);
        tokenRepository.save(token);

        // registrar la transacción para que aparezca en el historial
        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaOrigen(cuenta);
        transaccion.setCuentaDestino(null);
        transaccion.setTipo(TipoTransaccion.RETIRO_SIN_TARJETA);
        transaccion.setMonto(token.getMonto());
        transaccion.setEstado(EstadoTransaccion.EXITOSA);
        transaccion.setFecha(LocalDateTime.now());
        transaccionRepository.save(transaccion);
    }

    @Override
    @Transactional
    public void expirarTokens() {
        tokenRepository.findAll().forEach(token -> {
            if (token.getEstado() == EstadoToken.ACTIVO &&
                token.getFechaExpiracion().isBefore(LocalDateTime.now())) {

                Cuenta cuenta = token.getCuenta();
                // devolver el saldo que se había reservado
                cuenta.setSaldo(cuenta.getSaldo().add(token.getMonto()));
                cuenta.setSaldoDisponible(cuenta.getSaldo());
                cuenta.setSaldoReservado(cuenta.getSaldoReservado().subtract(token.getMonto()));
                token.setEstado(EstadoToken.EXPIRADO);

                cuentaRepository.save(cuenta);
                tokenRepository.save(token);
            }
        });
    }

    @Override
    public TokenRetiroEstadoDTO consultarEstado(String codigo) {
        TokenRetiro token = tokenRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RetiroRechazadoException("Token no existe"));
        long segundos = token.getFechaExpiracion().isAfter(LocalDateTime.now())
                ? Duration.between(LocalDateTime.now(), token.getFechaExpiracion()).getSeconds()
                : 0;
        return new TokenRetiroEstadoDTO(token.getEstado().name(), segundos);
    }

    private String generarCodigo6Digitos() {
        int numero = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(numero);
    }
}
