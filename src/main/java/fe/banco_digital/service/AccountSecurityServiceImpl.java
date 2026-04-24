package fe.banco_digital.service;

import fe.banco_digital.entity.Auditoria;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.repository.AuditoriaRepository;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountSecurityServiceImpl implements AccountSecurityService {

    private final UsuarioRepository usuarioRepo;
    private final CuentaRepository cuentaRepo;
    private final AuditoriaRepository auditoriaRepo;
    private final PasswordEncoder passwordEncoder;

    public AccountSecurityServiceImpl(UsuarioRepository usuarioRepo,
                                      CuentaRepository cuentaRepo,
                                      AuditoriaRepository auditoriaRepo,
                                      PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.cuentaRepo = cuentaRepo;
        this.auditoriaRepo = auditoriaRepo;
        this.passwordEncoder = passwordEncoder;
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

        Auditoria auditoria = new Auditoria();
        auditoria.setAccion("BLOQUEO_CUENTA");
        auditoria.setUsuario(usuario);
        auditoria.setDetalle("Cuenta " + cuenta.getNumeroCuenta() + " bloqueada via APP_MOVIL");
        auditoriaRepo.save(auditoria);
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

        Auditoria auditoria = new Auditoria();
        auditoria.setAccion("DESBLOQUEO_CUENTA");
        auditoria.setUsuario(usuario);
        auditoria.setDetalle("Cuenta " + cuenta.getNumeroCuenta() + " desbloqueada via APP_MOVIL");
        auditoriaRepo.save(auditoria);
    }
}
