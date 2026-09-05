package br.com.elotech.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.elotech.entity.Tarefa;
import br.com.elotech.entity.enums.StatusTarefa;

public interface TarefaRepository extends JpaRepository<Tarefa, Long>, JpaSpecificationExecutor<Tarefa> {

    Optional<Tarefa> findByIdAndProjetoId(Long tarefaId, Long projetoId);

    long countByResponsavelIdAndStatus(Long responsavelId, StatusTarefa status);

    boolean existsByProjetoIdAndResponsavelId(Long projetoId, Long responsavelId);

    void deleteByProjetoId(Long projetoId);

    @Query("""
        select tarefa
        from Tarefa tarefa
        where tarefa.projeto.id = :projetoId
          and (
              lower(tarefa.titulo) like lower(concat('%', :texto, '%'))
              or lower(coalesce(tarefa.descricao, '')) like lower(concat('%', :texto, '%'))
          )
        """)
    List<Tarefa> buscarPorTexto(@Param("projetoId") Long projetoId, @Param("texto") String texto);

    @Query("""
        select tarefa.status, count(tarefa)
        from Tarefa tarefa
        where tarefa.projeto.id = :projetoId
        group by tarefa.status
        """)
    List<Object[]> contarPorStatus(@Param("projetoId") Long projetoId);

    @Query("""
        select tarefa.prioridade, count(tarefa)
        from Tarefa tarefa
        where tarefa.projeto.id = :projetoId
        group by tarefa.prioridade
        """)
    List<Object[]> contarPorPrioridade(@Param("projetoId") Long projetoId);
}