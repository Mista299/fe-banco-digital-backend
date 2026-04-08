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

            Cuenta cta1 = cuentaRepo.findByNumeroCuenta("00010001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00010001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("850000.00"));
                cta.setCliente(c1);
                return cuentaRepo.save(cta);
            });

            Cuenta cta2 = cuentaRepo.findByNumeroCuenta("00020001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00020001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("1200000.00"));
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
                cta.setSaldo(new BigDecimal("50000.00"));
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

            if (transaccionRepo.count() == 0) {
                Transaccion t1 = new Transaccion();
                t1.setTipo(TipoTransaccion.DEPOSITO);
                t1.setEstado(EstadoTransaccion.EXITOSA);
                t1.setMonto(new BigDecimal("850000.00"));
                t1.setCuentaOrigen(null);
                t1.setCuentaDestino(cta1);
                transaccionRepo.save(t1);

                Transaccion t2 = new Transaccion();
                t2.setTipo(TipoTransaccion.DEPOSITO);
                t2.setEstado(EstadoTransaccion.EXITOSA);
                t2.setMonto(new BigDecimal("1200000.00"));
                t2.setCuentaOrigen(null);
                t2.setCuentaDestino(cta2);
                transaccionRepo.save(t2);

                Transaccion t3 = new Transaccion();
                t3.setTipo(TipoTransaccion.TRANSFERENCIA);
                t3.setEstado(EstadoTransaccion.EXITOSA);
                t3.setMonto(new BigDecimal("50000.00"));
                t3.setCuentaOrigen(cta1);
                t3.setCuentaDestino(cta4);
                transaccionRepo.save(t3);

                Transaccion t4 = new Transaccion();
                t4.setTipo(TipoTransaccion.RETIRO);
                t4.setEstado(EstadoTransaccion.EXITOSA);
                t4.setMonto(new BigDecimal("200000.00"));
                t4.setCuentaOrigen(cta2);
                t4.setCuentaDestino(null);
                transaccionRepo.save(t4);

                Transaccion t5 = new Transaccion();
                t5.setTipo(TipoTransaccion.DEPOSITO);
                t5.setEstado(EstadoTransaccion.EXITOSA);
                t5.setMonto(new BigDecimal("100000.00"));
                t5.setCuentaOrigen(null);
                t5.setCuentaDestino(cta4);
                transaccionRepo.save(t5);
            }

            if (auditoriaRepo.count() == 0) {
                auditoriaRepo.save(crearAuditoria("LOGIN", u1, "Inicio de sesión exitoso de bryan"));
                auditoriaRepo.save(crearAuditoria("CONSULTA_PERFIL", u2, "Consulta de perfil de cliente"));
                auditoriaRepo.save(crearAuditoria("CIERRE_CUENTA", u3, "Cierre exitoso de la cuenta 00030001"));
                auditoriaRepo.save(crearAuditoria("INTENTO_CIERRE", u4, "Intento de cierre rechazado por saldo pendiente"));
                auditoriaRepo.save(crearAuditoria("BLOQUEO_USUARIO", u5, "Usuario bloqueado para pruebas de seguridad"));
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
