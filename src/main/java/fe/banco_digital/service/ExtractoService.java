package fe.banco_digital.service;

public interface ExtractoService {
    byte[] generarExtracto(Long idCuenta, int anio, int mes, String username);
}
