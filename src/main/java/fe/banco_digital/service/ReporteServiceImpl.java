package fe.banco_digital.service;

import fe.banco_digital.dto.ReporteMovimientoDTO;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.entity.EstadoTransaccion;
import fe.banco_digital.repository.TransaccionRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final TransaccionRepository transaccionRepository;

    public ReporteServiceImpl(
            TransaccionRepository transaccionRepository
    ) {
        this.transaccionRepository = transaccionRepository;
    }

    // GENERAR REPORTE EN PANTALLA
    @Override
    public List<ReporteMovimientoDTO> generarReporte(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        List<Transaccion> transacciones =
                transaccionRepository.findByFechaBetween(inicio, fin);

        return transacciones.stream().map(t -> {

            ReporteMovimientoDTO dto =
                    new ReporteMovimientoDTO();

            dto.setIdTransaccion(
                    t.getIdTransaccion()
            );

            // CUENTA ORIGEN
            if (t.getCuentaOrigen() != null) {
                dto.setCuentaOrigen(
                        t.getCuentaOrigen().getIdCuenta()
                );
            }

            // CUENTA DESTINO
            if (t.getCuentaDestino() != null) {
                dto.setCuentaDestino(
                        t.getCuentaDestino().getIdCuenta()
                );
            }

            dto.setMonto(t.getMonto());

            dto.setEstado(
                    t.getEstado().name()
            );

            dto.setTipo(
             String.valueOf(t.getTipo())
);

            dto.setCanal(
                    t.getCanal()
            );

            dto.setFecha(
                    t.getFecha()
            );

            return dto;

        }).toList();
    }

    // EXPORTAR CSV
    @Override
    public byte[] exportarCSV(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        List<ReporteMovimientoDTO> datos =
                generarReporte(inicio, fin);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PrintWriter writer =
                new PrintWriter(output);

        //  ENCABEZADOS CSV
        writer.println(
                "ID_TRANSACCION," +
                "CUENTA_ORIGEN," +
                "CUENTA_DESTINO," +
                "MONTO," +
                "ESTADO," +
                "TIPO," +
                "CANAL," +
                "FECHA"
        );

        //  DATOS
        for (ReporteMovimientoDTO d : datos) {

            writer.println(
                    d.getIdTransaccion() + "," +
                    d.getCuentaOrigen() + "," +
                    d.getCuentaDestino() + "," +
                    d.getMonto() + "," +
                    d.getEstado() + "," +
                    d.getTipo() + "," +
                    d.getCanal() + "," +
                    d.getFecha()
            );
        }

        writer.flush();

        return output.toByteArray();
    }

    // TOTAL TRANSACCIONADO
    public BigDecimal calcularTotalTransaccionado(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        List<Transaccion> transacciones =
                transaccionRepository.findByFechaBetween(inicio, fin);

        return transacciones.stream()

                // SOLO EXITOSAS
                .filter(t ->
                        t.getEstado() == EstadoTransaccion.EXITOSA
                )

                // SUMAR MONTOS
                .map(Transaccion::getMonto)

                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}