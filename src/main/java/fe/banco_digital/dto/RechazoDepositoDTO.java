package fe.banco_digital.dto;

import java.math.BigDecimal;

public class RechazoDepositoDTO {

    private String motivo;
    private String numeroCuentaDestino;
    private BigDecimal monto;
    private String referenciaGateway;
    private boolean devolucionSimulada;
    private String mensajeDevolucion;

    public RechazoDepositoDTO(String motivo, String numeroCuentaDestino, BigDecimal monto,
                               String referenciaGateway, String canalOrigen) {
        this.motivo = motivo;
        this.numeroCuentaDestino = numeroCuentaDestino;
        this.monto = monto;
        this.referenciaGateway = referenciaGateway;
        this.devolucionSimulada = true;
        this.mensajeDevolucion = "Devolución de " + monto + " notificada al canal [" + canalOrigen + "] con referencia " + referenciaGateway + ".";
    }

    public String getMotivo() { return motivo; }
    public String getNumeroCuentaDestino() { return numeroCuentaDestino; }
    public BigDecimal getMonto() { return monto; }
    public String getReferenciaGateway() { return referenciaGateway; }
    public boolean isDevolucionSimulada() { return devolucionSimulada; }
    public String getMensajeDevolucion() { return mensajeDevolucion; }
}
