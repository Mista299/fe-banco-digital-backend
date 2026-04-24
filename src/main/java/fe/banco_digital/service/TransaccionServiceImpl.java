package fe.banco_digital.service;

import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.mapper.TransaccionMapper;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.TransaccionRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final TransaccionMapper transaccionMapper;
    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;

    public TransaccionServiceImpl(
            TransaccionRepository transaccionRepository,
            TransaccionMapper transaccionMapper,
            UsuarioRepository usuarioRepository,
            CuentaRepository cuentaRepository) {
        this.transaccionRepository = transaccionRepository;
        this.transaccionMapper = transaccionMapper;
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public List<MovimientoDTO> obtenerMovimientos(Long idCuenta, String username) {
        verificarPropietario(idCuenta, username);
        List<Transaccion> transacciones = transaccionRepository.findByCuentaIdOrderByFechaDesc(idCuenta);
        return transaccionMapper.aListaDTO(transacciones, idCuenta);
    }

    @Override
    public List<MovimientoDTO> obtenerMovimientosPorFecha(
            Long idCuenta,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String username) {
        verificarPropietario(idCuenta, username);
        List<Transaccion> transacciones = transaccionRepository
                .findByCuentaIdAndFechaBetweenOrderByFechaDesc(idCuenta, fechaInicio, fechaFin);
        return transaccionMapper.aListaDTO(transacciones, idCuenta);
    }

    private void verificarPropietario(Long idCuenta, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);
        Long idCliente = usuario.getCliente().getIdCliente();
        cuentaRepository.findByIdCuentaAndCliente_IdCliente(idCuenta, idCliente)
                .orElseThrow(AccesoNoAutorizadoException::new);
    }
}
