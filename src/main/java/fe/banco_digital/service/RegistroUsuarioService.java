package fe.banco_digital.service;

import fe.banco_digital.dto.RegistroNuevoUsuarioRequestDTO;
import fe.banco_digital.dto.RegistroNuevoUsuarioResponseDTO;
import fe.banco_digital.dto.ValidacionIdentidadResponseDTO;
import fe.banco_digital.dto.ValidarIdentidadRequestDTO;

public interface RegistroUsuarioService {
    ValidacionIdentidadResponseDTO validarIdentidad(ValidarIdentidadRequestDTO dto);
    RegistroNuevoUsuarioResponseDTO registrar(RegistroNuevoUsuarioRequestDTO dto);
}
