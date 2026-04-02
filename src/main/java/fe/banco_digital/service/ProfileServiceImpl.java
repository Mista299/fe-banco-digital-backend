package fe.banco_digital.service;

import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
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

        Cliente cliente = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Cuenta cuenta = accountRepository
                .findFirstByClienteIdClienteAndEstado(userId, EstadoCuenta.ACTIVA)
                .orElseThrow(() -> new RuntimeException("Cuenta activa no encontrada"));

        return ProfileMapper.toDTO(cliente, cuenta);
    }
}