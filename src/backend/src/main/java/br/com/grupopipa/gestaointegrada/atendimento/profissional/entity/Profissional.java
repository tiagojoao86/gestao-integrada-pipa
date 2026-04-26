package br.com.grupopipa.gestaointegrada.atendimento.profissional.entity;

import br.com.grupopipa.gestaointegrada.atendimento.profissional.ProfissionalValidator;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.TipoRemuneracao;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "profissional")
public class Profissional extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "pessoa_id",
        nullable = false,
        foreignKey = @jakarta.persistence.ForeignKey(name = "fk_profissional_pessoa")
    )
    private Pessoa pessoa;

    @Column(name = "conselho", nullable = false, length = 20)
    private String conselho;

    @Column(name = "codigo_conselho", nullable = false, length = 30)
    private String codigoConselho;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_remuneracao", nullable = false, length = 20)
    private TipoRemuneracao tipoRemuneracao;

    @Column(name = "banco", length = 100)
    private String banco;

    @Column(name = "conta", length = 50)
    private String conta;

    @Column(name = "chave_pix", length = 150)
    private String chavePix;

    @Column(name = "uf", length = 2)
    private String uf;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    private Profissional(ProfissionalValidator.ValidatedData data) {
        this.pessoa = data.pessoa;
        this.conselho = data.conselho;
        this.codigoConselho = data.codigoConselho;
        this.tipoRemuneracao = data.tipoRemuneracao;
        this.banco = data.banco;
        this.conta = data.conta;
        this.chavePix = data.chavePix;
        this.uf = data.uf;
        this.ativo = data.ativo != null ? data.ativo : true;
    }

    protected Profissional() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private Pessoa pessoa;
        private String conselho;
        private String codigoConselho;
        private TipoRemuneracao tipoRemuneracao;
        private String banco;
        private String conta;
        private String chavePix;
        private String uf;
        private Boolean ativo = true;

        public Builder pessoa(Pessoa pessoa) {
            this.pessoa = pessoa;
            return this;
        }

        public Builder conselho(String conselho) {
            this.conselho = conselho;
            return this;
        }

        public Builder codigoConselho(String codigoConselho) {
            this.codigoConselho = codigoConselho;
            return this;
        }

        public Builder tipoRemuneracao(TipoRemuneracao tipoRemuneracao) {
            this.tipoRemuneracao = tipoRemuneracao;
            return this;
        }

        public Builder banco(String banco) {
            this.banco = banco;
            return this;
        }

        public Builder conta(String conta) {
            this.conta = conta;
            return this;
        }

        public Builder chavePix(String chavePix) {
            this.chavePix = chavePix;
            return this;
        }

        public Builder uf(String uf) {
            this.uf = uf;
            return this;
        }

        public Builder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public Profissional build() {
            return new Profissional(ProfissionalValidator.validate(
                pessoa, conselho, codigoConselho, tipoRemuneracao, banco, conta, chavePix, uf, ativo));
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(
            Pessoa pessoaArg,
            String conselhoArg,
            String codigoConselhoArg,
            TipoRemuneracao tipoRemuneracaoArg,
            String bancoArg,
            String contaArg,
            String chavePixArg,
            String ufArg,
            Boolean ativoArg) {
        ProfissionalValidator.ValidatedData data = ProfissionalValidator.validate(
            pessoaArg, conselhoArg, codigoConselhoArg, tipoRemuneracaoArg,
            bancoArg, contaArg, chavePixArg, ufArg, ativoArg);
        this.pessoa = data.pessoa;
        this.conselho = data.conselho;
        this.codigoConselho = data.codigoConselho;
        this.tipoRemuneracao = data.tipoRemuneracao;
        this.banco = data.banco;
        this.conta = data.conta;
        this.chavePix = data.chavePix;
        this.uf = data.uf;
        if (data.ativo != null) {
            this.ativo = data.ativo;
        }
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public Pessoa getPessoa() {
        return pessoa;
    }

    public String getConselho() {
        return conselho;
    }

    public String getCodigoConselho() {
        return codigoConselho;
    }

    public TipoRemuneracao getTipoRemuneracao() {
        return tipoRemuneracao;
    }

    public String getBanco() {
        return banco;
    }

    public String getConta() {
        return conta;
    }

    public String getChavePix() {
        return chavePix;
    }

    public String getUf() {
        return uf;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public boolean isAtivo() {
        return ativo != null && ativo;
    }
}
