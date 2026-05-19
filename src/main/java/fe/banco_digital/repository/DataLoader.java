package fe.banco_digital.repository;

import fe.banco_digital.entity.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
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

            Rol rolAdmin = rolRepo.findByNombre(RolNombre.ADMIN)
                    .orElseGet(() -> {
                        Rol r = new Rol();
                        r.setNombre(RolNombre.ADMIN);
                        return rolRepo.save(r);
                    });

            Rol rolCliente = rolRepo.findByNombre(RolNombre.CLIENTE)
                    .orElseGet(() -> {
                        Rol r = new Rol();
                        r.setNombre(RolNombre.CLIENTE);
                        return rolRepo.save(r);
                    });

            Cliente c1 = clienteRepo.findByDocumento("123456789").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Bryan Molina");
                c.setDocumento("123456789");
                c.setFechaExpedicion(LocalDate.of(2020, 1, 10));
                c.setEmail("bryan@example.com");
                c.setDireccion("Calle 10 #20-30");
                c.setTelefono("3000000001");
                return clienteRepo.save(c);
            });

            Cliente c2 = clienteRepo.findByDocumento("987654321").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Ana Gómez");
                c.setDocumento("987654321");
                c.setFechaExpedicion(LocalDate.of(2019, 5, 2));
                c.setEmail("ana@example.com");
                c.setDireccion("Carrera 15 #8-45");
                c.setTelefono("3000000002");
                return clienteRepo.save(c);
            });

            Cliente c3 = clienteRepo.findByDocumento("111111111").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Carlos Pérez");
                c.setDocumento("111111111");
                c.setFechaExpedicion(LocalDate.of(2021, 7, 19));
                c.setEmail("carlos@example.com");
                c.setDireccion("Diagonal 50 #14-90");
                c.setTelefono("3000000003");
                return clienteRepo.save(c);
            });

            Cliente c4 = clienteRepo.findByDocumento("222222222").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Laura Martínez");
                c.setDocumento("222222222");
                c.setFechaExpedicion(LocalDate.of(2018, 3, 11));
                c.setEmail("laura@example.com");
                c.setDireccion("Transversal 12 #45-67");
                c.setTelefono("3000000004");
                return clienteRepo.save(c);
            });

            Cliente c5 = clienteRepo.findByDocumento("333333333").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Jorge Ramírez");
                c.setDocumento("333333333");
                c.setFechaExpedicion(LocalDate.of(2017, 9, 30));
                c.setEmail("jorge@example.com");
                c.setDireccion("Avenida 80 #30-12");
                c.setTelefono("3000000005");
                return clienteRepo.save(c);
            });

            Cliente c6 = clienteRepo.findByDocumento("444444444").orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNombre("Sofía Vargas");
                c.setDocumento("444444444");
                c.setFechaExpedicion(LocalDate.of(2022, 6, 15));
                c.setEmail("sofia@example.com");
                c.setDireccion("Calle 5 #10-20");
                c.setTelefono("3000000006");
                return clienteRepo.save(c);
            });
            // ── 6 Usuarios (1 por cliente) ────────────────────────────────────
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
                u.setEstado(EstadoUsuario.BLOQUEADO);
                u.setCliente(c5);
                u.setRoles(Set.of(rolCliente));
                return usuarioRepo.save(u);
            });

            Usuario u6 = usuarioRepo.findByUsername("sofia").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setUsername("sofia");
                u.setPasswordHash(passwordEncoder.encode("sofia123"));
                u.setEstado(EstadoUsuario.ACTIVO);
                u.setCliente(c6);
                u.setRoles(Set.of(rolCliente));
                return usuarioRepo.save(u);
            });

            // Saldos calculados según el historial de transacciones semilla:
            // cta1: depósito 1.000.000 − transferencia 50.000 = 950.000
            // cta2: depósito 1.500.000 − retiro 200.000 − ACH 75.000 = 1.225.000
            // cta3: sin transacciones = 0
            // cta4: transferencia 50.000 + depósito 100.000 = 150.000
            // cta5: sin transacciones, INACTIVA = 0
            // cta6: depósito 500.000 − ACH 200.000 + reverso 200.000 = 500.000
            Cuenta cta1 = cuentaRepo.findByNumeroCuenta("00010001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00010001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("950000.00"));
                cta.setCliente(c1);
                return cuentaRepo.save(cta);
            });

            Cuenta cta2 = cuentaRepo.findByNumeroCuenta("00020001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00020001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("1225000.00"));
                cta.setCliente(c2);
                return cuentaRepo.save(cta);
            });

            Cuenta cta3 = cuentaRepo.findByNumeroCuenta("00030001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00030001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(BigDecimal.ZERO);
                cta.setCliente(c3);
                return cuentaRepo.save(cta);
            });

            Cuenta cta4 = cuentaRepo.findByNumeroCuenta("00040001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00040001");
                cta.setTipo(TipoCuenta.CORRIENTE);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("150000.00"));
                cta.setCliente(c4);
                return cuentaRepo.save(cta);
            });

            Cuenta cta5 = cuentaRepo.findByNumeroCuenta("00050001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00050001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.INACTIVA);
                cta.setSaldo(BigDecimal.ZERO);
                cta.setCliente(c5);
                return cuentaRepo.save(cta);
            });

            Cuenta cta6 = cuentaRepo.findByNumeroCuenta("00060001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00060001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("500000.00"));
                cta.setCliente(c6);
                return cuentaRepo.save(cta);
            });

            if (transaccionRepo.count() == 0) {
                // Fechas escalonadas para que el historial se vea realista
                java.time.LocalDateTime hace7d = java.time.LocalDateTime.now().minusDays(7);
                java.time.LocalDateTime hace6d = java.time.LocalDateTime.now().minusDays(6);
                java.time.LocalDateTime hace5d = java.time.LocalDateTime.now().minusDays(5);
                java.time.LocalDateTime hace4d = java.time.LocalDateTime.now().minusDays(4);
                java.time.LocalDateTime hace3d = java.time.LocalDateTime.now().minusDays(3);
                java.time.LocalDateTime hace2d = java.time.LocalDateTime.now().minusDays(2);
                java.time.LocalDateTime hace1d = java.time.LocalDateTime.now().minusDays(1);

                // cta1 (Bryan): depósito 1.000.000 → saldo 1.000.000
                Transaccion t1 = new Transaccion();
                t1.setTipo(TipoTransaccion.DEPOSITO);
                t1.setEstado(EstadoTransaccion.EXITOSA);
                t1.setMonto(new BigDecimal("1000000.00"));
                t1.setCuentaOrigen(null);
                t1.setCuentaDestino(cta1);
                t1.setFecha(hace7d);
                transaccionRepo.save(t1);

                // cta2 (Ana): depósito 1.500.000 → saldo 1.500.000
                Transaccion t2 = new Transaccion();
                t2.setTipo(TipoTransaccion.DEPOSITO);
                t2.setEstado(EstadoTransaccion.EXITOSA);
                t2.setMonto(new BigDecimal("1500000.00"));
                t2.setCuentaOrigen(null);
                t2.setCuentaDestino(cta2);
                t2.setFecha(hace7d.plusHours(2));
                transaccionRepo.save(t2);

                // cta6 (Sofía): depósito inicial 500.000 → saldo 500.000
                Transaccion t3 = new Transaccion();
                t3.setTipo(TipoTransaccion.DEPOSITO);
                t3.setEstado(EstadoTransaccion.EXITOSA);
                t3.setMonto(new BigDecimal("500000.00"));
                t3.setCuentaOrigen(null);
                t3.setCuentaDestino(cta6);
                t3.setFecha(hace6d);
                transaccionRepo.save(t3);

                // cta1 → cta4: transferencia 50.000 — cta1 queda 950.000, cta4 queda 50.000
                Transaccion t4 = new Transaccion();
                t4.setTipo(TipoTransaccion.TRANSFERENCIA);
                t4.setEstado(EstadoTransaccion.EXITOSA);
                t4.setMonto(new BigDecimal("50000.00"));
                t4.setCuentaOrigen(cta1);
                t4.setCuentaDestino(cta4);
                t4.setFecha(hace5d);
                transaccionRepo.save(t4);

                // cta2: retiro 200.000 → saldo 1.300.000
                Transaccion t5 = new Transaccion();
                t5.setTipo(TipoTransaccion.RETIRO);
                t5.setEstado(EstadoTransaccion.EXITOSA);
                t5.setMonto(new BigDecimal("200000.00"));
                t5.setCuentaOrigen(cta2);
                t5.setCuentaDestino(null);
                t5.setFecha(hace4d);
                transaccionRepo.save(t5);

                // cta4: depósito 100.000 → saldo 150.000
                Transaccion t6 = new Transaccion();
                t6.setTipo(TipoTransaccion.DEPOSITO);
                t6.setEstado(EstadoTransaccion.EXITOSA);
                t6.setMonto(new BigDecimal("100000.00"));
                t6.setCuentaOrigen(null);
                t6.setCuentaDestino(cta4);
                t6.setFecha(hace3d);
                transaccionRepo.save(t6);

                // cta2 → Bancolombia: ACH exitosa 75.000 → saldo 1.225.000
                Transaccion t7 = new Transaccion();
                t7.setTipo(TipoTransaccion.TRANSFERENCIA_INTERBANCARIA);
                t7.setEstado(EstadoTransaccion.EXITOSA);
                t7.setMonto(new BigDecimal("75000.00"));
                t7.setCuentaOrigen(cta2);
                t7.setCuentaDestino(null);
                t7.setBancoDestino("Bancolombia");
                t7.setTipoCuentaDestinoExterna("AHORROS");
                t7.setNumeroCuentaDestinoExterna("45678901234");
                t7.setTipoDocumentoReceptor("CC");
                t7.setNumeroDocumentoReceptor("9876543210");
                t7.setNombreReceptorExterno("Pedro Suárez");
                t7.setReferenciaExterna("REF-2026-001");
                t7.setFecha(hace2d);
                transaccionRepo.save(t7);

                // cta6 → Davivienda: ACH reversada 200.000 (dinero debita y regresa)
                String refAchReversada = "REF-2026-002";
                String motivoRechazoAch = "Cuenta destino no existe en el banco receptor";

                Transaccion t8 = new Transaccion();
                t8.setTipo(TipoTransaccion.TRANSFERENCIA_INTERBANCARIA);
                t8.setEstado(EstadoTransaccion.REVERSADA);
                t8.setMonto(new BigDecimal("200000.00"));
                t8.setCuentaOrigen(cta6);
                t8.setCuentaDestino(null);
                t8.setBancoDestino("Davivienda");
                t8.setTipoCuentaDestinoExterna("CORRIENTE");
                t8.setNumeroCuentaDestinoExterna("11223344556");
                t8.setTipoDocumentoReceptor("CC");
                t8.setNumeroDocumentoReceptor("1122334455");
                t8.setNombreReceptorExterno("María López");
                t8.setReferenciaExterna(refAchReversada);
                t8.setMotivoRechazo(motivoRechazoAch);
                t8.setFecha(hace1d);
                transaccionRepo.save(t8);

                // REVERSO_ACH: regresa los 200.000 a cta6 → saldo 500.000
                Transaccion t9 = new Transaccion();
                t9.setTipo(TipoTransaccion.REVERSO_ACH);
                t9.setEstado(EstadoTransaccion.EXITOSA);
                t9.setMonto(new BigDecimal("200000.00"));
                t9.setCuentaOrigen(null);
                t9.setCuentaDestino(cta6);
                t9.setReferenciaExterna(refAchReversada);
                t9.setMotivoRechazo(motivoRechazoAch);
                t9.setFecha(hace1d.plusMinutes(5));
                transaccionRepo.save(t9);
            }

            if (auditoriaRepo.count() == 0) {
                auditoriaRepo.save(crearAuditoria("LOGIN", u1, "Inicio de sesión exitoso de bryan"));
                auditoriaRepo.save(crearAuditoria("CONSULTA_PERFIL", u2, "Consulta de perfil de cliente"));
                auditoriaRepo.save(crearAuditoria("CIERRE_CUENTA", u3, "Cierre exitoso de la cuenta 00030001"));
                auditoriaRepo.save(crearAuditoria("INTENTO_CIERRE", u4, "Intento de cierre rechazado por saldo pendiente"));
                auditoriaRepo.save(crearAuditoria("BLOQUEO_USUARIO", u5, "Usuario bloqueado para pruebas de seguridad"));
                auditoriaRepo.save(crearAuditoria("TRANSFERENCIA_ACH", u6, "Transferencia interbancaria rechazada por cuenta destino inexistente"));
            }
        };
    }

    private Auditoria crearAuditoria(String accion, Usuario usuario, String detalle) {
        Auditoria auditoria = new Auditoria();
        auditoria.setAccion(accion);
        auditoria.setUsuario(usuario);
        auditoria.setDetalle(detalle);
        return auditoria;
    }
}
