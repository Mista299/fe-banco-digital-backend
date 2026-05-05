package fe.banco_digital.service;

import fe.banco_digital.repository.CuentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NumeroCuentaServiceImplTest {

    @Mock CuentaRepository cuentaRepository;

    @InjectMocks NumeroCuentaServiceImpl service;

    @Test
    void generarNumeroCuenta_retornaNumeroDeOchoDigitos() {
        when(cuentaRepository.existsByNumeroCuenta(any())).thenReturn(false);

        String numero = service.generarNumeroCuenta();

        assertNotNull(numero);
        assertEquals(8, numero.length());
        assertTrue(Long.parseLong(numero) >= 10_000_000L);
        assertTrue(Long.parseLong(numero) <= 99_999_999L);
    }

    @Test
    void generarNumeroCuenta_reintentaCuandoNumeroDuplicado() {
        when(cuentaRepository.existsByNumeroCuenta(any()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        String numero = service.generarNumeroCuenta();

        assertNotNull(numero);
        verify(cuentaRepository, atLeast(3)).existsByNumeroCuenta(any());
    }
}
