package br.com.elotech.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.elotech.entity.Projeto;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    @Query("""
        select distinct projeto
        from Projeto projeto
        left join fetch projeto.membros
        where exists (
            select 1
            from Projeto projetoAcessivel
            join projetoAcessivel.membros membro
            where projetoAcessivel = projeto and membro.id = :usuarioId
        )
        """)
    List<Projeto> buscarTodosAcessiveisPeloUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("""
        select distinct projeto
        from Projeto projeto
        left join fetch projeto.membros
        where projeto.id = :projetoId
          and exists (
              select 1
              from Projeto projetoAcessivel
              join projetoAcessivel.membros membro
              where projetoAcessivel = projeto and membro.id = :usuarioId
          )
        """)
    Optional<Projeto> buscarAcessivelPorId(
        @Param("projetoId") Long projetoId,
        @Param("usuarioId") Long usuarioId
    );
}