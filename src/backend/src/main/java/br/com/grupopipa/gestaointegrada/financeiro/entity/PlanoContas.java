package br.com.grupopipa.gestaointegrada.financeiro.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.entity.UnidadeNegocioFiltravel;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Entidade para Plano de Contas com estrutura hierárquica */
@Entity
@Table(name = "plano_contas", uniqueConstraints = {
    @UniqueConstraint(name = "uk_plano_contas_codigo", columnNames = "codigo")
})
public class PlanoContas extends BaseEntity implements UnidadeNegocioFiltravel {

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 200)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoPlanoContas tipo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plano_pai_id", foreignKey = @ForeignKey(name = "fk_plano_contas_pai"))
    private PlanoContas planoPai;

    @OneToMany(mappedBy = "planoPai", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanoContas> planosFilhos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_negocio_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_plano_contas_unidade_negocio"))
    private UnidadeNegocio unidadeNegocio;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    private PlanoContas(
            String codigo,
            String descricao,
            TipoPlanoContas tipo,
            PlanoContas planoPai,
            UnidadeNegocio unidadeNegocio) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.planoPai = planoPai;
        this.unidadeNegocio = unidadeNegocio;
    }

    protected PlanoContas() {
    }

    private static class ValidatedData {
        final String codigo;
        final String descricao;
        final TipoPlanoContas tipo;
        final PlanoContas planoPai;
        final UnidadeNegocio unidadeNegocio;

        ValidatedData(
                String codigo,
                String descricao,
                TipoPlanoContas tipo,
                PlanoContas planoPai,
                UnidadeNegocio unidadeNegocio) {
            this.codigo = codigo;
            this.descricao = descricao;
            this.tipo = tipo;
            this.planoPai = planoPai;
            this.unidadeNegocio = unidadeNegocio;
        }
    }

    private static ValidatedData validate(
            String codigo,
            String descricao,
            TipoPlanoContas tipo,
            PlanoContas planoPai,
            UnidadeNegocio unidadeNegocio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(codigo, "código", violations).notBlank().maxLength(20);
        Validator.of(descricao, "descrição", violations).notBlank().maxLength(200);
        Validator.of(tipo, "tipo", violations).notNull();
        Validator.of(unidadeNegocio, "unidade de negócio", violations).notNull();

        // Validar plano pai
        if (planoPai != null && tipo != null && !planoPai.getTipo().equals(tipo)) {
            violations.add(new BeanValidationMessage(
                    "validation.planoContas.tipoPaiDiferente",
                    "Plano pai deve ser do mesmo tipo: " + tipo));
        }
        // Nota: Validação de auto-referência não é possível aqui pois o objeto ainda
        // não foi criado
        // Esta validação deve ser feita em @PrePersist/@PreUpdate se necessário

        if (!violations.isEmpty()) {
            throw new BeanValidationException("planoContas", violations);
        }

        return new ValidatedData(codigo, descricao, tipo, planoPai, unidadeNegocio);
    }

    public void atualizar(String descricao) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(descricao, "descrição", violations).notBlank().maxLength(200);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("planoContas", violations);
        }

        this.descricao = descricao;
    }

    public void atualizarUnidadeNegocio(UnidadeNegocio unidadeNegocio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(unidadeNegocio, "unidade de negócio", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException("planoContas", violations);
        }

        this.unidadeNegocio = unidadeNegocio;
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

    public UnidadeNegocio getUnidadeNegocio() {
        return unidadeNegocio;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public boolean isAtivo() {
        return ativo != null && ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlanoContas)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
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

    public static class Builder {
        private String codigo;
        private String descricao;
        private TipoPlanoContas tipo;
        private PlanoContas planoPai;
        private UnidadeNegocio unidadeNegocio;

        public Builder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder tipo(TipoPlanoContas tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder planoPai(PlanoContas planoPai) {
            this.planoPai = planoPai;
            return this;
        }

        public Builder unidadeNegocio(UnidadeNegocio unidadeNegocio) {
            this.unidadeNegocio = unidadeNegocio;
            return this;
        }

        public PlanoContas build() {
            ValidatedData data = validate(this.codigo, this.descricao, this.tipo, this.planoPai, this.unidadeNegocio);
            return new PlanoContas(
                    data.codigo, data.descricao, data.tipo, data.planoPai, data.unidadeNegocio);
        }
    }
}
