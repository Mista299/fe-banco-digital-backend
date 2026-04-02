package fe.banco_digital.repository;

import fe.banco_digital.entity.Account;
import fe.banco_digital.entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner init(UserRepository userRepo, AccountRepository accRepo) {
        return args -> {

            // ===== Crear usuario =====
            User user = new User();
            user.setNombre("Bryan");
            user.setApellido("Molina");
            user.setNumeroIdentificacion("123456789");

            userRepo.save(user);

            // ===== Crear cuenta =====
            Account acc = new Account();
            acc.setNumeroCuenta("1234567890"); // 10 dígitos
            acc.setSaldo(new BigDecimal("150000"));
            acc.setActive(true);
            acc.setUser(user); // relación correcta

            accRepo.save(acc);
        };
    }
}
