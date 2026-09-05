package br.com.elotech.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "projetos")
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 2000)
    private String descricao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dono_id", nullable = false)
    private Usuario dono;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "projeto_membros",
        joinColumns = @JoinColumn(name = "projeto_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> membros = new HashSet<>();

    protected Projeto() {
    }

    public Projeto(String nome, String descricao, Usuario dono) {
        this.nome = nome;
        this.descricao = descricao;
        this.dono = dono;
        this.membros.add(dono);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public Usuario getDono() {
        return dono;
    }

    public Set<Usuario> getMembros() {
        return membros;
    }

    public void atualizar(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public boolean adicionarMembro(Usuario usuario) {
        return membros.add(usuario);
    }

    public boolean removerMembro(Usuario usuario) {
        return membros.remove(usuario);
    }

    public boolean possuiMembro(Long usuarioId) {
        return membros.stream().anyMatch(usuario -> usuario.getId().equals(usuarioId));
    }
}