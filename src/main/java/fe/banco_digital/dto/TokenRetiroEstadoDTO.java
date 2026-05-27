package fe.banco_digital.dto;

public class TokenRetiroEstadoDTO {

    private String estado;
    private long segundosRestantes;

    public TokenRetiroEstadoDTO(String estado, long segundosRestantes) {
        this.estado = estado;
        this.segundosRestantes = segundosRestantes;
    }

    public String getEstado() { return estado; }
    public long getSegundosRestantes() { return segundosRestantes; }
}
