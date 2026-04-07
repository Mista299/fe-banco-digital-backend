package fe.banco_digital.mapper;

import fe.banco_digital.dto.RegistroRequestDTO;
import fe.banco_digital.dto.UsuarioRegistradoDTO;
import fe.banco_digital.entity.EstadoUsuario;
import fe.banco_digital.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario aEntidad(RegistroRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setEstado(EstadoUsuario.ACTIVO);
        return usuario;
    }

    public UsuarioRegistradoDTO aDTO(Usuario usuario) {
        UsuarioRegistradoDTO dto = new UsuarioRegistradoDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setUsername(usuario.getUsername());
        dto.setEstado(usuario.getEstado().name());
        return dto;
    }
}
