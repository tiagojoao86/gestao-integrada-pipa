package br.com.grupopipa.gestaointegrada.atendimento.convenio.entity;

import java.util.Objects;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioValidator;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "convenio")
public class Convenio extends BaseEntity {

    @Embedded
    private Nome nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "pessoa_id",
        nullable = true,
        foreignKey = @jakarta.persistence.ForeignKey(name = "fk_convenio_pessoa")
    )
    private Pessoa pessoa;

    @Column(name = "registro_ans", length = 20)
    private String registroAns;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cobranca", nullable = false)
    private ConvenioTipoCobrancaEnum tipoCobranca = ConvenioTipoCobrancaEnum.FATURADO;

    private Convenio(ConvenioValidator.ValidatedData data) {
        this.nome = data.nome;
        this.pessoa = data.pessoa;
        this.registroAns = data.registroAns;
        this.ativo = data.ativo != null ? data.ativo : true;
        this.tipoCobranca = data.tipoCobranca != null
            ? data.tipoCobranca : ConvenioTipoCobrancaEnum.FATURADO;
    }

    protected Convenio() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private String nome;
        private Pessoa pessoa;
        private String registroAns;
        private Boolean ativo = true;
        private ConvenioTipoCobrancaEnum tipoCobranca = ConvenioTipoCobrancaEnum.FATURADO;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder pessoa(Pessoa pessoa) {
            this.pessoa = pessoa;
            return this;
        }

        public Builder registroAns(String registroAns) {
            this.registroAns = registroAns;
            return this;
        }

        public Builder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public Builder tipoCobranca(ConvenioTipoCobrancaEnum tipoCobranca) {
            this.tipoCobranca = tipoCobranca;
            return this;
        }

        public Convenio build() {
            ConvenioValidator.ValidatedData data = ConvenioValidator.validate(
                nome, pessoa, registroAns, ativo, tipoCobranca
            );
            return new Convenio(data);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(
            String nomeStr, Pessoa pessoaArg, String registroAnsArg,
            Boolean ativoArg, ConvenioTipoCobrancaEnum tipoCobrancaArg) {
        ConvenioValidator.ValidatedData data = ConvenioValidator.validate(
            nomeStr, pessoaArg, registroAnsArg, ativoArg, tipoCobrancaArg
        );
        this.nome = data.nome;
        this.pessoa = data.pessoa;
        this.registroAns = data.registroAns;
        if (data.ativo != null) {
            this.ativo = data.ativo;
        }
        this.tipoCobranca = data.tipoCobranca;
    }

    // Getters
    public String getNome() {
        return nome != null ? nome.getValue() : null;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public String getRegistroAns() {
        return registroAns;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public boolean isAtivo() {
        return ativo != null && ativo;
    }

    public ConvenioTipoCobrancaEnum getTipoCobranca() {
        return tipoCobranca;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Convenio)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Convenio convenio = (Convenio) o;
        return Objects.equals(getNome(), convenio.getNome());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getNome());
    }
}
