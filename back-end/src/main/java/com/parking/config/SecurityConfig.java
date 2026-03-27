package com.parking.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/test/hello").permitAll()
                .requestMatchers(HttpMethod.GET, "/espacios").hasAnyAuthority("ROLE_OPERADOR", "ROLE_operador", "ROLE_ADMIN", "ROLE_admin")
                .requestMatchers(HttpMethod.GET, "/espacios/inactivos").hasAnyAuthority("ROLE_ADMIN", "ROLE_admin")
                .requestMatchers(HttpMethod.PATCH, "/espacios/*/estado").hasAnyAuthority("ROLE_OPERADOR", "ROLE_operador", "ROLE_ADMIN", "ROLE_admin")
                .requestMatchers(HttpMethod.PATCH, "/espacios/*/activar").hasAnyAuthority("ROLE_ADMIN", "ROLE_admin")
                .requestMatchers(HttpMethod.POST, "/espacios/lote").hasAnyAuthority("ROLE_ADMIN", "ROLE_admin")
                .requestMatchers(HttpMethod.DELETE, "/espacios/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_admin")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Responde con 401 JSON cuando el request llega sin token (o con token inválido) a una ruta protegida
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                "No autorizado", "Se requiere autenticación para acceder a este recurso", request.getRequestURI());
    }

    // Responde con 403 JSON cuando el token es válido pero el rol no tiene permisos
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                "Prohibido", "No tienes permisos para acceder a este recurso", request.getRequestURI());
    }

    private void writeJson(HttpServletResponse response, int status, String error,
                           String message, String path) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String timestamp = LocalDateTime.now().toString();
        response.getWriter().write(
                "{\"status\":" + status + "," +
                "\"error\":\"" + error + "\"," +
                "\"message\":\"" + message + "\"," +
                "\"timestamp\":\"" + timestamp + "\"," +
                "\"path\":\"" + path + "\"}"
        );
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
