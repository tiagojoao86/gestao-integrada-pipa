package br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoValidator;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
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

    @Column(name = "titulo_categoria_id")
    private UUID tituloCategoriaId;

    private Procedimento(ProcedimentoValidator.ValidatedData data) {
        this.codigo = data.codigo;
        this.codigoTiss = data.codigoTiss;
        this.codigoTuss = data.codigoTuss;
        this.descricao = data.descricao;
        this.ativo = data.ativo != null ? data.ativo : true;
    }

    protected Procedimento() {
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
        private UUID tituloCategoriaId;

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

        public Builder tituloCategoriaId(UUID tituloCategoriaId) {
            this.tituloCategoriaId = tituloCategoriaId;
            return this;
        }

        public Procedimento build() {
            Procedimento p = new Procedimento(
                ProcedimentoValidator.validate(codigo, codigoTiss, codigoTuss, descricao, ativo));
            p.tituloCategoriaId = this.tituloCategoriaId;
            return p;
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(
            String codigoArg, String codigoTissArg, String codigoTussArg,
            String descricaoArg, Boolean ativoArg, UUID tituloCategoriaIdArg) {
        ProcedimentoValidator.ValidatedData data =
            ProcedimentoValidator.validate(codigoArg, codigoTissArg, codigoTussArg, descricaoArg, ativoArg);
        this.codigo = data.codigo;
        this.codigoTiss = data.codigoTiss;
        this.codigoTuss = data.codigoTuss;
        this.descricao = data.descricao;
        if (data.ativo != null) {
            this.ativo = data.ativo;
        }
        this.tituloCategoriaId = tituloCategoriaIdArg;
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

    public UUID getTituloCategoriaId() {
        return tituloCategoriaId;
    }
}
