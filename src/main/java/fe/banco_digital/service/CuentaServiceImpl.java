package fe.banco_digital.service;

import fe.banco_digital.dto.CierreCuentaRespuestaDTO;
import fe.banco_digital.dto.CierreCuentaSolicitudDTO;
import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.entity.Auditoria;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.*;
import fe.banco_digital.mapper.CuentaMapper;
import fe.banco_digital.repository.AuditoriaRepository;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CuentaMapper cuentaMapper;
    private final AuditoriaRepository auditoriaRepository;

    // Inyección por constructor — patrón del equipo
    public CuentaServiceImpl(CuentaRepository cuentaRepository,
                             UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder,
                             CuentaMapper cuentaMapper,
                             AuditoriaRepository auditoriaRepository) {
        this.cuentaRepository = cuentaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.cuentaMapper = cuentaMapper;
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Cierra una cuenta de ahorros.
     * Escenario 4 → valida contraseña antes de cualquier operación.
     * Escenario 2 → impide cierre si saldo > 0.
     * Escenario 1 → cambia estado a CERRADA y confirma al usuario.
     */
    @Override
    @Transactional
    public CierreCuentaRespuestaDTO cerrarCuenta(CierreCuentaSolicitudDTO solicitud, String username) {

        // ── Escenario 4: Re-autenticación ─────────────────────────────────
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        boolean contrasenaValida = passwordEncoder.matches(
                solicitud.getContrasena(), usuario.getPasswordHash());

        if (!contrasenaValida) {
            throw new AutenticacionFallidaException();
        }

        // ── Buscar la cuenta y verificar que pertenece al usuario ──────────
        Cuenta cuenta = cuentaRepository
                .findByIdCuentaAndCliente_IdCliente(
                        solicitud.getIdCuenta(),
                        usuario.getCliente().getIdCliente())
                .orElseThrow(() -> new CuentaNoEncontradaException(solicitud.getIdCuenta()));

        // ── Validar que no esté ya cerrada ─────────────────────────────────
        if (cuenta.getEstado() == EstadoCuenta.INACTIVA) {
            throw new CuentaYaCerradaException(cuenta.getNumeroCuenta());
        }

        // ── Escenario 2: Validar saldo cero ───────────────────────────────
        if (cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
            throw new SaldoPendienteException();
        }

        // ── Escenario 1: Cambiar estado y confirmar ────────────────────────
        cuenta.setEstado(EstadoCuenta.INACTIVA);
        cuentaRepository.save(cuenta);

        Auditoria auditoria = new Auditoria();
        auditoria.setAccion("CIERRE_CUENTA");
        auditoria.setUsuario(usuario);
        auditoria.setDetalle("Cuenta " + cuenta.getNumeroCuenta() + " cerrada por el cliente.");
        auditoriaRepository.save(auditoria);

        return new CierreCuentaRespuestaDTO(
                cuenta.getNumeroCuenta(),
                cuenta.getEstado().name(),
                "El cierre de tu cuenta ha sido realizado exitosamente."
        );
    }

    /**
     * Lista las cuentas del cliente para el dashboard.
     * Escenario 3 → el mapper/DTO aplica etiqueta visual y bloquea transacciones
     * en cuentas con estado CERRADA.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CuentaResumenDTO> obtenerCuentasDelCliente(String username) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        List<Cuenta> cuentas = cuentaRepository
                .findByCliente_IdCliente(usuario.getCliente().getIdCliente());

        return cuentas.stream()
                .map(cuentaMapper::aCuentaResumenDTO)
                .collect(Collectors.toList());
    }
}