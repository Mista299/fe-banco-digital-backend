package fe.banco_digital.dto;

import java.math.BigDecimal;

public class TokenRetiroRespuestaDTO {

    private String codigo;
    private BigDecimal monto;
    private long segundosRestantes;

    public TokenRetiroRespuestaDTO(String codigo, BigDecimal monto, long segundosRestantes) {
        this.codigo = codigo;
        this.monto = monto;
        this.segundosRestantes = segundosRestantes;
    }

    public String getCodigo() { return codigo; }
    public BigDecimal getMonto() { return monto; }
    public long getSegundosRestantes() { return segundosRestantes; }
}
