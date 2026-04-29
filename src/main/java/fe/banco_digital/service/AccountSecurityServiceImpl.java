package fe.banco_digital.service;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.event.AuditoriaEvent;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountSecurityServiceImpl implements AccountSecurityService {

    private final UsuarioRepository usuarioRepo;
    private final CuentaRepository cuentaRepo;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public AccountSecurityServiceImpl(UsuarioRepository usuarioRepo,
                                      CuentaRepository cuentaRepo,
                                      PasswordEncoder passwordEncoder,
                                      ApplicationEventPublisher eventPublisher) {
        this.usuarioRepo = usuarioRepo;
        this.cuentaRepo = cuentaRepo;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void bloquearCuenta(String username, String password) {
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new AutenticacionFallidaException();
        }

        Long clienteId = usuario.getCliente().getIdCliente();
        Cuenta cuenta = cuentaRepo
                .findFirstByClienteIdClienteAndEstado(clienteId, EstadoCuenta.ACTIVA)
                .orElseThrow(() -> new CuentaNoEncontradaException(clienteId));

        cuenta.setEstado(EstadoCuenta.BLOQUEADA);
        cuentaRepo.save(cuenta);

        eventPublisher.publishEvent(new AuditoriaEvent(this, "BLOQUEO_CUENTA",
                usuario.getIdUsuario(),
                "Cuenta " + cuenta.getNumeroCuenta() + " bloqueada via APP_MOVIL"));
    }

    @Override
    @Transactional
    public void desbloquearCuenta(String username, String password) {
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new AutenticacionFallidaException();
        }

        Long clienteId = usuario.getCliente().getIdCliente();
        Cuenta cuenta = cuentaRepo
                .findFirstByClienteIdClienteAndEstado(clienteId, EstadoCuenta.BLOQUEADA)
                .orElseThrow(() -> new CuentaNoEncontradaException(clienteId));

        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuentaRepo.save(cuenta);

        eventPublisher.publishEvent(new AuditoriaEvent(this, "DESBLOQUEO_CUENTA",
                usuario.getIdUsuario(),
                "Cuenta " + cuenta.getNumeroCuenta() + " desbloqueada via APP_MOVIL"));
    }
}
