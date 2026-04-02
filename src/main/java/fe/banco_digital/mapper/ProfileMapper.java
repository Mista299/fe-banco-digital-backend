package fe.banco_digital.mapper;

import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.entity.Account;
import fe.banco_digital.entity.User;

public class ProfileMapper {

    public static ProfileDTO toDTO(User user, Account account) {

        String fullName = user.getNombre() + " " + user.getApellido();

        return new ProfileDTO(
                fullName,
                user.getNumeroIdentificacion(),
                account.getNumeroCuenta(),
                account.getSaldo()
        );
    }
}