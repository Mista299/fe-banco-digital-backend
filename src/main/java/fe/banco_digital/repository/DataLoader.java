package fe.banco_digital.repository;

import fe.banco_digital.entity.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            MovimientoRepository movimientoRepo,
            TransferenciaRepository transferenciaRepo,
            TransferenciaExternaRepository transferenciaExternaRepo,
            TransferenciaInternacionalRepository transferenciaInternacionalRepo,
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
                c.setGenero(Genero.MASCULINO);
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
                c.setGenero(Genero.FEMENINO);
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
                c.setGenero(Genero.MASCULINO);
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
                c.setGenero(Genero.FEMENINO);
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
                c.setGenero(Genero.MASCULINO);
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
                c.setGenero(Genero.FEMENINO);
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

            Usuario u6 = usuarioRepo.findByUsername("sofia").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setUsername("sofia");
                u.setPasswordHash(passwordEncoder.encode("sofia123"));
                u.setEstado(EstadoUsuario.ACTIVO);
                u.setCliente(c6);
                u.setRoles(Set.of(rolCliente));
                return usuarioRepo.save(u);
            });

            // Saldos calculados según el historial semilla:
            // cta1: depósito 1.000.000 − transferencia 50.000 − swift 420.000 = 530.000
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
                cta.setSaldo(new BigDecimal("530000.00"));
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

            if (movimientoRepo.count() == 0 && transferenciaRepo.count() == 0) {
                LocalDateTime hace7d = LocalDateTime.now().minusDays(7);
                LocalDateTime hace6d = LocalDateTime.now().minusDays(6);
                LocalDateTime hace5d = LocalDateTime.now().minusDays(5);
                LocalDateTime hace4d = LocalDateTime.now().minusDays(4);
                LocalDateTime hace3d = LocalDateTime.now().minusDays(3);
                LocalDateTime hace2d = LocalDateTime.now().minusDays(2);
                LocalDateTime hace1d = LocalDateTime.now().minusDays(1);

                // cta1 (Bryan): depósito 1.000.000
                Movimiento m1 = new Movimiento();
                m1.setCuenta(cta1);
                m1.setTipo(TipoMovimiento.DEPOSITO);
                m1.setEstado(EstadoMovimiento.EXITOSO);
                m1.setMonto(new BigDecimal("1000000.00"));
                m1.setFecha(hace7d);
                movimientoRepo.save(m1);

                // cta2 (Ana): depósito 1.500.000
                Movimiento m2 = new Movimiento();
                m2.setCuenta(cta2);
                m2.setTipo(TipoMovimiento.DEPOSITO);
                m2.setEstado(EstadoMovimiento.EXITOSO);
                m2.setMonto(new BigDecimal("1500000.00"));
                m2.setFecha(hace7d.plusHours(2));
                movimientoRepo.save(m2);

                // cta6 (Sofía): depósito inicial 500.000
                Movimiento m3 = new Movimiento();
                m3.setCuenta(cta6);
                m3.setTipo(TipoMovimiento.DEPOSITO);
                m3.setEstado(EstadoMovimiento.EXITOSO);
                m3.setMonto(new BigDecimal("500000.00"));
                m3.setFecha(hace6d);
                movimientoRepo.save(m3);

                // cta1 → cta4: transferencia 50.000
                Transferencia t4 = new Transferencia();
                t4.setCuentaOrigen(cta1);
                t4.setCuentaDestino(cta4);
                t4.setEstado(EstadoTransferencia.EXITOSA);
                t4.setMonto(new BigDecimal("50000.00"));
                t4.setFecha(hace5d);
                transferenciaRepo.save(t4);

                // cta2: retiro 200.000
                Movimiento m5 = new Movimiento();
                m5.setCuenta(cta2);
                m5.setTipo(TipoMovimiento.RETIRO);
                m5.setEstado(EstadoMovimiento.EXITOSO);
                m5.setMonto(new BigDecimal("200000.00"));
                m5.setFecha(hace4d);
                movimientoRepo.save(m5);

                // cta4: depósito 100.000
                Movimiento m6 = new Movimiento();
                m6.setCuenta(cta4);
                m6.setTipo(TipoMovimiento.DEPOSITO);
                m6.setEstado(EstadoMovimiento.EXITOSO);
                m6.setMonto(new BigDecimal("100000.00"));
                m6.setFecha(hace3d);
                movimientoRepo.save(m6);

                // cta2 → Bancolombia: ACH exitosa 75.000
                TransferenciaExterna te7 = new TransferenciaExterna();
                te7.setCuentaOrigen(cta2);
                te7.setBancoDestino("Bancolombia");
                te7.setTipoCuentaDestino("AHORROS");
                te7.setNumeroCuentaDestino("45678901234");
                te7.setTipoDocumentoReceptor("CC");
                te7.setNumeroDocumentoReceptor("9876543210");
                te7.setNombreReceptor("Pedro Suárez");
                te7.setMonto(new BigDecimal("75000.00"));
                te7.setEstado(EstadoTransferenciaExterna.EXITOSA);
                te7.setReferenciaExterna("REF-2026-001");
                te7.setFecha(hace2d);
                transferenciaExternaRepo.save(te7);

                // cta6 → Davivienda: ACH reversada 200.000
                String refAchReversada = "REF-2026-002";
                String motivoRechazoAch = "Cuenta destino no existe en el banco receptor";

                TransferenciaExterna te8 = new TransferenciaExterna();
                te8.setCuentaOrigen(cta6);
                te8.setBancoDestino("Davivienda");
                te8.setTipoCuentaDestino("CORRIENTE");
                te8.setNumeroCuentaDestino("11223344556");
                te8.setTipoDocumentoReceptor("CC");
                te8.setNumeroDocumentoReceptor("1122334455");
                te8.setNombreReceptor("María López");
                te8.setMonto(new BigDecimal("200000.00"));
                te8.setEstado(EstadoTransferenciaExterna.REVERSADA);
                te8.setReferenciaExterna(refAchReversada);
                te8.setMotivoRechazo(motivoRechazoAch);
                te8.setFecha(hace1d);
                transferenciaExternaRepo.save(te8);

                // Devolución ACH a cta6
                Movimiento m9 = new Movimiento();
                m9.setCuenta(cta6);
                m9.setTipo(TipoMovimiento.DEPOSITO);
                m9.setEstado(EstadoMovimiento.EXITOSO);
                m9.setMonto(new BigDecimal("200000.00"));
                m9.setDescripcion("Reversión ACH: " + refAchReversada);
                m9.setFecha(hace1d.plusMinutes(5));
                movimientoRepo.save(m9);

                // cta1 (Bryan) → Citibank USA: SWIFT exitosa 100 USD a tasa 4200
                TransferenciaInternacional ti10 = new TransferenciaInternacional();
                ti10.setCuentaOrigen(cta1);
                ti10.setBancoDestino("Citibank");
                ti10.setCodigoSwift("CITIUS33");
                ti10.setPaisDestino("Estados Unidos");
                ti10.setTipoCuentaDestino("CHECKING");
                ti10.setIbanCuentaDestino("US64SVBKUS6S3300958879");
                ti10.setTipoDocumentoReceptor("PASSPORT");
                ti10.setNumeroDocumentoReceptor("A12345678");
                ti10.setNombreReceptor("John Smith");
                ti10.setMontoUsd(new BigDecimal("100.00"));
                ti10.setTasaCambio(new BigDecimal("4200.000000"));
                ti10.setMontoCop(new BigDecimal("420000.0000"));
                ti10.setMoneda("USD");
                ti10.setEstado(EstadoTransferenciaInternacional.EXITOSA);
                ti10.setReferenciaSwift("SWIFT-2026-001");
                ti10.setFecha(hace1d.plusHours(2));
                transferenciaInternacionalRepo.save(ti10);
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
