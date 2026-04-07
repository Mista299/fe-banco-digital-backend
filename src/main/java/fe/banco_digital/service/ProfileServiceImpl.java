package fe.banco_digital.service;

import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.exception.ClienteNoEncontradoException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.mapper.ProfileMapper;
import fe.banco_digital.repository.ClienteRepository;
import fe.banco_digital.repository.CuentaRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;

    public ProfileServiceImpl(ClienteRepository clienteRepository,
                              CuentaRepository cuentaRepository) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public ProfileDTO getProfile(Long userId) {
        Cliente cliente = clienteRepository.findById(userId)
                .orElseThrow(() -> new ClienteNoEncontradoException(userId));

        Cuenta cuenta = cuentaRepository
                .findFirstByClienteIdClienteAndEstado(userId, EstadoCuenta.ACTIVA)
                .orElseThrow(() -> new CuentaNoEncontradaException(userId));

        return ProfileMapper.toDTO(cliente, cuenta);
    }
}
