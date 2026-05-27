package fe.banco_digital.dto;

import java.time.LocalDateTime;

public class BusquedaClienteResponseDTO {

    private Long idCliente;
    private String nombre;
    private String documento;           // siempre enmascarado: ***456
    private String estadoCuenta;        // ACTIVA / BLOQUEADA / INACTIVA
    private LocalDateTime fechaVinculacion;
    private String numeroCuenta;

    public BusquedaClienteResponseDTO(Long idCliente, String nombre, String documento, String estadoCuenta, LocalDateTime fechaVinculacion, String numeroCuenta) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.documento = documento;
        this.estadoCuenta = estadoCuenta;
        this.fechaVinculacion = fechaVinculacion;
        this.numeroCuenta = numeroCuenta;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public LocalDateTime getFechaVinculacion() {
        return fechaVinculacion;
    }

    public void setFechaVinculacion(LocalDateTime fechaVinculacion) {
        this.fechaVinculacion = fechaVinculacion;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }
}
