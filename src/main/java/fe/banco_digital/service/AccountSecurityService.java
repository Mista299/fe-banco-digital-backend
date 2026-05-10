package fe.banco_digital.service;

/**
 * Servicio encargado de la seguridad de cuentas bancarias.
 *
 * Permite bloquear y desbloquear cuentas de usuario
 * mediante validación de credenciales.
 */
public interface AccountSecurityService {

    /**
     * Bloquea una cuenta de usuario si las credenciales son válidas.
     *
     * @param username nombre de usuario
     * @param password contraseña del usuario
     */
    void bloquearCuenta(String username, String password);

    /**
     * Desbloquea una cuenta previamente bloqueada si las credenciales son válidas.
     *
     * @param username nombre de usuario
     * @param password contraseña del usuario
     */
    void desbloquearCuenta(String username, String password);
}