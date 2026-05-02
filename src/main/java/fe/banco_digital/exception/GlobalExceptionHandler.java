package fe.banco_digital.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SaldoPendienteException.class)
    public ResponseEntity<Map<String, Object>> manejarSaldoPendiente(SaldoPendienteException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> manejarSaldoInsuficiente(SaldoInsuficienteException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(SinMovimientosException.class)
    public ResponseEntity<Map<String, Object>> manejarSinMovimientos(SinMovimientosException ex) {
        return construirRespuesta(HttpStatus.OK, ex.getMessage());
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<Map<String, Object>> manejarCuentaBloqueada(CuentaBloqueadaException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> manejarConflictoConcurrencia(
            org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
        return construirRespuesta(HttpStatus.CONFLICT,
                "La operación no pudo completarse por una modificación concurrente. Intente nuevamente.");
    }

    @ExceptionHandler(AutenticacionFallidaException.class)
    public ResponseEntity<Map<String, Object>> manejarAutenticacionFallida(AutenticacionFallidaException ex) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> manejarCuentaNoEncontrada(CuentaNoEncontradaException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ClienteNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarClienteNoEncontrado(ClienteNoEncontradoException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CuentaYaCerradaException.class)
    public ResponseEntity<Map<String, Object>> manejarCuentaYaCerrada(CuentaYaCerradaException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({
            IdentificacionDuplicadaException.class,
            UsuarioYaExisteException.class,
            ClienteYaTieneUsuarioException.class,
            EmailYaExisteException.class
    })
    public ResponseEntity<Map<String, Object>> manejarConflictos(RuntimeException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({
            CredencialesInvalidasException.class,
            TokenExpiradoException.class,
            TokenInvalidoException.class
    })
    public ResponseEntity<Map<String, Object>> manejarNoAutorizado(RuntimeException ex) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<Map<String, Object>> manejarAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        return construirRespuesta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(CamposObligatoriosException.class)
    public ResponseEntity<Map<String, Object>> manejarCamposObligatorios(CamposObligatoriosException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, Object> cuerpo = cuerpoBase(HttpStatus.BAD_REQUEST,
                "Los campos faltantes son obligatorios para continuar.");
        List<Map<String, String>> detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::aDetalleCampo)
                .toList();
        cuerpo.put("detalles", detalles);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarExcepcionGeneral(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(cuerpoBase(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se pudo cargar la información, intente más tarde"));
    }

    private Map<String, String> aDetalleCampo(FieldError error) {
        Map<String, String> detalle = new LinkedHashMap<>();
        detalle.put("campo", error.getField());
        detalle.put("mensaje", error.getDefaultMessage());
        return detalle;
    }

    private ResponseEntity<Map<String, Object>> construirRespuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(cuerpoBase(estado, mensaje));
    }

    private Map<String, Object> cuerpoBase(HttpStatus estado, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now());
        cuerpo.put("estado", estado.value());
        cuerpo.put("error", estado.getReasonPhrase());
        cuerpo.put("mensaje", mensaje);
        return cuerpo;
    }
}
