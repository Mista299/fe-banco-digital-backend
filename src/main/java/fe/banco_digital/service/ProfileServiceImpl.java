package fe.banco_digital.service;

import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.entity.Account;
import fe.banco_digital.entity.User;
import fe.banco_digital.mapper.ProfileMapper;
import fe.banco_digital.repository.AccountRepository;
import fe.banco_digital.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public ProfileServiceImpl(UserRepository userRepository,
                              AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public ProfileDTO getProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Account account = accountRepository
                .findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("Cuenta activa no encontrada"));

        return ProfileMapper.toDTO(user, account);
    }
}