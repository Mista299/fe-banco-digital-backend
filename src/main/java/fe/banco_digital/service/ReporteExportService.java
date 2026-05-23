package fe.banco_digital.service;

import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.repository.TransaccionRepository;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteExportService {

    private final TransaccionRepository transaccionRepository;

    public ReporteExportService(TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    public void exportarCsv(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            PrintWriter writer
    ) {

        List<Transaccion> transacciones =
                transaccionRepository.findByFechaBetween(
                        fechaInicio,
                        fechaFin
                );

        writer.println(
                "ID_TRANSACCION," +
                "CUENTA_ORIGEN," +
                "CUENTA_DESTINO," +
                "MONTO," +
                "TIPO," +
                "ESTADO," +
                "CANAL," +
                "FECHA"
        );

        for (Transaccion t : transacciones) {

            writer.println(
                    t.getIdTransaccion() + "," +
                    t.getCuentaOrigen().getIdCuenta() + "," +
                    t.getCuentaDestino().getIdCuenta() + "," +
                    t.getMonto() + "," +
                    String.valueOf(t.getTipo()) + "," +
                    String.valueOf(t.getEstado()) + "," +
                    t.getCanal() + "," +
                    t.getFecha()
            );
        }
    }
}