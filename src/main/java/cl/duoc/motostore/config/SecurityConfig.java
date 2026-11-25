package cl.duoc.motostore.config;

import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${MOTORSTORE_JWT_SECRET}") String secret) {

        byte[] clave = Base64.getDecoder().decode(secret);

        if (clave.length < 32) {
            throw new IllegalArgumentException(
                    "La clave JWT debe tener al menos 32 bytes"
            );
        }

        return new SecretKeySpec(clave, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {

        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        "motorstore-api"
                )
        );

        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(
                    corsConfigurationSource()
            ))

            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )

            .authorizeHttpRequests(auth -> auth

                .dispatcherTypeMatchers(DispatcherType.ERROR)
                .permitAll()

                // Documentacion Swagger / OpenAPI
                .requestMatchers(
                        HttpMethod.GET,
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()

                // Registro e inicio de sesion
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/auth/registro",
                        "/api/v1/auth/login",
                        "/api/v1/auth/google"
                ).permitAll()

                // Catalogo publico
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/productos",
                        "/api/v1/productos/**",
                        "/api/v1/categorias",
                        "/api/v1/categorias/**"
                ).permitAll()

                // Formulario de contacto publico
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/contacto"
                ).permitAll()

                // Consultar sesion autenticada
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/auth/me"
                ).hasAnyRole(
                        "ADMIN", "VENDEDOR", "CLIENTE"
                )

                // Crear pedidos: clientes y administradores
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/pedidos"
                ).hasAnyRole("CLIENTE", "ADMIN")

                // Historial y detalle de pedidos propios
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/pedidos/mis-pedidos",
                        "/api/v1/pedidos/mis-pedidos/**"
                ).hasRole("CLIENTE")

                // Consultar pedidos: administrador y vendedor
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/pedidos",
                        "/api/v1/pedidos/*"
                ).hasAnyRole("ADMIN", "VENDEDOR")

                // Confirmar pagos y cancelar pedidos
                // Solamente administrador
                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/v1/pedidos/*/estado"
                ).hasRole("ADMIN")

                // Consultar mensajes de contacto: administrador y vendedor
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/contacto",
                        "/api/v1/contacto/*"
                ).hasAnyRole("ADMIN", "VENDEDOR")

                // Eliminar mensajes de contacto: solamente administrador
                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/v1/contacto/*"
                ).hasRole("ADMIN")

                // Administracion de usuarios: solamente administrador
                .requestMatchers(
                        "/api/v1/usuarios",
                        "/api/v1/usuarios/**"
                ).hasRole("ADMIN")

                // Administracion de productos y categorias
                .requestMatchers(
                        "/api/v1/productos",
                        "/api/v1/productos/**",
                        "/api/v1/categorias",
                        "/api/v1/categorias/**"
                ).hasRole("ADMIN")

                // Bloquear cualquier endpoint no autorizado
                .anyRequest().denyAll()
            )

            .oauth2ResourceServer(oauth2 ->
                    oauth2.jwt(jwt ->
                            jwt.jwtAuthenticationConverter(
                                    jwtAuthenticationConverter()
                            )
                    )
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
