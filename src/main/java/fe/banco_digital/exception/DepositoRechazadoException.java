package fe.banco_digital.exception;

import fe.banco_digital.dto.RechazoDepositoDTO;

public class DepositoRechazadoException extends RuntimeException {

    private final RechazoDepositoDTO rechazo;

    public DepositoRechazadoException(RechazoDepositoDTO rechazo) {
        super(rechazo.getMotivo());
        this.rechazo = rechazo;
    }

    public RechazoDepositoDTO getRechazo() { return rechazo; }
}
