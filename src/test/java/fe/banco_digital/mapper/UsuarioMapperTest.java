package fe.banco_digital.mapper;

import fe.banco_digital.dto.RegistroRequestDTO;
import fe.banco_digital.dto.UsuarioRegistradoDTO;
import fe.banco_digital.entity.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapper();

    @Test
    void aEntidad_setsFieldsCorrectly() {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setUsername("juan");

        Usuario u = mapper.aEntidad(dto);

        assertEquals("juan", u.getUsername());
        assertNotNull(u.getEstado());
    }

    @Test
    void aDTO_returnsExpectedFields() {
        Usuario u = new Usuario();
        u.setIdUsuario(5L);
        u.setUsername("maria");
        u.setEstado(fe.banco_digital.entity.EstadoUsuario.ACTIVO);

        UsuarioRegistradoDTO dto = mapper.aDTO(u);

        assertEquals(5L, dto.getIdUsuario());
        assertEquals("maria", dto.getUsername());
        assertEquals("ACTIVO", dto.getEstado());
    }
}
