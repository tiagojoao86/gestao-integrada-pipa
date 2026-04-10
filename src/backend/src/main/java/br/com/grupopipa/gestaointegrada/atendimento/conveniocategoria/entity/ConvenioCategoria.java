package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "convenio_categoria")
public class ConvenioCategoria extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "convenio_id",
        nullable = false,
        foreignKey = @jakarta.persistence.ForeignKey(name = "fk_convenio_categoria_convenio")
    )
    private Convenio convenio;

    @Embedded
    private Nome nome;

    @Column(name = "codigo_ans_plano", length = 20)
    private String codigoAnsPlano;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    private ConvenioCategoria(ValidatedData data) {
        this.convenio = data.convenio;
        this.nome = data.nome;
        this.codigoAnsPlano = data.codigoAnsPlano;
        this.ativo = data.ativo != null ? data.ativo : true;
    }

    protected ConvenioCategoria() {
    }

    // =========================================================================
    // ValidatedData
    // =========================================================================

    private static class ValidatedData {
        final Convenio convenio;
        final Nome nome;
        final String codigoAnsPlano;
        final Boolean ativo;

        ValidatedData(Convenio convenio, Nome nome, String codigoAnsPlano, Boolean ativo) {
            this.convenio = convenio;
            this.nome = nome;
            this.codigoAnsPlano = codigoAnsPlano;
            this.ativo = ativo;
        }
    }

    private static ValidatedData validate(
        Convenio convenio,
        String nomeStr,
        String codigoAnsPlano,
        Boolean ativo
    ) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(convenio, "convenio", violations).notNull();
        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
        Validator.of(codigoAnsPlano, "codigoAnsPlano", violations).maxLength(20);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("convenioCategoria", violations);
        }

        return new ValidatedData(convenio, nome, codigoAnsPlano, ativo);
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private Convenio convenio;
        private String nome;
        private String codigoAnsPlano;
        private Boolean ativo = true;

        public Builder convenio(Convenio convenio) {
            this.convenio = convenio;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder codigoAnsPlano(String codigoAnsPlano) {
            this.codigoAnsPlano = codigoAnsPlano;
            return this;
        }

        public Builder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public ConvenioCategoria build() {
            ValidatedData data = validate(convenio, nome, codigoAnsPlano, ativo);
            return new ConvenioCategoria(data);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(Convenio convenioArg, String nomeStr, String codigoAnsPlanoArg, Boolean ativoArg) {
        ValidatedData data = validate(convenioArg, nomeStr, codigoAnsPlanoArg, ativoArg);
        this.convenio = data.convenio;
        this.nome = data.nome;
        this.codigoAnsPlano = data.codigoAnsPlano;
        if (data.ativo != null) {
            this.ativo = data.ativo;
        }
    }

    // Getters
    public Convenio getConvenio() {
        return convenio;
    }

    public String getNome() {
        return nome != null ? nome.getValue() : null;
    }

    public String getCodigoAnsPlano() {
        return codigoAnsPlano;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public boolean isAtivo() {
        return ativo != null && ativo;
    }
}
