package fe.banco_digital.mapper;

import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;

public class ProfileMapper {

    public static ProfileDTO toDTO(Cliente cliente, Cuenta cuenta) {

        String fullName = cliente.getNombre();

        return new ProfileDTO(
                fullName,
                cliente.getDocumento(),
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo()
        );
    }
}