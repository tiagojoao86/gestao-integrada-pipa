package br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "procedimento")
@SuppressWarnings("checkstyle:MagicNumber")
public class Procedimento extends BaseEntity {

    @Column(name = "codigo", length = 30, nullable = false)
    private String codigo;

    @Column(name = "codigo_tiss", length = 20)
    private String codigoTiss;

    @Column(name = "codigo_tuss", length = 20)
    private String codigoTuss;

    @Column(name = "descricao", length = 200, nullable = false)
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    private Procedimento(ValidatedData data) {
        this.codigo = data.codigo;
        this.codigoTiss = data.codigoTiss;
        this.codigoTuss = data.codigoTuss;
        this.descricao = data.descricao;
        this.ativo = data.ativo != null ? data.ativo : true;
    }

    protected Procedimento() {
    }

    // =========================================================================
    // Validation
    // =========================================================================

    private static class ValidatedData {
        final String codigo;
        final String codigoTiss;
        final String codigoTuss;
        final String descricao;
        final Boolean ativo;

        ValidatedData(String codigo, String codigoTiss, String codigoTuss, String descricao, Boolean ativo) {
            this.codigo = codigo;
            this.codigoTiss = codigoTiss;
            this.codigoTuss = codigoTuss;
            this.descricao = descricao;
            this.ativo = ativo;
        }
    }

    private static ValidatedData validate(
            String codigo, String codigoTiss, String codigoTuss, String descricao, Boolean ativo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(codigo, "codigo", violations).notNull().maxLength(30);
        Validator.of(descricao, "descricao", violations).notNull().maxLength(200);
        Validator.of(codigoTiss, "codigoTiss", violations).maxLength(20);
        Validator.of(codigoTuss, "codigoTuss", violations).maxLength(20);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("procedimento", violations);
        }
        return new ValidatedData(codigo, codigoTiss, codigoTuss, descricao, ativo);
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private String codigo;
        private String codigoTiss;
        private String codigoTuss;
        private String descricao;
        private Boolean ativo = true;

        public Builder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public Builder codigoTiss(String codigoTiss) {
            this.codigoTiss = codigoTiss;
            return this;
        }

        public Builder codigoTuss(String codigoTuss) {
            this.codigoTuss = codigoTuss;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public Procedimento build() {
            ValidatedData data = validate(codigo, codigoTiss, codigoTuss, descricao, ativo);
            return new Procedimento(data);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(
            String codigoArg, String codigoTissArg, String codigoTussArg,
            String descricaoArg, Boolean ativoArg) {
        ValidatedData data = validate(codigoArg, codigoTissArg, codigoTussArg, descricaoArg, ativoArg);
        this.codigo = data.codigo;
        this.codigoTiss = data.codigoTiss;
        this.codigoTuss = data.codigoTuss;
        this.descricao = data.descricao;
        if (data.ativo != null) {
            this.ativo = data.ativo;
        }
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public String getCodigo() {
        return codigo;
    }

    public String getCodigoTiss() {
        return codigoTiss;
    }

    public String getCodigoTuss() {
        return codigoTuss;
    }

    public String getDescricao() {
        return descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }
}
