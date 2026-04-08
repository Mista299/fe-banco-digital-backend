package fe.banco_digital.repository;

import fe.banco_digital.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock ClienteRepository clienteRepo;
    @Mock UsuarioRepository usuarioRepo;
    @Mock RolRepository rolRepo;
    @Mock CuentaRepository cuentaRepo;
    @Mock TransaccionRepository transaccionRepo;
    @Mock AuditoriaRepository auditoriaRepo;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks
    DataLoader dataLoader;

    @Test
    void init_createsMissingSeedData() throws Exception {
        when(rolRepo.findByNombre(any())).thenReturn(Optional.empty());
        when(clienteRepo.findByDocumento(any())).thenReturn(Optional.empty());
        when(usuarioRepo.findByUsername(any())).thenReturn(Optional.empty());
        when(cuentaRepo.findByNumeroCuenta(any())).thenReturn(Optional.empty());
        when(transaccionRepo.count()).thenReturn(0L);
        when(auditoriaRepo.count()).thenReturn(0L);

        when(rolRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(clienteRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(usuarioRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(cuentaRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transaccionRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(auditoriaRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        when(passwordEncoder.encode(any())).thenReturn("hashed-password");

        var runner = dataLoader.init(clienteRepo, usuarioRepo, rolRepo, cuentaRepo, transaccionRepo, auditoriaRepo, passwordEncoder);
        runner.run();

        verify(rolRepo, atLeastOnce()).save(any(Rol.class));
        verify(clienteRepo, atLeastOnce()).save(any(Cliente.class));
        verify(usuarioRepo, atLeastOnce()).save(any(Usuario.class));
        verify(cuentaRepo, atLeastOnce()).save(any(Cuenta.class));
        verify(transaccionRepo, atLeastOnce()).save(any(Transaccion.class));
        verify(auditoriaRepo, atLeastOnce()).save(any(Auditoria.class));
    }
}