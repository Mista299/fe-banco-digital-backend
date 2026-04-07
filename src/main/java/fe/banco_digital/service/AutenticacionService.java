package fe.banco_digital.service;

import fe.banco_digital.dto.LoginRequestDTO;
import fe.banco_digital.dto.LoginResponseDTO;
import fe.banco_digital.dto.RegistroRequestDTO;
import fe.banco_digital.dto.UsuarioRegistradoDTO;

public interface AutenticacionService {

    LoginResponseDTO login(LoginRequestDTO dto);

    UsuarioRegistradoDTO registrar(RegistroRequestDTO dto);
}
