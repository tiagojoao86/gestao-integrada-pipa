package br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.valueobject.Email;
import br.com.grupopipa.gestaointegrada.core.valueobject.PhoneNumber;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade base para Pessoa - utiliza JOINED inheritance
 */
@Entity
@Table(name = "pessoa")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Pessoa extends BaseEntity {
    
    @Column(name = "nome", nullable = false, length = 200)
    private String nome;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email"))
    private Email email;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "telefone"))
    private PhoneNumber telefone;
    
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;
    
    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;
    
    protected Pessoa() {}
    
    public Pessoa(String nome, Email email, PhoneNumber telefone) {
        validarNome(nome);
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }
    
    private void validarNome(String nome) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        
        if (nome == null || nome.isBlank()) {
            violations.add(new BeanValidationMessage("nome", "Nome é obrigatório"));
        } else if (nome.length() > 200) {
            violations.add(new BeanValidationMessage("nome", "Nome deve ter no máximo 200 caracteres"));
        }
        
        if (!violations.isEmpty()) {
            throw new BeanValidationException("pessoa", violations);
        }
    }
    
    public void atualizar(String nome, Email email, PhoneNumber telefone) {
        validarNome(nome);
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }
    
    public void adicionarObservacao(String observacao) {
        if (this.observacoes == null) {
            this.observacoes = observacao;
        } else {
            this.observacoes += "\n" + observacao;
        }
    }
    
    public void ativar() {
        this.ativa = true;
    }
    
    public void inativar() {
        this.ativa = false;
    }
    
    // Getters
    public String getNome() {
        return nome;
    }
    
    public Email getEmail() {
        return email;
    }
    
    public PhoneNumber getTelefone() {
        return telefone;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public Boolean getAtiva() {
        return ativa;
    }
    
    public boolean isAtiva() {
        return ativa != null && ativa;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pessoa)) return false;
        if (!super.equals(o)) return false;
        Pessoa pessoa = (Pessoa) o;
        return Objects.equals(nome, pessoa.nome);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nome);
    }
}
