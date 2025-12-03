package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade para Unidade de Negócio (centro de custo/receita)
 */
@Entity
@Table(name = "unidade_negocio", uniqueConstraints = {
    @UniqueConstraint(name = "uk_unidade_negocio_codigo", columnNames = "codigo")
})
public class UnidadeNegocio extends BaseEntity {
    
    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;
    
    @Column(name = "nome", nullable = false, length = 200)
    private String nome;
    
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
    
    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;
    
    protected UnidadeNegocio() {}
    
    public UnidadeNegocio(String codigo, String nome) {
        validarCamposObrigatorios(codigo, nome);
        this.codigo = codigo;
        this.nome = nome;
    }
    
    public UnidadeNegocio(String codigo, String nome, String descricao) {
        this(codigo, nome);
        this.descricao = descricao;
    }
    
    private void validarCamposObrigatorios(String codigo, String nome) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        
        if (codigo == null || codigo.isBlank()) {
            violations.add(new BeanValidationMessage("codigo", "Código é obrigatório"));
        } else if (codigo.length() > 20) {
            violations.add(new BeanValidationMessage("codigo", "Código deve ter no máximo 20 caracteres"));
        }
        
        if (nome == null || nome.isBlank()) {
            violations.add(new BeanValidationMessage("nome", "Nome é obrigatório"));
        } else if (nome.length() > 200) {
            violations.add(new BeanValidationMessage("nome", "Nome deve ter no máximo 200 caracteres"));
        }
        
        if (!violations.isEmpty()) {
            throw new BeanValidationException("unidadeNegocio", violations);
        }
    }
    
    public void atualizar(String nome, String descricao) {
        if (nome != null && !nome.isBlank()) {
            validarCamposObrigatorios(this.codigo, nome);
            this.nome = nome;
        }
        this.descricao = descricao;
    }
    
    public void ativar() {
        this.ativa = true;
    }
    
    public void inativar() {
        this.ativa = false;
    }
    
    // Getters
    public String getCodigo() {
        return codigo;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getDescricao() {
        return descricao;
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
        if (!(o instanceof UnidadeNegocio)) return false;
        if (!super.equals(o)) return false;
        UnidadeNegocio that = (UnidadeNegocio) o;
        return Objects.equals(codigo, that.codigo);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), codigo);
    }
    
    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}
