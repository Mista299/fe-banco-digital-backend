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

            // ── Roles ──────────────────────────────────────────────────────────────
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

            // ── Clientes ───────────────────────────────────────────────────────────
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

            // ── Usuarios ───────────────────────────────────────────────────────────
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

            // ── Cuentas ────────────────────────────────────────────────────────────
            // Saldos calculados a partir del historial semilla completo (abril + mayo):
            //
            // cta1  Bryan  AHORROS   00010001 : ABR[+200k de Ana, −150k a Ana] MAY[+1M dep, +100k Carlos, −50k Laura, −80k ACH, −420k SWIFT] = 600.000
            // cta2  Ana    AHORROS   00020001 : ABR[+1.5M dep, −350k ret, −200k Ana→Bryan, +150k Bryan→Ana, −90k ret, −120k ACH, +80k dep] MAY[+1.5M dep, −200k ret, −300k Carlos, −75k ACH, +20k Laura, −200k SWIFT] = 1.715.000
            // cta3  Carlos AHORROS   00030001 : +800.000 dep  −150.000 ret  −100.000 transf(Bryan)  +300.000 (de Ana)  −50.000 ret  = 800.000
            // cta4  Laura  CORRIENTE 00040001 : +50.000 (de Bryan)  +100.000 dep  −30.000 ret  −20.000 transf(Ana)  = 100.000
            // cta5  Jorge  AHORROS   00050001 : sin movimientos, INACTIVA = 0
            // cta6  Sofía  AHORROS   00060001 : +500.000 dep  +120.000 (de Carlos)  −200.000 ACH(reversada)  +200.000 reverso  = 620.000
            // cta7  Bryan  CORRIENTE 00010002 : +2.000.000 dep  −300.000 ret  −150.000 ACH  −200.000 transf(Carlos)  = 1.350.000
            // cta8  Carlos CORRIENTE 00030002 : +600.000 dep  −80.000 ret  −120.000 transf(Sofía)  −60.000 ACH  +200.000 (de Bryan)  = 540.000

            Cuenta cta1 = cuentaRepo.findByNumeroCuenta("00010001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00010001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("600000.00"));
                cta.setCliente(c1);
                return cuentaRepo.save(cta);
            });

            Cuenta cta2 = cuentaRepo.findByNumeroCuenta("00020001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00020001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("1715000.00"));
                cta.setCliente(c2);
                return cuentaRepo.save(cta);
            });

            Cuenta cta3 = cuentaRepo.findByNumeroCuenta("00030001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00030001");
                cta.setTipo(TipoCuenta.AHORROS);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("800000.00"));
                cta.setCliente(c3);
                return cuentaRepo.save(cta);
            });

            Cuenta cta4 = cuentaRepo.findByNumeroCuenta("00040001").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00040001");
                cta.setTipo(TipoCuenta.CORRIENTE);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("100000.00"));
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
                cta.setSaldo(new BigDecimal("620000.00"));
                cta.setCliente(c6);
                return cuentaRepo.save(cta);
            });

            Cuenta cta7 = cuentaRepo.findByNumeroCuenta("00010002").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00010002");
                cta.setTipo(TipoCuenta.CORRIENTE);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("1350000.00"));
                cta.setCliente(c1);
                return cuentaRepo.save(cta);
            });

            Cuenta cta8 = cuentaRepo.findByNumeroCuenta("00030002").orElseGet(() -> {
                Cuenta cta = new Cuenta();
                cta.setNumeroCuenta("00030002");
                cta.setTipo(TipoCuenta.CORRIENTE);
                cta.setEstado(EstadoCuenta.ACTIVA);
                cta.setSaldo(new BigDecimal("540000.00"));
                cta.setCliente(c3);
                return cuentaRepo.save(cta);
            });

            // ── Transacciones ──────────────────────────────────────────────────────
            if (movimientoRepo.count() == 0 && transferenciaRepo.count() == 0) {

                LocalDateTime hace12d = LocalDateTime.now().minusDays(12);
                LocalDateTime hace10d = LocalDateTime.now().minusDays(10);
                LocalDateTime hace9d  = LocalDateTime.now().minusDays(9);
                LocalDateTime hace8d  = LocalDateTime.now().minusDays(8);
                LocalDateTime hace7d  = LocalDateTime.now().minusDays(7);
                LocalDateTime hace6d  = LocalDateTime.now().minusDays(6);
                LocalDateTime hace5d  = LocalDateTime.now().minusDays(5);
                LocalDateTime hace4d  = LocalDateTime.now().minusDays(4);
                LocalDateTime hace3d  = LocalDateTime.now().minusDays(3);
                LocalDateTime hace2d  = LocalDateTime.now().minusDays(2);
                LocalDateTime hace1d  = LocalDateTime.now().minusDays(1);

                // ── Bryan cta7 (CORRIENTE) ─────────────────────────────────────────
                // Depósito inicial en cuenta corriente
                Movimiento m_b7_dep = new Movimiento();
                m_b7_dep.setCuenta(cta7);
                m_b7_dep.setTipo(TipoMovimiento.DEPOSITO);
                m_b7_dep.setEstado(EstadoMovimiento.EXITOSO);
                m_b7_dep.setMonto(new BigDecimal("2000000.00"));
                m_b7_dep.setFecha(hace12d);
                movimientoRepo.save(m_b7_dep);

                // Retiro
                Movimiento m_b7_ret = new Movimiento();
                m_b7_ret.setCuenta(cta7);
                m_b7_ret.setTipo(TipoMovimiento.RETIRO);
                m_b7_ret.setEstado(EstadoMovimiento.EXITOSO);
                m_b7_ret.setMonto(new BigDecimal("300000.00"));
                m_b7_ret.setFecha(hace8d);
                movimientoRepo.save(m_b7_ret);

                // ACH cta7 → Bancolombia (exitosa) 150.000
                TransferenciaExterna te_b7_ach = new TransferenciaExterna();
                te_b7_ach.setCuentaOrigen(cta7);
                te_b7_ach.setBancoDestino("Bancolombia");
                te_b7_ach.setTipoCuentaDestino("AHORROS");
                te_b7_ach.setNumeroCuentaDestino("30012345678");
                te_b7_ach.setTipoDocumentoReceptor("CC");
                te_b7_ach.setNumeroDocumentoReceptor("1098765432");
                te_b7_ach.setNombreReceptor("Luis Fernández");
                te_b7_ach.setMonto(new BigDecimal("150000.00"));
                te_b7_ach.setEstado(EstadoTransferenciaExterna.EXITOSA);
                te_b7_ach.setReferenciaExterna("REF-2026-003");
                te_b7_ach.setFecha(hace5d.plusHours(1));
                transferenciaExternaRepo.save(te_b7_ach);

                // cta7 → cta8: Bryan transfiere a Carlos (CORRIENTE) 200.000
                Transferencia t_b7_c8 = new Transferencia();
                t_b7_c8.setCuentaOrigen(cta7);
                t_b7_c8.setCuentaDestino(cta8);
                t_b7_c8.setEstado(EstadoTransferencia.EXITOSA);
                t_b7_c8.setMonto(new BigDecimal("200000.00"));
                t_b7_c8.setFecha(hace2d.plusHours(3));
                transferenciaRepo.save(t_b7_c8);

                // ── Bryan cta1 (AHORROS) ───────────────────────────────────────────
                // Depósito inicial
                Movimiento m1 = new Movimiento();
                m1.setCuenta(cta1);
                m1.setTipo(TipoMovimiento.DEPOSITO);
                m1.setEstado(EstadoMovimiento.EXITOSO);
                m1.setMonto(new BigDecimal("1000000.00"));
                m1.setFecha(hace7d);
                movimientoRepo.save(m1);

                // Recibe transferencia de Carlos cta3
                // (se registra como el destino de t_c3_b1 — la Transferencia ya lo refleja)

                // cta1 → cta4: Bryan transfiere a Laura 50.000
                Transferencia t4 = new Transferencia();
                t4.setCuentaOrigen(cta1);
                t4.setCuentaDestino(cta4);
                t4.setEstado(EstadoTransferencia.EXITOSA);
                t4.setMonto(new BigDecimal("50000.00"));
                t4.setFecha(hace5d);
                transferenciaRepo.save(t4);

                // ACH cta1 → Nequi (exitosa) 80.000
                TransferenciaExterna te_b1_nequi = new TransferenciaExterna();
                te_b1_nequi.setCuentaOrigen(cta1);
                te_b1_nequi.setBancoDestino("Nequi");
                te_b1_nequi.setTipoCuentaDestino("AHORROS");
                te_b1_nequi.setNumeroCuentaDestino("3100000001");
                te_b1_nequi.setTipoDocumentoReceptor("CC");
                te_b1_nequi.setNumeroDocumentoReceptor("1000111222");
                te_b1_nequi.setNombreReceptor("Valentina Ríos");
                te_b1_nequi.setMonto(new BigDecimal("80000.00"));
                te_b1_nequi.setEstado(EstadoTransferenciaExterna.EXITOSA);
                te_b1_nequi.setReferenciaExterna("REF-2026-004");
                te_b1_nequi.setFecha(hace4d.plusHours(2));
                transferenciaExternaRepo.save(te_b1_nequi);

                // SWIFT cta1 → Citibank USA 100 USD (exitosa)
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

                // ── Ana cta2 (AHORROS) ─────────────────────────────────────────────
                // Depósito inicial
                Movimiento m2 = new Movimiento();
                m2.setCuenta(cta2);
                m2.setTipo(TipoMovimiento.DEPOSITO);
                m2.setEstado(EstadoMovimiento.EXITOSO);
                m2.setMonto(new BigDecimal("1500000.00"));
                m2.setFecha(hace7d.plusHours(2));
                movimientoRepo.save(m2);

                // Retiro
                Movimiento m5 = new Movimiento();
                m5.setCuenta(cta2);
                m5.setTipo(TipoMovimiento.RETIRO);
                m5.setEstado(EstadoMovimiento.EXITOSO);
                m5.setMonto(new BigDecimal("200000.00"));
                m5.setFecha(hace4d);
                movimientoRepo.save(m5);

                // cta2 → cta3: Ana transfiere a Carlos (AHORROS) 300.000
                Transferencia t_a2_c3 = new Transferencia();
                t_a2_c3.setCuentaOrigen(cta2);
                t_a2_c3.setCuentaDestino(cta3);
                t_a2_c3.setEstado(EstadoTransferencia.EXITOSA);
                t_a2_c3.setMonto(new BigDecimal("300000.00"));
                t_a2_c3.setFecha(hace4d.plusHours(1));
                transferenciaRepo.save(t_a2_c3);

                // ACH cta2 → Bancolombia (exitosa) 75.000
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

                // Recibe de Laura cta4 → cta2: 20.000
                // (se refleja en la Transferencia t_l4_a2 más abajo)

                // SWIFT cta2 → Santander España 50 USD (exitosa)
                TransferenciaInternacional ti_a2_swift = new TransferenciaInternacional();
                ti_a2_swift.setCuentaOrigen(cta2);
                ti_a2_swift.setBancoDestino("Santander");
                ti_a2_swift.setCodigoSwift("BSCHESMMXXX");
                ti_a2_swift.setPaisDestino("España");
                ti_a2_swift.setTipoCuentaDestino("CUENTA_CORRIENTE");
                ti_a2_swift.setIbanCuentaDestino("ES9121000418450200051332");
                ti_a2_swift.setTipoDocumentoReceptor("PASSPORT");
                ti_a2_swift.setNumeroDocumentoReceptor("XAB123456");
                ti_a2_swift.setNombreReceptor("Elena Castillo");
                ti_a2_swift.setMontoUsd(new BigDecimal("50.00"));
                ti_a2_swift.setTasaCambio(new BigDecimal("4000.000000"));
                ti_a2_swift.setMontoCop(new BigDecimal("200000.0000"));
                ti_a2_swift.setMoneda("USD");
                ti_a2_swift.setEstado(EstadoTransferenciaInternacional.EXITOSA);
                ti_a2_swift.setReferenciaSwift("SWIFT-2026-002");
                ti_a2_swift.setFecha(hace2d.plusHours(4));
                transferenciaInternacionalRepo.save(ti_a2_swift);

                // ── Carlos cta3 (AHORROS) ──────────────────────────────────────────
                // Depósito inicial
                Movimiento m_c3_dep1 = new Movimiento();
                m_c3_dep1.setCuenta(cta3);
                m_c3_dep1.setTipo(TipoMovimiento.DEPOSITO);
                m_c3_dep1.setEstado(EstadoMovimiento.EXITOSO);
                m_c3_dep1.setMonto(new BigDecimal("800000.00"));
                m_c3_dep1.setFecha(hace10d);
                movimientoRepo.save(m_c3_dep1);

                // Retiro
                Movimiento m_c3_ret1 = new Movimiento();
                m_c3_ret1.setCuenta(cta3);
                m_c3_ret1.setTipo(TipoMovimiento.RETIRO);
                m_c3_ret1.setEstado(EstadoMovimiento.EXITOSO);
                m_c3_ret1.setMonto(new BigDecimal("150000.00"));
                m_c3_ret1.setFecha(hace8d.plusHours(1));
                movimientoRepo.save(m_c3_ret1);

                // cta3 → cta1: Carlos transfiere a Bryan (AHORROS) 100.000
                Transferencia t_c3_b1 = new Transferencia();
                t_c3_b1.setCuentaOrigen(cta3);
                t_c3_b1.setCuentaDestino(cta1);
                t_c3_b1.setEstado(EstadoTransferencia.EXITOSA);
                t_c3_b1.setMonto(new BigDecimal("100000.00"));
                t_c3_b1.setFecha(hace6d.plusHours(1));
                transferenciaRepo.save(t_c3_b1);

                // Recibe de Ana cta2 → cta3: 300.000 (ya registrado en t_a2_c3)

                // Retiro final
                Movimiento m_c3_ret2 = new Movimiento();
                m_c3_ret2.setCuenta(cta3);
                m_c3_ret2.setTipo(TipoMovimiento.RETIRO);
                m_c3_ret2.setEstado(EstadoMovimiento.EXITOSO);
                m_c3_ret2.setMonto(new BigDecimal("50000.00"));
                m_c3_ret2.setFecha(hace2d.plusHours(1));
                movimientoRepo.save(m_c3_ret2);

                // ── Carlos cta8 (CORRIENTE) ────────────────────────────────────────
                // Depósito inicial
                Movimiento m_c8_dep = new Movimiento();
                m_c8_dep.setCuenta(cta8);
                m_c8_dep.setTipo(TipoMovimiento.DEPOSITO);
                m_c8_dep.setEstado(EstadoMovimiento.EXITOSO);
                m_c8_dep.setMonto(new BigDecimal("600000.00"));
                m_c8_dep.setFecha(hace9d);
                movimientoRepo.save(m_c8_dep);

                // Retiro
                Movimiento m_c8_ret = new Movimiento();
                m_c8_ret.setCuenta(cta8);
                m_c8_ret.setTipo(TipoMovimiento.RETIRO);
                m_c8_ret.setEstado(EstadoMovimiento.EXITOSO);
                m_c8_ret.setMonto(new BigDecimal("80000.00"));
                m_c8_ret.setFecha(hace7d.plusHours(3));
                movimientoRepo.save(m_c8_ret);

                // cta8 → cta6: Carlos transfiere a Sofía 120.000
                Transferencia t_c8_s6 = new Transferencia();
                t_c8_s6.setCuentaOrigen(cta8);
                t_c8_s6.setCuentaDestino(cta6);
                t_c8_s6.setEstado(EstadoTransferencia.EXITOSA);
                t_c8_s6.setMonto(new BigDecimal("120000.00"));
                t_c8_s6.setFecha(hace5d.plusHours(2));
                transferenciaRepo.save(t_c8_s6);

                // ACH cta8 → BBVA (exitosa) 60.000
                TransferenciaExterna te_c8_bbva = new TransferenciaExterna();
                te_c8_bbva.setCuentaOrigen(cta8);
                te_c8_bbva.setBancoDestino("BBVA");
                te_c8_bbva.setTipoCuentaDestino("CORRIENTE");
                te_c8_bbva.setNumeroCuentaDestino("55566677788");
                te_c8_bbva.setTipoDocumentoReceptor("CC");
                te_c8_bbva.setNumeroDocumentoReceptor("3003003003");
                te_c8_bbva.setNombreReceptor("Receptor BBVA");
                te_c8_bbva.setMonto(new BigDecimal("60000.00"));
                te_c8_bbva.setEstado(EstadoTransferenciaExterna.EXITOSA);
                te_c8_bbva.setReferenciaExterna("REF-2026-005");
                te_c8_bbva.setFecha(hace3d.plusHours(1));
                transferenciaExternaRepo.save(te_c8_bbva);

                // Recibe de Bryan cta7 → cta8: 200.000 (ya registrado en t_b7_c8)

                // ── Laura cta4 (CORRIENTE) ─────────────────────────────────────────
                // Depósito
                Movimiento m6 = new Movimiento();
                m6.setCuenta(cta4);
                m6.setTipo(TipoMovimiento.DEPOSITO);
                m6.setEstado(EstadoMovimiento.EXITOSO);
                m6.setMonto(new BigDecimal("100000.00"));
                m6.setFecha(hace3d);
                movimientoRepo.save(m6);

                // Retiro
                Movimiento m_l4_ret = new Movimiento();
                m_l4_ret.setCuenta(cta4);
                m_l4_ret.setTipo(TipoMovimiento.RETIRO);
                m_l4_ret.setEstado(EstadoMovimiento.EXITOSO);
                m_l4_ret.setMonto(new BigDecimal("30000.00"));
                m_l4_ret.setFecha(hace3d.plusHours(2));
                movimientoRepo.save(m_l4_ret);

                // cta4 → cta2: Laura transfiere a Ana 20.000
                Transferencia t_l4_a2 = new Transferencia();
                t_l4_a2.setCuentaOrigen(cta4);
                t_l4_a2.setCuentaDestino(cta2);
                t_l4_a2.setEstado(EstadoTransferencia.EXITOSA);
                t_l4_a2.setMonto(new BigDecimal("20000.00"));
                t_l4_a2.setFecha(hace2d.plusHours(2));
                transferenciaRepo.save(t_l4_a2);

                // ── Sofía cta6 (AHORROS) ──────────────────────────────────────────
                // Depósito inicial
                Movimiento m3 = new Movimiento();
                m3.setCuenta(cta6);
                m3.setTipo(TipoMovimiento.DEPOSITO);
                m3.setEstado(EstadoMovimiento.EXITOSO);
                m3.setMonto(new BigDecimal("500000.00"));
                m3.setFecha(hace6d);
                movimientoRepo.save(m3);

                // Recibe de Carlos cta8 → cta6: 120.000 (ya registrado en t_c8_s6)

                // ACH cta6 → Davivienda (reversada) 200.000
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

                // Reversión ACH → depósito de vuelta a cta6
                Movimiento m9 = new Movimiento();
                m9.setCuenta(cta6);
                m9.setTipo(TipoMovimiento.DEPOSITO);
                m9.setEstado(EstadoMovimiento.EXITOSO);
                m9.setMonto(new BigDecimal("200000.00"));
                m9.setDescripcion("Reversión ACH: " + refAchReversada);
                m9.setFecha(hace1d.plusMinutes(5));
                movimientoRepo.save(m9);

                // ── Ana cta2 — ABRIL 2026 (historial para extractos) ──────────────
                // Net abril cta2: +1.500k dep, −350k ret, −200k transf(Bryan), +150k (de Bryan), −90k ret, −120k ACH, +80k dep = +970k
                // Net abril cta1: +200k (de Ana), −150k (a Ana) = +50k

                LocalDateTime abr03 = LocalDateTime.of(2026, 4,  3,  9,  0);
                LocalDateTime abr09 = LocalDateTime.of(2026, 4,  9, 14, 30);
                LocalDateTime abr14 = LocalDateTime.of(2026, 4, 14, 11, 15);
                LocalDateTime abr18 = LocalDateTime.of(2026, 4, 18, 16,  0);
                LocalDateTime abr22 = LocalDateTime.of(2026, 4, 22, 10, 45);
                LocalDateTime abr25 = LocalDateTime.of(2026, 4, 25,  8, 20);
                LocalDateTime abr28 = LocalDateTime.of(2026, 4, 28, 17, 30);

                Movimiento a_dep1 = new Movimiento();
                a_dep1.setCuenta(cta2);
                a_dep1.setTipo(TipoMovimiento.DEPOSITO);
                a_dep1.setEstado(EstadoMovimiento.EXITOSO);
                a_dep1.setMonto(new BigDecimal("1500000.00"));
                a_dep1.setDescripcion("Consignación nómina abril");
                a_dep1.setFecha(abr03);
                movimientoRepo.save(a_dep1);

                Movimiento a_ret1 = new Movimiento();
                a_ret1.setCuenta(cta2);
                a_ret1.setTipo(TipoMovimiento.RETIRO);
                a_ret1.setEstado(EstadoMovimiento.EXITOSO);
                a_ret1.setMonto(new BigDecimal("350000.00"));
                a_ret1.setDescripcion("Retiro cajero");
                a_ret1.setFecha(abr09);
                movimientoRepo.save(a_ret1);

                // cta2 → cta1: Ana transfiere a Bryan 200.000
                Transferencia t_abr_a2_b1 = new Transferencia();
                t_abr_a2_b1.setCuentaOrigen(cta2);
                t_abr_a2_b1.setCuentaDestino(cta1);
                t_abr_a2_b1.setEstado(EstadoTransferencia.EXITOSA);
                t_abr_a2_b1.setMonto(new BigDecimal("200000.00"));
                t_abr_a2_b1.setFecha(abr14);
                transferenciaRepo.save(t_abr_a2_b1);

                // cta1 → cta2: Bryan transfiere a Ana 150.000
                Transferencia t_abr_b1_a2 = new Transferencia();
                t_abr_b1_a2.setCuentaOrigen(cta1);
                t_abr_b1_a2.setCuentaDestino(cta2);
                t_abr_b1_a2.setEstado(EstadoTransferencia.EXITOSA);
                t_abr_b1_a2.setMonto(new BigDecimal("150000.00"));
                t_abr_b1_a2.setFecha(abr18);
                transferenciaRepo.save(t_abr_b1_a2);

                Movimiento a_ret2 = new Movimiento();
                a_ret2.setCuenta(cta2);
                a_ret2.setTipo(TipoMovimiento.RETIRO);
                a_ret2.setEstado(EstadoMovimiento.EXITOSO);
                a_ret2.setMonto(new BigDecimal("90000.00"));
                a_ret2.setDescripcion("Retiro cajero");
                a_ret2.setFecha(abr22);
                movimientoRepo.save(a_ret2);

                // ACH cta2 → Nequi abril 120.000
                TransferenciaExterna te_abr_a2_nequi = new TransferenciaExterna();
                te_abr_a2_nequi.setCuentaOrigen(cta2);
                te_abr_a2_nequi.setBancoDestino("Nequi");
                te_abr_a2_nequi.setTipoCuentaDestino("AHORROS");
                te_abr_a2_nequi.setNumeroCuentaDestino("3200000099");
                te_abr_a2_nequi.setTipoDocumentoReceptor("CC");
                te_abr_a2_nequi.setNumeroDocumentoReceptor("1019283746");
                te_abr_a2_nequi.setNombreReceptor("Mario Ruiz");
                te_abr_a2_nequi.setMonto(new BigDecimal("120000.00"));
                te_abr_a2_nequi.setEstado(EstadoTransferenciaExterna.EXITOSA);
                te_abr_a2_nequi.setReferenciaExterna("REF-2026-ABR-001");
                te_abr_a2_nequi.setFecha(abr25);
                transferenciaExternaRepo.save(te_abr_a2_nequi);

                Movimiento a_dep2 = new Movimiento();
                a_dep2.setCuenta(cta2);
                a_dep2.setTipo(TipoMovimiento.DEPOSITO);
                a_dep2.setEstado(EstadoMovimiento.EXITOSO);
                a_dep2.setMonto(new BigDecimal("80000.00"));
                a_dep2.setDescripcion("Reembolso gastos");
                a_dep2.setFecha(abr28);
                movimientoRepo.save(a_dep2);
            }

            // ── Auditoría ──────────────────────────────────────────────────────────
            if (auditoriaRepo.count() == 0) {
                auditoriaRepo.save(crearAuditoria("LOGIN",                u1, "Inicio de sesión exitoso de bryan"));
                auditoriaRepo.save(crearAuditoria("CONSULTA_PERFIL",      u2, "Consulta de perfil de cliente: Ana Gómez"));
                auditoriaRepo.save(crearAuditoria("APERTURA_CUENTA",      u3, "Apertura de cuenta corriente 00030002 para Carlos Pérez"));
                auditoriaRepo.save(crearAuditoria("INTENTO_CIERRE",       u4, "Intento de cierre rechazado por saldo pendiente en cuenta 00040001"));
                auditoriaRepo.save(crearAuditoria("BLOQUEO_USUARIO",      u5, "Usuario bloqueado por múltiples intentos fallidos de autenticación"));
                auditoriaRepo.save(crearAuditoria("TRANSFERENCIA_ACH",    u6, "Transferencia interbancaria rechazada — cuenta destino inexistente en Davivienda"));
                auditoriaRepo.save(crearAuditoria("TRANSFERENCIA_SWIFT",  u1, "Transferencia internacional aprobada: 100 USD → Citibank CITIUS33"));
                auditoriaRepo.save(crearAuditoria("TRANSFERENCIA_INTERNA",u3, "Transferencia interna de carlos a bryan: $100.000 (cta3 → cta1)"));
                auditoriaRepo.save(crearAuditoria("TRANSFERENCIA_INTERNA",u2, "Transferencia interna de ana a carlos: $300.000 (cta2 → cta3)"));
                auditoriaRepo.save(crearAuditoria("LOGIN_FALLIDO",        u5, "Intento de login con usuario bloqueado: jorge"));
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
