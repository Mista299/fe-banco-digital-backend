package fe.banco_digital.security;

import fe.banco_digital.entity.Rol;
import fe.banco_digital.entity.RolNombre;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.entity.EstadoUsuario;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetallesServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @InjectMocks
    UsuarioDetallesService service;

    @Test
    void loadUserByUsername_returnsUserDetails() {
        Usuario u = new Usuario();
        u.setUsername("abc");
        u.setPasswordHash("ph");
        u.setEstado(EstadoUsuario.ACTIVO);
        Rol r = new Rol(); r.setNombre(RolNombre.CLIENTE);
        u.setRoles(Set.of(r));

        when(usuarioRepository.findByUsername("abc")).thenReturn(Optional.of(u));

        UserDetails ud = service.loadUserByUsername("abc");
        assertEquals("abc", ud.getUsername());
        assertEquals("ph", ud.getPassword());
        assertTrue(ud.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_missing_throws() {
        when(usuarioRepository.findByUsername("x")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("x"));
    }
}
