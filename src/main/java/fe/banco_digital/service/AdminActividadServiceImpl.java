package fe.banco_digital.service;

import fe.banco_digital.dto.ActividadClienteResponseDTO;
import fe.banco_digital.dto.BusquedaClienteResponseDTO;
import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.event.AuditoriaEvent;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.ClienteNoEncontradoException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.mapper.TransaccionMapper;
import fe.banco_digital.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminActividadServiceImpl implements AdminActividadService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final TransferenciaExternaRepository transferenciaExternaRepository;
    private final TransferenciaInternacionalRepository transferenciaInternacionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransaccionMapper transaccionMapper;
    private final ApplicationEventPublisher eventPublisher;

    public AdminActividadServiceImpl(ClienteRepository clienteRepository, CuentaRepository cuentaRepository, MovimientoRepository movimientoRepository, TransferenciaRepository transferenciaRepository, TransferenciaExternaRepository transferenciaExternaRepository, TransferenciaInternacionalRepository transferenciaInternacionalRepository, UsuarioRepository usuarioRepository, TransaccionMapper transaccionMapper, ApplicationEventPublisher eventPublisher) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.transferenciaExternaRepository = transferenciaExternaRepository;
        this.transferenciaInternacionalRepository = transferenciaInternacionalRepository;
        this.usuarioRepository = usuarioRepository;
        this.transaccionMapper = transaccionMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public ActividadClienteResponseDTO consultarActividadPorDocumento(
            String documento, LocalDateTime fechaInicio, LocalDateTime fechaFin,
            String tipoMovimiento, String usernameAdmin) {

        Cliente cliente = clienteRepository.findByDocumento(documento)
                .orElseThrow(() -> new ClienteNoEncontradoException(-1L));

        return construirActividad(cliente, fechaInicio, fechaFin, tipoMovimiento, usernameAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public ActividadClienteResponseDTO consultarActividadPorNumeroCuenta(
            String numeroCuenta, LocalDateTime fechaInicio, LocalDateTime fechaFin,
            String tipoMovimiento, String usernameAdmin) {

        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(-1L));

        // Cargar el cliente desde la cuenta — LAZY, usar getId
        Cliente cliente = clienteRepository.findById(cuenta.getCliente().getIdCliente())
                .orElseThrow(() -> new ClienteNoEncontradoException(-1L));

        return construirActividad(cliente, fechaInicio, fechaFin, tipoMovimiento, usernameAdmin);
    }

    private ActividadClienteResponseDTO construirActividad(
            Cliente cliente, LocalDateTime fechaInicio, LocalDateTime fechaFin,
            String tipoMovimiento, String usernameAdmin) {

        // Escenario 4: determinar rol del admin para enmascarado
        Usuario admin = usuarioRepository.findByUsername(usernameAdmin)
                .orElseThrow(AutenticacionFallidaException::new);
        boolean esGerente = admin.getRoles().stream()
                .anyMatch(r -> r.getNombre() == RolNombre.GERENTE);

        // Obtener todas las cuentas del cliente
        List<Cuenta> cuentas = cuentaRepository.findByCliente_IdClienteOrderByIdCuentaAsc(cliente.getIdCliente());

        // Escenario 1: datos básicos — tomar la primera cuenta activa o la primera disponible
        if (cuentas.isEmpty()) {
            throw new CuentaNoEncontradaException(cliente.getIdCliente());
        }
        Cuenta cuentaPrincipal = cuentas.stream()
                .filter(c -> c.getEstado() == EstadoCuenta.ACTIVA)
                .findFirst()
                .orElse(cuentas.get(0));

        String numeroCuentaMostrado = esGerente
                ? cuentaPrincipal.getNumeroCuenta()
                : enmascararCuenta(cuentaPrincipal.getNumeroCuenta());

        String documentoMostrado = esGerente
                ? cliente.getDocumento()
                : enmascararDocumento(cliente.getDocumento());

        BusquedaClienteResponseDTO datosCliente = new BusquedaClienteResponseDTO(
                cliente.getIdCliente(),
                cliente.getNombre(),
                documentoMostrado,
                cuentaPrincipal.getEstado().name(),
                cliente.getFechaRegistro(),
                numeroCuentaMostrado
        );

        // Escenario 2: consolidar movimientos de TODAS las cuentas del cliente
        // usando los 4 repositorios + el mapper que ya existe en el proyecto

        List<MovimientoDTO> todos = new ArrayList<>();
        Set<Long> transferenciasVistas = new HashSet<>();

        for (Cuenta cuenta : cuentas) {
            Long id = cuenta.getIdCuenta();

            List<Movimiento> movs = (fechaInicio != null && fechaFin != null)
                    ? movimientoRepository.findByCuenta_IdCuentaAndFechaBetweenOrderByFechaDesc(id, fechaInicio, fechaFin)
                    : movimientoRepository.findByCuenta_IdCuentaOrderByFechaDesc(id);

            // Bug 3: deduplicar transferencias internas entre cuentas del mismo cliente
            List<Transferencia> transfs = (fechaInicio != null && fechaFin != null)
                    ? transferenciaRepository.findByCuentaIdAndFechaBetweenOrderByFechaDesc(id, fechaInicio, fechaFin)
                    : transferenciaRepository.findByCuentaIdOrderByFechaDesc(id);
            List<Transferencia> transfsNuevas = transfs.stream()
                    .filter(t -> transferenciasVistas.add(t.getIdTransferencia()))
                    .collect(Collectors.toList());

            List<TransferenciaExterna> externas = (fechaInicio != null && fechaFin != null)
                    ? transferenciaExternaRepository.findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(id, fechaInicio, fechaFin)
                    : transferenciaExternaRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(id);

            List<TransferenciaInternacional> internacionales = (fechaInicio != null && fechaFin != null)
                    ? transferenciaInternacionalRepository.findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(id, fechaInicio, fechaFin)
                    : transferenciaInternacionalRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(id);

            List<MovimientoDTO> dtosParaCuenta = transaccionMapper.aListaDTOUnificada(
                    movs, transfsNuevas, id, externas, internacionales);

            // Bug 5: calcular saldo acumulado por cuenta (iterando de más reciente a más antiguo)
            BigDecimal saldoAcumulado = cuenta.getSaldo();
            for (MovimientoDTO dto : dtosParaCuenta) {
                dto.setSaldoResultante(saldoAcumulado);
                saldoAcumulado = saldoAcumulado.subtract(dto.getMonto());
            }

            todos.addAll(dtosParaCuenta);
        }

        // Reordenar el consolidado total cronológicamente
        todos.sort(Comparator.comparing(MovimientoDTO::getFechaHora).reversed());

        // Escenario 3: filtrar por tipo si se especifica
        if (tipoMovimiento != null && !tipoMovimiento.isBlank()) {
            String filtro = tipoMovimiento.toUpperCase();
            todos = todos.stream()
                    .filter(m -> m.getConcepto() != null && m.getConcepto().contains(filtro))
                    .collect(Collectors.toList());
        }

        // Escenario 4: registrar auditoría asíncrona
        eventPublisher.publishEvent(new AuditoriaEvent(this,
                "CONSULTA_ACTIVIDAD_ADMIN",
                admin.getIdUsuario(),
                "Admin " + usernameAdmin + " consultó actividad del cliente ID "
                        + cliente.getIdCliente()));

        return new ActividadClienteResponseDTO(datosCliente, todos, todos.size());
    }

    private String enmascararCuenta(String numero) {
        if (numero == null || numero.length() < 4) return "****";
        return "******" + numero.substring(numero.length() - 4);
    }

    private String enmascararDocumento(String documento) {
        if (documento == null || documento.length() < 3) return "***";
        return "***" + documento.substring(documento.length() - 3);
    }
}