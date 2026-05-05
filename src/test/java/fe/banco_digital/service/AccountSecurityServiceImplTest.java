package fe.banco_digital.service;

import fe.banco_digital.entity.Auditoria;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.repository.AuditoriaRepository;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountSecurityServiceImplTest {

    @Mock
    UsuarioRepository usuarioRepo;

    @Mock
    CuentaRepository cuentaRepo;

    @Mock
    AuditoriaRepository auditoriaRepo;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    AccountSecurityServiceImpl service;

    Usuario usuario;
    Cliente cliente;
    Cuenta cuenta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(7L);

        usuario = new Usuario();
        usuario.setIdUsuario(9L);
        usuario.setUsername("testuser");
        usuario.setCliente(cliente);

        cuenta = new Cuenta();
        cuenta.setIdCuenta(21L);
        cuenta.setNumeroCuenta("ACC-21");
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setCliente(cliente);
    }

    @Test
    void bloquearCuenta_success_savesCuentaAndAuditoria() {
        when(usuarioRepo.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(cuentaRepo.findFirstByClienteIdClienteAndEstado(7L, EstadoCuenta.ACTIVA)).thenReturn(Optional.of(cuenta));

        service.bloquearCuenta("testuser", "1234");

        verify(cuentaRepo).save(cuenta);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void bloquearCuenta_wrongPassword_throws() {
        when(usuarioRepo.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.bloquearCuenta("testuser", "bad"));
    }

    @Test
    void bloquearCuenta_missingUsuario_throws() {
        when(usuarioRepo.findByUsername("testuser")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.bloquearCuenta("testuser", "1234"));
    }

    @Test
    void desbloquearCuenta_success_savesCuentaAndAuditoria() {
        cuenta.setEstado(EstadoCuenta.BLOQUEADA);
        when(usuarioRepo.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(cuentaRepo.findFirstByClienteIdClienteAndEstado(7L, EstadoCuenta.BLOQUEADA)).thenReturn(Optional.of(cuenta));

        service.desbloquearCuenta("testuser", "1234");

        verify(cuentaRepo).save(cuenta);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void desbloquearCuenta_noBlockedAccount_throws() {
        when(usuarioRepo.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(cuentaRepo.findFirstByClienteIdClienteAndEstado(7L, EstadoCuenta.BLOQUEADA)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.desbloquearCuenta("testuser", "1234"));
    }
}
