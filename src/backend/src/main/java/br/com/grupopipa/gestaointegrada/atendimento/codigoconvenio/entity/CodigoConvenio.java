package br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.entity;

import br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.CodigoConvenioValidator;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
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

    private CodigoConvenio(CodigoConvenioValidator.ValidatedData data) {
        this.convenio = data.convenio;
        this.procedimento = data.procedimento;
        this.codigo = data.codigo;
    }

    protected CodigoConvenio() {
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
            return new CodigoConvenio(
                CodigoConvenioValidator.validate(convenio, procedimento, codigo));
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(String novoCodigo) {
        CodigoConvenioValidator.ValidatedData data =
            CodigoConvenioValidator.validate(this.convenio, this.procedimento, novoCodigo);
        this.codigo = data.codigo;
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
