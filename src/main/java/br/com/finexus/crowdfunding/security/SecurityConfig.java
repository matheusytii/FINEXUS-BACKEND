package br.com.finexus.crowdfunding.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/autenticacao/cadastro").permitAll()
                .requestMatchers(HttpMethod.POST, "/autenticacao/entrar").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/propostas/**").hasRole("TOMADOR")
                .requestMatchers(HttpMethod.GET, "/propostas/**").hasRole("TOMADOR")
                .requestMatchers(HttpMethod.PUT, "/propostas/**").hasRole("TOMADOR")
                .requestMatchers(HttpMethod.DELETE, "/propostas/**").hasRole("TOMADOR")
                 .requestMatchers(HttpMethod.POST, "/formularios/**").hasRole("TOMADOR")
                .requestMatchers(HttpMethod.PUT, "/formularios/**").hasRole("TOMADOR")
                .requestMatchers(HttpMethod.GET, "/formularios/**").hasRole("TOMADOR")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
