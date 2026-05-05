package fe.banco_digital.dto;

import java.time.LocalDateTime;

public class DepositoPendienteRespuestaDTO {

    private String referenciaGateway;
    private LocalDateTime fechaExpiracion;
    private long segundosRestantes;
    private String estado;

    public DepositoPendienteRespuestaDTO(String referenciaGateway, LocalDateTime fechaExpiracion,
                                          long segundosRestantes, String estado) {
        this.referenciaGateway = referenciaGateway;
        this.fechaExpiracion = fechaExpiracion;
        this.segundosRestantes = segundosRestantes;
        this.estado = estado;
    }

    public String getReferenciaGateway() { return referenciaGateway; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public long getSegundosRestantes() { return segundosRestantes; }
    public String getEstado() { return estado; }
}
