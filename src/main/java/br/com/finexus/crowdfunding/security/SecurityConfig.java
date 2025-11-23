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
                        .requestMatchers("/usuarios/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/propostas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/propostas", "/propostas/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/propostas/**").permitAll()
                        .requestMatchers("/investimentos/**").permitAll()
                        .requestMatchers("/formularios/**").permitAll()
                        .requestMatchers("/saldos/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/usuarios/**").permitAll()                        
                        .requestMatchers("/parcelas/**").permitAll()                        
                        .requestMatchers("/dividas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/formularios/usuario/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}