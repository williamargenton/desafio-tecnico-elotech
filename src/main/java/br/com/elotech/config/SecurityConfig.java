package br.com.elotech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.elotech.config.security.AuthFilter;
import br.com.elotech.config.security.TratadorErroSeguranca;
import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.repository.UsuarioRepository;

@Configuration
public class SecurityConfig {

    @Bean
    UserDetailsService detalhesUsuarioService(UsuarioRepository usuarioRepository) {
        return email -> usuarioRepository.findByEmailIgnoreCase(email)
            .map(UsuarioAutenticado::de)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @Bean
    PasswordEncoder codificadorSenha() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider provedorAutenticacao(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provedor = new DaoAuthenticationProvider(userDetailsService);
        provedor.setPasswordEncoder(passwordEncoder);
        return provedor;
    }

    @Bean
    AuthenticationManager gerenciadorAutenticacao(
        AuthenticationConfiguration configuracaoAutenticacao
    ) throws Exception {
        return configuracaoAutenticacao.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain cadeiaFiltrosSeguranca(
        HttpSecurity http,
        AuthFilter filtroAutenticacaoJwt,
        TratadorErroSeguranca tratadorErroSeguranca
    ) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(excecoes -> excecoes
                .authenticationEntryPoint(tratadorErroSeguranca)
                .accessDeniedHandler(tratadorErroSeguranca)
            )
            .authorizeHttpRequests(autorizacao -> autorizacao
                .requestMatchers(
                    "/autenticacao/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/h2-console/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(filtroAutenticacaoJwt, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}