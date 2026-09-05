package br.com.elotech.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.elotech.entity.Usuario;
import br.com.elotech.entity.enums.Perfil;
import br.com.elotech.repository.UsuarioRepository;

@Configuration
@ConditionalOnProperty(
    name = "app.dados-demonstracao.habilitados",
    havingValue = "true"
)
public class DadosDemonstracaoConfig {

    @Bean
    CommandLineRunner usuariosDemonstracao(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder
    ) {
        return argumentos -> {
            criarSeAusente(
                usuarioRepository,
                passwordEncoder,
                "Administrador",
                "admin@elotech.com",
                "admin123",
                Perfil.ADMIN
            );
            criarSeAusente(
                usuarioRepository,
                passwordEncoder,
                "Administrador 1",
                "admin1@elotech.com",
                "admin1231",
                Perfil.ADMIN
            );
            criarSeAusente(
                usuarioRepository,
                passwordEncoder,
                "Membro",
                "member@elotech.com",
                "member123",
                Perfil.MEMBER
            );
            criarSeAusente(
                usuarioRepository,
                passwordEncoder,
                "Membro 1",
                "member1@elotech.com",
                "member1231",
                Perfil.MEMBER
            );
        };
    }

    private void criarSeAusente(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        String nome,
        String email,
        String senha,
        Perfil perfil
    ) {
        if (usuarioRepository.findByEmailIgnoreCase(email).isEmpty()) {
            usuarioRepository.save(
                new Usuario(nome, email, passwordEncoder.encode(senha), perfil)
            );
        }
    }
}
