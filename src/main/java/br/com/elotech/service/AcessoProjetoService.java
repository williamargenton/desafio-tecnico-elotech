package br.com.elotech.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.elotech.config.exception.ElotechException;
import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.entity.Projeto;
import br.com.elotech.entity.enums.Perfil;
import br.com.elotech.repository.ProjetoRepository;

@Service
public class AcessoProjetoService {

    private final ProjetoRepository projetoRepository;

    public AcessoProjetoService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    public Projeto buscarProjetoAcessivel(Long projetoId, UsuarioAutenticado usuarioAutenticado) {
        return projetoRepository.buscarAcessivelPorId(projetoId, usuarioAutenticado.id())
            .orElseThrow(() -> new ElotechException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
    }

    public Projeto buscarProjetoAdministrado(Long projetoId, UsuarioAutenticado usuarioAutenticado) {
        Projeto projeto = buscarProjetoAcessivel(projetoId, usuarioAutenticado);
        boolean administradorDono = usuarioAutenticado.perfil() == Perfil.ADMIN
            && projeto.getDono().getId().equals(usuarioAutenticado.id());

        if (!administradorDono) {
            throw new ElotechException(
                HttpStatus.FORBIDDEN,
                "Somente o ADMIN dono do projeto pode realizar esta operação"
            );
        }
        return projeto;
    }
}