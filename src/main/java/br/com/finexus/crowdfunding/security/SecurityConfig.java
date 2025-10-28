
package br.com.finexus.crowdfunding.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Rotas abertas
                .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()       
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()   
                .requestMatchers(HttpMethod.GET, "/usuarios/**").permitAll()  
                // Swagger
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Outras rotas precisam de autenticação
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
