package fe.banco_digital.dto;

import java.util.List;

public class ActividadClienteResponseDTO {
    private BusquedaClienteResponseDTO cliente;
    private List<MovimientoDTO> movimientos;    // lista ya filtrada y ordenada
    private int totalMovimientos;

    public ActividadClienteResponseDTO(BusquedaClienteResponseDTO cliente, List<MovimientoDTO> movimientos, int totalMovimientos) {
        this.movimientos = movimientos;
        this.totalMovimientos = totalMovimientos;
        this.cliente = cliente;
    }

    public BusquedaClienteResponseDTO getCliente() {
        return cliente;
    }

    public void setCliente(BusquedaClienteResponseDTO cliente) {
        this.cliente = cliente;
    }

    public int getTotalMovimientos() {
        return totalMovimientos;
    }

    public void setTotalMovimientos(int totalMovimientos) {
        this.totalMovimientos = totalMovimientos;
    }

    public List<MovimientoDTO> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<MovimientoDTO> movimientos) {
        this.movimientos = movimientos;
    }
}
