package br.com.grupopipa.gestaointegrada.financeiro.entity;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade para Plano de Contas com estrutura hierárquica
 */
@Entity
@Table(name = "plano_contas", uniqueConstraints = {
    @UniqueConstraint(name = "uk_plano_contas_codigo", columnNames = "codigo")
})
public class PlanoContas extends BaseEntity {
    
    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;
    
    @Column(name = "descricao", nullable = false, length = 200)
    private String descricao;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoPlanoContas tipo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_pai_id", foreignKey = @ForeignKey(name = "fk_plano_contas_pai"))
    private PlanoContas planoPai;
    
    @OneToMany(mappedBy = "planoPai", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanoContas> planosFilhos = new ArrayList<>();
    
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;
    
    protected PlanoContas() {}
    
    public PlanoContas(String codigo, String descricao, TipoPlanoContas tipo) {
        validarCamposObrigatorios(codigo, descricao, tipo);
        this.codigo = codigo;
        this.descricao = descricao;
        this.tipo = tipo;
    }
    
    public PlanoContas(String codigo, String descricao, TipoPlanoContas tipo, PlanoContas planoPai) {
        this(codigo, descricao, tipo);
        definirPlanoPai(planoPai);
    }
    
    private void validarCamposObrigatorios(String codigo, String descricao, TipoPlanoContas tipo) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        
        if (codigo == null || codigo.isBlank()) {
            violations.add(new BeanValidationMessage("codigo", "Código é obrigatório"));
        } else if (codigo.length() > 20) {
            violations.add(new BeanValidationMessage("codigo", "Código deve ter no máximo 20 caracteres"));
        }
        
        if (descricao == null || descricao.isBlank()) {
            violations.add(new BeanValidationMessage("descricao", "Descrição é obrigatória"));
        } else if (descricao.length() > 200) {
            violations.add(new BeanValidationMessage("descricao", "Descrição deve ter no máximo 200 caracteres"));
        }
        
        if (tipo == null) {
            violations.add(new BeanValidationMessage("tipo", "Tipo é obrigatório"));
        }
        
        if (!violations.isEmpty()) {
            throw new BeanValidationException("planoContas", violations);
        }
    }
    
    public void definirPlanoPai(PlanoContas planoPai) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        
        if (planoPai != null && !planoPai.getTipo().equals(this.tipo)) {
            violations.add(new BeanValidationMessage("planoPai", 
                "Plano pai deve ser do mesmo tipo: " + this.tipo));
        }
        if (planoPai != null && planoPai.equals(this)) {
            violations.add(new BeanValidationMessage("planoPai", 
                "Plano de contas não pode ser pai de si mesmo"));
        }
        
        if (!violations.isEmpty()) {
            throw new BeanValidationException("planoContas", violations);
        }
        
        this.planoPai = planoPai;
    }
    
    public void atualizar(String descricao) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        
        if (descricao != null && !descricao.isBlank() && descricao.length() > 200) {
            violations.add(new BeanValidationMessage("descricao", 
                "Descrição deve ter no máximo 200 caracteres"));
        }
        
        if (!violations.isEmpty()) {
            throw new BeanValidationException("planoContas", violations);
        }
        
        if (descricao != null && !descricao.isBlank()) {
            this.descricao = descricao;
        }
    }
    
    public void ativar() {
        this.ativo = true;
    }
    
    public void inativar() {
        this.ativo = false;
    }
    
    public boolean isAnalitico() {
        return planosFilhos.isEmpty();
    }
    
    public boolean isSintetico() {
        return !planosFilhos.isEmpty();
    }
    
    public Integer getNivel() {
        int nivel = 1;
        PlanoContas pai = this.planoPai;
        while (pai != null) {
            nivel++;
            pai = pai.getPlanoPai();
        }
        return nivel;
    }
    
    // Getters
    public String getCodigo() {
        return codigo;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public TipoPlanoContas getTipo() {
        return tipo;
    }
    
    public PlanoContas getPlanoPai() {
        return planoPai;
    }
    
    public List<PlanoContas> getPlanosFilhos() {
        return planosFilhos;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public boolean isAtivo() {
        return ativo != null && ativo;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanoContas)) return false;
        if (!super.equals(o)) return false;
        PlanoContas that = (PlanoContas) o;
        return Objects.equals(codigo, that.codigo);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), codigo);
    }
    
    @Override
    public String toString() {
        return codigo + " - " + descricao;
    }
}
