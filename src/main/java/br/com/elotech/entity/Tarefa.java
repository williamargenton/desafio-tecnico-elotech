package br.com.elotech.entity;

import java.time.Instant;
import java.time.LocalDate;

import br.com.elotech.entity.enums.PrioridadeTarefa;
import br.com.elotech.entity.enums.StatusTarefa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "tarefas",
    indexes = {
        @Index(name = "idx_tarefa_projeto_status", columnList = "projeto_id,status"),
        @Index(name = "idx_tarefa_responsavel_status", columnList = "responsavel_id,status")
    }
)
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 4000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTarefa status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadeTarefa prioridade;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(nullable = false)
    private Instant atualizadaEm;

    private LocalDate prazo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    protected Tarefa() {
    }

    public Tarefa(
        String titulo,
        String descricao,
        StatusTarefa status,
        PrioridadeTarefa prioridade,
        LocalDate prazo,
        Projeto projeto,
        Usuario responsavel
    ) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
        this.prioridade = prioridade;
        this.prazo = prazo;
        this.projeto = projeto;
        this.responsavel = responsavel;
    }

    @PrePersist
    void antesDePersistir() {
        criadaEm = Instant.now();
        atualizadaEm = criadaEm;
    }

    @PreUpdate
    void antesDeAtualizar() {
        atualizadaEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusTarefa getStatus() {
        return status;
    }

    public PrioridadeTarefa getPrioridade() {
        return prioridade;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Instant getAtualizadaEm() {
        return atualizadaEm;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public Projeto getProjeto() {
        return projeto;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public void atualizar(
        String titulo,
        String descricao,
        StatusTarefa status,
        PrioridadeTarefa prioridade,
        LocalDate prazo,
        Usuario responsavel
    ) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
        this.prioridade = prioridade;
        this.prazo = prazo;
        this.responsavel = responsavel;
    }
}