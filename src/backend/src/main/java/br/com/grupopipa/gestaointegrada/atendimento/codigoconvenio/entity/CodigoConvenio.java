package br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.entity;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "codigo_convenio")
@SuppressWarnings("checkstyle:MagicNumber")
public class CodigoConvenio extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "convenio_id",
        nullable = false,
        foreignKey = @jakarta.persistence.ForeignKey(name = "fk_codigo_convenio_convenio")
    )
    private Convenio convenio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "procedimento_id",
        nullable = false,
        foreignKey = @jakarta.persistence.ForeignKey(name = "fk_codigo_convenio_procedimento")
    )
    private Procedimento procedimento;

    @Column(name = "codigo", length = 30, nullable = false)
    private String codigo;

    private CodigoConvenio(ValidatedData data) {
        this.convenio = data.convenio;
        this.procedimento = data.procedimento;
        this.codigo = data.codigo;
    }

    protected CodigoConvenio() {
    }

    // =========================================================================
    // Validation
    // =========================================================================

    private static class ValidatedData {
        final Convenio convenio;
        final Procedimento procedimento;
        final String codigo;

        ValidatedData(Convenio convenio, Procedimento procedimento, String codigo) {
            this.convenio = convenio;
            this.procedimento = procedimento;
            this.codigo = codigo;
        }
    }

    private static ValidatedData validate(Convenio convenio, Procedimento procedimento, String codigo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(convenio, "convenio", violations).notNull();
        Validator.of(procedimento, "procedimento", violations).notNull();
        Validator.of(codigo, "codigo", violations).notNull().maxLength(30);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("codigoConvenio", violations);
        }
        return new ValidatedData(convenio, procedimento, codigo);
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private Convenio convenio;
        private Procedimento procedimento;
        private String codigo;

        public Builder convenio(Convenio convenio) {
            this.convenio = convenio;
            return this;
        }

        public Builder procedimento(Procedimento procedimento) {
            this.procedimento = procedimento;
            return this;
        }

        public Builder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public CodigoConvenio build() {
            ValidatedData data = validate(convenio, procedimento, codigo);
            return new CodigoConvenio(data);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(String novoCodigo) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        Validator.of(novoCodigo, "codigo", violations).notNull().maxLength(30);
        if (!violations.isEmpty()) {
            throw new BeanValidationException("codigoConvenio", violations);
        }
        this.codigo = novoCodigo;
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public Convenio getConvenio() {
        return convenio;
    }

    public Procedimento getProcedimento() {
        return procedimento;
    }

    public String getCodigo() {
        return codigo;
    }
}
