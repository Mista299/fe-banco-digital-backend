package fe.banco_digital.repository;

import fe.banco_digital.entity.Auditoria;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.EstadoUsuario;
import fe.banco_digital.entity.Rol;
import fe.banco_digital.entity.RolNombre;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.entity.TipoTransaccion;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.entity.Usuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
            AuditoriaRepository auditoriaRepo
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

            Cliente c1 = clienteRepo.findByDocumento("123456789")
                    .orElseGet(() -> {
                        Cliente c = new Cliente();
                        c.setNombre("Bryan Molina");
                        c.setDocumento("123456789");
                        c.setEmail("bryan@example.com");
                        c.setTelefono("3000000000");
                        return clienteRepo.save(c);
                    });

            Usuario u1 = usuarioRepo.findByUsername("bryan")
                    .orElseGet(() -> {
                        Usuario u = new Usuario();
                        u.setUsername("bryan");
                        u.setPasswordHash("{noop}bryan123");
                        u.setEstado(EstadoUsuario.ACTIVO);
                        u.setCliente(c1);
                        u.setRoles(Set.of(rolCliente));
                        return usuarioRepo.save(u);
                    });

            Cuenta cta1 = cuentaRepo.findByNumeroCuenta("00010001")
                    .orElseGet(() -> {
                        Cuenta cta = new Cuenta();
                        cta.setNumeroCuenta("00010001");
                        cta.setTipo(TipoCuenta.AHORROS);
                        cta.setEstado(EstadoCuenta.ACTIVA);
                        cta.setSaldo(new BigDecimal("150000.00"));
                        cta.setCliente(c1);
                        return cuentaRepo.save(cta);
                    });

            Cuenta cta2 = cuentaRepo.findByNumeroCuenta("00010002")
                    .orElseGet(() -> {
                        Cuenta cta = new Cuenta();
                        cta.setNumeroCuenta("00010002");
                        cta.setTipo(TipoCuenta.CORRIENTE);
                        cta.setEstado(EstadoCuenta.ACTIVA);
                        cta.setSaldo(new BigDecimal("50000.00"));
                        cta.setCliente(c1);
                        return cuentaRepo.save(cta);
                    });

            Cliente c2 = clienteRepo.findByDocumento("987654321")
                    .orElseGet(() -> {
                        Cliente c = new Cliente();
                        c.setNombre("Ana Gómez");
                        c.setDocumento("987654321");
                        c.setEmail("ana@example.com");
                        c.setTelefono("3100000000");
                        return clienteRepo.save(c);
                    });

            Usuario u2 = usuarioRepo.findByUsername("ana")
                    .orElseGet(() -> {
                        Usuario u = new Usuario();
                        u.setUsername("ana");
                        u.setPasswordHash("{noop}ana123");
                        u.setEstado(EstadoUsuario.ACTIVO);
                        u.setCliente(c2);
                        u.setRoles(Set.of(rolAdmin));
                        return usuarioRepo.save(u);
                    });

            Cuenta cta3 = cuentaRepo.findByNumeroCuenta("00020001")
                    .orElseGet(() -> {
                        Cuenta cta = new Cuenta();
                        cta.setNumeroCuenta("00020001");
                        cta.setTipo(TipoCuenta.AHORROS);
                        cta.setEstado(EstadoCuenta.ACTIVA);
                        cta.setSaldo(new BigDecimal("250000.00"));
                        cta.setCliente(c2);
                        return cuentaRepo.save(cta);
                    });

            if (transaccionRepo.count() == 0) {
                Transaccion t1 = new Transaccion();
                t1.setTipo(TipoTransaccion.DEPOSITO);
                t1.setMonto(new BigDecimal("100000.00"));
                t1.setCuentaOrigen(cta1);
                t1.setCuentaDestino(cta1);
                transaccionRepo.save(t1);

                Transaccion t2 = new Transaccion();
                t2.setTipo(TipoTransaccion.TRANSFERENCIA);
                t2.setMonto(new BigDecimal("25000.00"));
                t2.setCuentaOrigen(cta1);
                t2.setCuentaDestino(cta3);
                transaccionRepo.save(t2);

                Transaccion t3 = new Transaccion();
                t3.setTipo(TipoTransaccion.RETIRO);
                t3.setMonto(new BigDecimal("10000.00"));
                t3.setCuentaOrigen(cta2);
                t3.setCuentaDestino(cta2);
                transaccionRepo.save(t3);
            }

            if (auditoriaRepo.count() == 0) {
                Auditoria a1 = new Auditoria();
                a1.setAccion("LOGIN");
                a1.setUsuario(u1);
                a1.setDetalle("Inicio de sesión exitoso");
                auditoriaRepo.save(a1);

                Auditoria a2 = new Auditoria();
                a2.setAccion("CONSULTA_SALDO");
                a2.setUsuario(u2);
                a2.setDetalle("Consulta de saldo de cuenta " + cta3.getNumeroCuenta());
                auditoriaRepo.save(a2);
            }
        };
    }
}
