package fe.banco_digital.service;

import fe.banco_digital.dto.DepositoPendienteRespuestaDTO;
import fe.banco_digital.dto.RegistrarDepositoPendienteDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.DepositoPendiente;
import fe.banco_digital.entity.EstadoDepositoPendiente;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.DepositoPendienteRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class DepositoPendienteServiceImpl implements DepositoPendienteService {

    private static final long MINUTOS_EXPIRACION = 15L;

    private final DepositoPendienteRepository depositoPendienteRepository;
    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;

    public DepositoPendienteServiceImpl(DepositoPendienteRepository depositoPendienteRepository,
                                         CuentaRepository cuentaRepository,
                                         UsuarioRepository usuarioRepository) {
        this.depositoPendienteRepository = depositoPendienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public DepositoPendienteRespuestaDTO registrar(RegistrarDepositoPendienteDTO solicitud, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AccesoNoAutorizadoException::new);

        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(solicitud.getNumeroCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada."));

        if (!cuenta.getCliente().getIdCliente().equals(usuario.getCliente().getIdCliente())) {
            throw new AccesoNoAutorizadoException("La cuenta no pertenece al usuario autenticado.");
        }

        DepositoPendiente pendiente = new DepositoPendiente();
        pendiente.setReferenciaGateway(solicitud.getReferenciaGateway());
        pendiente.setNumeroCuenta(solicitud.getNumeroCuenta());
        pendiente.setMonto(solicitud.getMonto());
        pendiente.setEstado(EstadoDepositoPendiente.PENDIENTE);
        pendiente.setFechaCreacion(LocalDateTime.now());
        pendiente.setFechaExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));

        depositoPendienteRepository.save(pendiente);
        return toDTO(pendiente);
    }

    @Override
    @Transactional
    public DepositoPendienteRespuestaDTO consultar(String referenciaGateway, String username) {
        DepositoPendiente pendiente = depositoPendienteRepository
                .findByReferenciaGateway(referenciaGateway)
                .orElseThrow(() -> new CuentaNoEncontradaException("Referencia no encontrada."));

        if (pendiente.getEstado() == EstadoDepositoPendiente.PENDIENTE
                && pendiente.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            pendiente.setEstado(EstadoDepositoPendiente.EXPIRADO);
            depositoPendienteRepository.save(pendiente);
        }

        return toDTO(pendiente);
    }

    private DepositoPendienteRespuestaDTO toDTO(DepositoPendiente pendiente) {
        long segundos = ChronoUnit.SECONDS.between(LocalDateTime.now(), pendiente.getFechaExpiracion());
        return new DepositoPendienteRespuestaDTO(
                pendiente.getReferenciaGateway(),
                pendiente.getFechaExpiracion(),
                Math.max(segundos, 0),
                pendiente.getEstado().name()
        );
    }
}
