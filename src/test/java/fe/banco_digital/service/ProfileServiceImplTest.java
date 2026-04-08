package fe.banco_digital.service;

import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.repository.ClienteRepository;
import fe.banco_digital.repository.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private Cliente cliente;

    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Unit Test");
        cliente.setDocumento("ABC123");

        cuenta = new Cuenta();
        cuenta.setIdCuenta(10L);
        cuenta.setNumeroCuenta("5000000009");
        cuenta.setTipo(TipoCuenta.AHORROS);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(new BigDecimal("1234.56"));
        cuenta.setCliente(cliente);
    }

    @Test
    void getProfile_returnsDto_whenDataExists() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findFirstByClienteIdClienteAndEstado(anyLong(), eq(EstadoCuenta.ACTIVA)))
            .thenReturn(Optional.of(cuenta));

        ProfileDTO dto = profileService.getProfile(1L);

        assertNotNull(dto);
        assertEquals("Unit Test", dto.getFullName());
        assertEquals("ABC123", dto.getIdentificationNumber());
        assertEquals("5000000009", dto.getAccountNumber());
        assertEquals(0, dto.getBalance().compareTo(new BigDecimal("1234.56")));
    }

    @Test
    void getProfile_throws_whenClienteMissing() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> profileService.getProfile(1L));
        assertTrue(ex.getMessage().contains("Cliente no encontrado"));
    }

    @Test
    void getProfile_throws_whenCuentaMissing() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findFirstByClienteIdClienteAndEstado(anyLong(), eq(EstadoCuenta.ACTIVA)))
            .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> profileService.getProfile(1L));
        assertTrue(ex.getMessage().contains("Cuenta activa no encontrada"));
    }
}
