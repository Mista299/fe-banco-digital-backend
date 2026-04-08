package fe.banco_digital.service;

import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.mapper.TransaccionMapper;
import fe.banco_digital.repository.TransaccionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final TransaccionMapper transaccionMapper;

    public TransaccionServiceImpl(
            TransaccionRepository transaccionRepository,
            TransaccionMapper transaccionMapper) {
        this.transaccionRepository = transaccionRepository;
        this.transaccionMapper = transaccionMapper;
    }

    @Override
    public List<MovimientoDTO> obtenerMovimientos(Long idCuenta) {

        List<Transaccion> transacciones = transaccionRepository.findByCuentaIdOrderByFechaDesc(idCuenta);

        return transaccionMapper.aListaDTO(transacciones, idCuenta);
    }

    @Override
    public List<MovimientoDTO> obtenerMovimientosPorFecha(
            Long idCuenta,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

        List<Transaccion> transacciones = transaccionRepository
                .findByCuentaIdAndFechaBetweenOrderByFechaDesc(idCuenta, fechaInicio, fechaFin);

        return transaccionMapper.aListaDTO(transacciones, idCuenta);
    }
}