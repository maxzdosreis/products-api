package com.maxzdosreis.products_api.config;

import com.maxzdosreis.products_api.security.jwt.JwtTokenFilter;
import com.maxzdosreis.products_api.security.jwt.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${cors.originPatterns:http://localhost:8080,http://localhost:3000}")
    private String corsOriginPatterns;

    @Bean
    PasswordEncoder passwordEncoder(){
        PasswordEncoder pbkdf2Encoder = new Pbkdf2PasswordEncoder(
                "", 8, 185000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("pbkdf2", pbkdf2Encoder);
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);

        passwordEncoder.setDefaultPasswordEncoderForMatches(pbkdf2Encoder);
        return passwordEncoder;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        // Converte a string de padrões em lista
        List<String> allowedOrigins = Arrays.asList(corsOriginPatterns.split(","));
        configuration.setAllowedOriginPatterns(allowedOrigins);

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Headers que podem ser expostos
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Permite credenciais
        configuration.setAllowCredentials(true);

        // Cache da configuração CORS por 1 hora
        configuration.setMaxAge(3600L);

        // Aplica a configuração para todos os endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        JwtTokenFilter filter = new JwtTokenFilter(tokenProvider);

        return http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(
                        authorizeHttpRequests -> authorizeHttpRequests
                                .requestMatchers(
                                        "/auth/signin",
                                        "/auth/signup",
                                        "/auth/refresh/**",
                                        "/auth/forgot-password",
                                        "/auth/reset-password",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                ).permitAll()
                                // Regra para endpoints de gerenciamento de usuários
                                .requestMatchers("/api/users/**").hasRole("ADMIN")

                                // Regras para endpoints de produtos
                                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("ADMIN", "MANAGER")
                                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("ADMIN", "MANAGER")
                                .requestMatchers(HttpMethod.PATCH, "/api/products/**").hasAnyRole("ADMIN", "MANAGER")
                                .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("ADMIN", "MANAGER", "USER")
                                .anyRequest().authenticated()
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .build();
    }
}