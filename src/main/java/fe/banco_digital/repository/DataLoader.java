package fe.banco_digital.repository;

import fe.banco_digital.entity.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Set;

@Configuration
@Profile("seed")
public class DataLoader {

    @Bean
    CommandLineRunner init(
            ClienteRepository clienteRepo,
            UsuarioRepository usuarioRepo,
            RolRepository rolRepo,
            CuentaRepository cuentaRepo,
            TransaccionRepository transaccionRepo,
            AuditoriaRepository auditoriaRepo,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            // ── Roles (2 posibles por enum: ADMIN, CLIENTE) ───────────────────
            Rol rolAdmin = rolRepo.findByNombre(RolNombre.ADMIN)
                    .orElseGet(() -> { Rol r = new Rol(); r.setNombre(RolNombre.ADMIN); return rolRepo.save(r); });

            Rol rolCliente = rolRepo.findByNombre(RolNombre.CLIENTE)
                    .orElseGet(() -> { Rol r = new Rol(); r.setNombre(RolNombre.CLIENTE); return rolRepo.save(r); });

            // ── 5 Clientes ────────────────────────────────────────────────────
            Cliente c1 = clienteRepo.findByDocumento("123456789").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Bryan Molina");
                c.setDocumento("123456789");
                c.setEmail("bryan@example.com");
                c.setTelefono("3000000001");
                return clienteRepo.save(c);
            });

            Cliente c2 = clienteRepo.findByDocumento("987654321").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Ana Gómez");
                c.setDocumento("987654321");
                c.setEmail("ana@example.com");
                c.setTelefono("3000000002");
                return clienteRepo.save(c);
            });

            Cliente c3 = clienteRepo.findByDocumento("111111111").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Carlos Pérez");
                c.setDocumento("111111111");
                c.setEmail("carlos@example.com");
                c.setTelefono("3000000003");
                return clienteRepo.save(c);
            });

            Cliente c4 = clienteRepo.findByDocumento("222222222").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Laura Martínez");
                c.setDocumento("222222222");
                c.setEmail("laura@example.com");
                c.setTelefono("3000000004");
                return clienteRepo.save(c);
            });

            Cliente c5 = clienteRepo.findByDocumento("333333333").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Jorge Ramírez");
                c.setDocumento("333333333");
                c.setEmail("jorge@example.com");
                c.setTelefono("3000000005");
                return clienteRepo.save(c);
            });

            clienteRepo.findByDocumento("444444444").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Sofía Vargas");
                c.setDocumento("444444444");
                c.setEmail("sofia@example.com");
                c.setTelefono("3000000006");
                return clienteRepo.save(c);
            });

            // ── 5 Usuarios (1 por cliente) ────────────────────────────────────
            Usuario u1 = usuarioRepo.findByUsername("bryan").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setUsername("bryan");
                u.setPasswordHash(passwordEncoder.encode("bryan123"));
                u.setEstado(EstadoUsuario.ACTIVO);
                u.setCliente(c1);
                u.setRoles(Set.of(rolAdmin));
                return usuarioRepo.save(u);
            });

            Usuario u2 = usuarioRepo.findByUsername("ana").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setUsername("ana");
                u.setPasswordHash(passwordEncoder.encode("ana123"));
                u.setEstado(EstadoUsuario.ACTIVO);
                u.setCliente(c2);
                u.setRoles(Set.of(rolCliente));
                return usuarioRepo.save(u);
            });

            Usuario u3 = usuarioRepo.findByUsername("carlos").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setUsername("carlos");
                u.setPasswordHash(passwordEncoder.encode("carlos123"));
                u.setEstado(EstadoUsuario.ACTIVO);
                u.setCliente(c3);
                u.setRoles(Set.of(rolCliente));
                return usuarioRepo.save(u);
            });

            Usuario u4 = usuarioRepo.findByUsername("laura").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setUsername("laura");
                u.setPasswordHash(passwordEncoder.encode("laura123"));
                u.setEstado(EstadoUsuario.ACTIVO);
                u.setCliente(c4);
                u.setRoles(Set.of(rolCliente));
                return usuarioRepo.save(u);
            });

            Usuario u5 = usuarioRepo.findByUsername("jorge").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setUsername("jorge");
                u.setPasswordHash(passwordEncoder.encode("jorge123"));
                u.setEstado(EstadoUsuario.BLOQUEADO);   // usuario bloqueado — prueba de acceso denegado
                u.setCliente(c5);
                u.setRoles(Set.of(rolCliente));
                return usuarioRepo.save(u);
            });

            // ── 5 Cuentas (una por cliente, estados variados) ─────────────────
            // Bryan  — ACTIVA con saldo alto
            Cuenta cta1 = cuentaRepo.findByNumeroCuenta("00010001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00010001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("850000.00"));
                cta.setCliente(c1);
                return cuentaRepo.save(cta);
            });

            // Ana    — ACTIVA con saldo alto
            Cuenta cta2 = cuentaRepo.findByNumeroCuenta("00020001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00020001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("1200000.00"));
                cta.setCliente(c2);
                return cuentaRepo.save(cta);
            });

            // Carlos — ACTIVA con saldo CERO → prueba Escenario 1 (cierre exitoso)
            Cuenta cta3 = cuentaRepo.findByNumeroCuenta("00030001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00030001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(BigDecimal.ZERO);
                cta.setCliente(c3);
                return cuentaRepo.save(cta);
            });

            // Laura  — ACTIVA con saldo pendiente → prueba Escenario 2 (no se puede cerrar)
            Cuenta cta4 = cuentaRepo.findByNumeroCuenta("00040001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00040001");
                cta.setTipo(TipoCuenta.CORRIENTE);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("50000.00"));
                cta.setCliente(c4);
                return cuentaRepo.save(cta);
            });

            // Jorge  — INACTIVA saldo 0 → prueba Escenario 3 (etiqueta "Cuenta Cerrada")
            Cuenta cta5 = cuentaRepo.findByNumeroCuenta("00050001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00050001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.INACTIVA);
                cta.setSaldo(BigDecimal.ZERO);
                cta.setCliente(c5);
                return cuentaRepo.save(cta);
            });

            // ── 5 Transacciones ───────────────────────────────────────────────
            if (transaccionRepo.count() == 0) {

                // 1. Depósito externo → Bryan
                Transaccion t1 = new Transaccion();
                t1.setTipo(TipoTransaccion.DEPOSITO);
                t1.setEstado(EstadoTransaccion.EXITOSA);
                t1.setMonto(new BigDecimal("850000.00"));
                t1.setCuentaOrigen(null);
                t1.setCuentaDestino(cta1);
                transaccionRepo.save(t1);

                // 2. Depósito externo → Ana
                Transaccion t2 = new Transaccion();
                t2.setTipo(TipoTransaccion.DEPOSITO);
                t2.setEstado(EstadoTransaccion.EXITOSA);
                t2.setMonto(new BigDecimal("1200000.00"));
                t2.setCuentaOrigen(null);
                t2.setCuentaDestino(cta2);
                transaccionRepo.save(t2);

                // 3. Transferencia Bryan → Laura
                Transaccion t3 = new Transaccion();
                t3.setTipo(TipoTransaccion.TRANSFERENCIA);
                t3.setEstado(EstadoTransaccion.EXITOSA);
                t3.setMonto(new BigDecimal("50000.00"));
                t3.setCuentaOrigen(cta1);
                t3.setCuentaDestino(cta4);
                transaccionRepo.save(t3);

                // 4. Retiro de Ana
                Transaccion t4 = new Transaccion();
                t4.setTipo(TipoTransaccion.RETIRO);
                t4.setEstado(EstadoTransaccion.EXITOSA);
                t4.setMonto(new BigDecimal("100000.00"));
                t4.setCuentaOrigen(cta2);
                t4.setCuentaDestino(null);
                transaccionRepo.save(t4);

                // 5. Retiro fallido — Laura intentó retirar más de su saldo
                Transaccion t5 = new Transaccion();
                t5.setTipo(TipoTransaccion.RETIRO);
                t5.setEstado(EstadoTransaccion.FALLIDA);
                t5.setMonto(new BigDecimal("200000.00"));
                t5.setCuentaOrigen(cta4);
                t5.setCuentaDestino(null);
                transaccionRepo.save(t5);
            }

            // ── 5 Registros de Auditoría ──────────────────────────────────────
            if (auditoriaRepo.count() == 0) {

                Auditoria a1 = new Auditoria();
                a1.setAccion("LOGIN");
                a1.setUsuario(u1);
                a1.setDetalle("Inicio de sesión exitoso — bryan");
                auditoriaRepo.save(a1);

                Auditoria a2 = new Auditoria();
                a2.setAccion("LOGIN");
                a2.setUsuario(u2);
                a2.setDetalle("Inicio de sesión exitoso — ana");
                auditoriaRepo.save(a2);

                Auditoria a3 = new Auditoria();
                a3.setAccion("CIERRE_CUENTA");
                a3.setUsuario(u5);
                a3.setDetalle("Cuenta " + cta5.getNumeroCuenta() + " cerrada por el cliente.");
                auditoriaRepo.save(a3);

                Auditoria a4 = new Auditoria();
                a4.setAccion("BLOQUEO_CUENTA");
                a4.setUsuario(u1);
                a4.setDetalle("Usuario jorge bloqueado por intentos fallidos.");
                auditoriaRepo.save(a4);

                Auditoria a5 = new Auditoria();
                a5.setAccion("CONSULTA_SALDO");
                a5.setUsuario(u3);
                a5.setDetalle("Consulta de saldo de cuenta " + cta3.getNumeroCuenta());
                auditoriaRepo.save(a5);
            }
        };
    }
}
