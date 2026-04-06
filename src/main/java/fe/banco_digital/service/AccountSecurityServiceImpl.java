package fe.banco_digital.service;

import fe.banco_digital.entity.Auditoria;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.repository.AuditoriaRepository;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AccountSecurityServiceImpl implements AccountSecurityService {

    private final UsuarioRepository usuarioRepo;
    private final CuentaRepository cuentaRepo;
    private final AuditoriaRepository auditoriaRepo;

    public AccountSecurityServiceImpl(UsuarioRepository usuarioRepo,
                                      CuentaRepository cuentaRepo,
                                      AuditoriaRepository auditoriaRepo) {
        this.usuarioRepo = usuarioRepo;
        this.cuentaRepo = cuentaRepo;
        this.auditoriaRepo = auditoriaRepo;
    }

    @Override
    @Transactional
    public void bloquearCuenta(Long idUsuario, String password) {
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!"1234".equals(password)) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        Long clienteId = usuario.getCliente().getIdCliente();
        Cuenta cuenta = cuentaRepo
                .findFirstByClienteIdClienteAndEstado(clienteId, EstadoCuenta.ACTIVA)
                .orElseThrow(() -> new RuntimeException("Cuenta activa no encontrada"));

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
    public void desbloquearCuenta(Long idUsuario, String password) {
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!"1234".equals(password)) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        Long clienteId = usuario.getCliente().getIdCliente();
        Cuenta cuenta = cuentaRepo
                .findFirstByClienteIdClienteAndEstado(clienteId, EstadoCuenta.BLOQUEADA)
                .orElseThrow(() -> new RuntimeException("No existe una cuenta bloqueada"));

        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuentaRepo.save(cuenta);

        Auditoria auditoria = new Auditoria();
        auditoria.setAccion("DESBLOQUEO_CUENTA");
        auditoria.setUsuario(usuario);
        auditoria.setDetalle("Cuenta " + cuenta.getNumeroCuenta() + " desbloqueada via APP_MOVIL");
        auditoriaRepo.save(auditoria);
    }
}
