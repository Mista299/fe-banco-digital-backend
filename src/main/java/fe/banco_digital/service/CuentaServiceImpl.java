package fe.banco_digital.service;

import fe.banco_digital.dto.AbrirCuentaSolicitudDTO;
import fe.banco_digital.dto.CierreCuentaRespuestaDTO;
import fe.banco_digital.dto.CierreCuentaSolicitudDTO;
import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.event.AuditoriaEvent;
import fe.banco_digital.exception.*;
import fe.banco_digital.mapper.CuentaMapper;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CuentaMapper cuentaMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final NumeroCuentaService numeroCuentaService;

    public CuentaServiceImpl(CuentaRepository cuentaRepository,
                             UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder,
                             CuentaMapper cuentaMapper,
                             ApplicationEventPublisher eventPublisher,
                             NumeroCuentaService numeroCuentaService) {
        this.cuentaRepository = cuentaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.cuentaMapper = cuentaMapper;
        this.eventPublisher = eventPublisher;
        this.numeroCuentaService = numeroCuentaService;
    }

    @Override
    @Transactional
    public CuentaResumenDTO abrirCuenta(AbrirCuentaSolicitudDTO solicitud, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        Long idCliente = usuario.getCliente().getIdCliente();
        List<EstadoCuenta> estadosOcupados = List.of(
                EstadoCuenta.ACTIVA, EstadoCuenta.BLOQUEADA, EstadoCuenta.PENDIENTE_APROBACION);

        if (cuentaRepository.countByClienteIdClienteAndEstadoIn(idCliente, estadosOcupados) >= 3) {
            throw new OperacionNoPermitidaException("No puedes tener más de 3 cuentas activas.");
        }

        if (solicitud.getTipoCuenta() == TipoCuenta.AHORROS) {
            if (cuentaRepository.countByClienteIdClienteAndTipoAndEstadoIn(
                    idCliente, TipoCuenta.AHORROS, estadosOcupados) >= 2) {
                throw new OperacionNoPermitidaException("Ya tienes el máximo de 2 cuentas de ahorro activas.");
            }
        } else {
            if (cuentaRepository.countByClienteIdClienteAndTipoAndEstadoIn(
                    idCliente, TipoCuenta.CORRIENTE, estadosOcupados) >= 1) {
                throw new OperacionNoPermitidaException("Ya tienes una cuenta corriente activa.");
            }
            if (cuentaRepository.countByClienteIdClienteAndTipoAndEstadoIn(
                    idCliente, TipoCuenta.AHORROS, List.of(EstadoCuenta.ACTIVA)) == 0) {
                throw new OperacionNoPermitidaException(
                        "Debes tener al menos una cuenta de ahorros activa para abrir una cuenta corriente.");
            }
        }

        Cuenta cuenta = new Cuenta();
        cuenta.setCliente(usuario.getCliente());
        cuenta.setNumeroCuenta(numeroCuentaService.generarNumeroCuenta());
        cuenta.setTipo(solicitud.getTipoCuenta());
        cuenta.setEstado(EstadoCuenta.PENDIENTE_APROBACION);
        cuenta.setSaldo(BigDecimal.ZERO);
        Cuenta guardada = cuentaRepository.save(cuenta);

        eventPublisher.publishEvent(new AuditoriaEvent(this, "SOLICITUD_APERTURA_CUENTA",
                usuario.getIdUsuario(),
                "Solicitud de apertura cuenta " + guardada.getTipo().name()
                        + " " + guardada.getNumeroCuenta() + " pendiente de aprobación"));

        return new CuentaResumenDTO(guardada);
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

        eventPublisher.publishEvent(new AuditoriaEvent(this, "CIERRE_CUENTA",
                usuario.getIdUsuario(),
                "Cuenta " + cuenta.getNumeroCuenta() + " cerrada por el cliente."));

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
                .filter(c -> c.getEstado() != EstadoCuenta.INACTIVA)
                .map(cuentaMapper::aCuentaResumenDTO)
                .collect(Collectors.toList());
    }
}