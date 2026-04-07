package fe.banco_digital.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private final UsuarioDetallesService usuarioDetallesService;
    private final JwtUtil jwtUtil;

    public ConfiguracionSeguridad(UsuarioDetallesService usuarioDetallesService, JwtUtil jwtUtil) {
        this.usuarioDetallesService = usuarioDetallesService;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public PasswordEncoder codificadorPassword() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider proveedorAutenticacion() {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(usuarioDetallesService);
        proveedor.setPasswordEncoder(codificadorPassword());
        return proveedor;
    }

    @Bean
    public AuthenticationManager gestorAutenticacion(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // El filtro se instancia aquí como @Bean en lugar de usar @Component en FiltroJwt.
    // Así Spring Boot NO lo registra como filtro servlet genérico y solo vive
    // dentro de la cadena de seguridad → se ejecuta una única vez por request.
    @Bean
    public FiltroJwt filtroJwt() {
        return new FiltroJwt(jwtUtil, usuarioDetallesService);
    }

    @Bean
    public SecurityFilterChain cadenaFiltros(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",      // <-- agrega esto
                                "/swagger-ui/index.html", // <-- y esto
                                "/v3/api-docs/**",
                                "/api/db/ping",
                                "/favicon.ico"           // <-- y esto para el favicon
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS   )
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"mensaje\":\"No autenticado. Incluye un token Bearer válido.\"}");
                        })
                )
                .authenticationProvider(proveedorAutenticacion())
                .addFilterBefore(filtroJwt(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
