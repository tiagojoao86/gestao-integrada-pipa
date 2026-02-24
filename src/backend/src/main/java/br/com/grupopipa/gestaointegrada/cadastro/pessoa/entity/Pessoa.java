package br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaValidator;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.CNPJ;
import br.com.grupopipa.gestaointegrada.core.valueobject.CPF;
import br.com.grupopipa.gestaointegrada.core.valueobject.Email;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import br.com.grupopipa.gestaointegrada.core.valueobject.PhoneNumber;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Entidade Pessoa - modelo flat (sem herança). Contém campos para Pessoa Física
 * e Jurídica,
 * validados por tipo.
 */
@Entity
@Table(name = "pessoa")
public class Pessoa extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false, length = 20)
    private TipoPessoa tipoPessoa;

    @Embedded
    private Nome nome;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email"))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "telefone"))
    private PhoneNumber telefone;

    // Campos específicos de Pessoa Física
    @Embedded
    private CPF cpf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    // Campos específicos de Pessoa Jurídica
    @Embedded
    private CNPJ cnpj;

    @Column(name = "razao_social", length = 200)
    private String razaoSocial;

    @Column(name = "inscricao_estadual", length = 20)
    private String inscricaoEstadual;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;

    private Pessoa(PessoaValidator.ValidatedData data) {
        this.tipoPessoa = data.tipoPessoa;
        this.nome = data.nome;
        this.email = data.email;
        this.telefone = data.telefone;
        this.cpf = data.cpf;
        this.dataNascimento = data.dataNascimento;
        this.cnpj = data.cnpj;
        this.razaoSocial = data.razaoSocial;
        this.inscricaoEstadual = data.inscricaoEstadual;
        this.observacoes = data.observacoes;
        this.ativa = data.ativa != null ? data.ativa : true;
    }

    protected Pessoa() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private TipoPessoa tipoPessoa;
        private String nome;
        private String email;
        private String telefone;
        private String cpf;
        private LocalDate dataNascimento;
        private String cnpj;
        private String razaoSocial;
        private String inscricaoEstadual;
        private String observacoes;
        private Boolean ativa = true;

        public Builder tipoPessoa(TipoPessoa tipoPessoa) {
            this.tipoPessoa = tipoPessoa;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder telefone(String telefone) {
            this.telefone = telefone;
            return this;
        }

        public Builder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public Builder dataNascimento(LocalDate dataNascimento) {
            this.dataNascimento = dataNascimento;
            return this;
        }

        public Builder cnpj(String cnpj) {
            this.cnpj = cnpj;
            return this;
        }

        public Builder razaoSocial(String razaoSocial) {
            this.razaoSocial = razaoSocial;
            return this;
        }

        public Builder inscricaoEstadual(String inscricaoEstadual) {
            this.inscricaoEstadual = inscricaoEstadual;
            return this;
        }

        public Builder observacoes(String observacoes) {
            this.observacoes = observacoes;
            return this;
        }

        public Builder ativa(Boolean ativa) {
            this.ativa = ativa;
            return this;
        }

        public Pessoa build() {
            PessoaValidator.ValidatedData data = PessoaValidator.validate(tipoPessoa, nome, email, telefone, cpf,
                    dataNascimento, cnpj, razaoSocial, inscricaoEstadual, observacoes, ativa);
            return new Pessoa(data);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    @PrePersist
    @PreUpdate
    private void validarCamposPorTipo() {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (tipoPessoa == null) {
            violations.add(new BeanValidationMessage("tipoPessoa", "Tipo de pessoa é obrigatório"));
        } else if (tipoPessoa == TipoPessoa.FISICA) {
            if (cpf == null) {
                violations.add(new BeanValidationMessage("cpf", "CPF é obrigatório para Pessoa Física"));
            }
            if (cnpj != null) {
                violations.add(
                        new BeanValidationMessage("cnpj", "CNPJ não deve ser informado para Pessoa Física"));
            }
            if (razaoSocial != null) {
                violations.add(
                        new BeanValidationMessage(
                                "razaoSocial", "Razão Social não deve ser informada para Pessoa Física"));
            }
            if (inscricaoEstadual != null) {
                violations.add(
                        new BeanValidationMessage(
                                "inscricaoEstadual",
                                "Inscrição Estadual não deve ser informada para Pessoa Física"));
            }
        } else if (tipoPessoa == TipoPessoa.JURIDICA) {
            if (cnpj == null) {
                violations.add(
                        new BeanValidationMessage("cnpj", "CNPJ é obrigatório para Pessoa Jurídica"));
            }
            if (cpf != null) {
                violations.add(
                        new BeanValidationMessage("cpf", "CPF não deve ser informado para Pessoa Jurídica"));
            }
            if (dataNascimento != null) {
                violations.add(
                        new BeanValidationMessage(
                                "dataNascimento",
                                "Data de Nascimento não deve ser informada para Pessoa Jurídica"));
            }
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("Pessoa", violations);
        }
    }

    public void atualizar(
            String nomeStr,
            String emailStr,
            String telefoneStr,
            String cpfStr,
            LocalDate dataNascimentoArg,
            String cnpjStr,
            String razaoSocialStr,
            String inscricaoEstadualStr) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Nome nomeValidado = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
        Email emailValidado = ValidationUtils.validateAndGet(() -> new Email(emailStr), violations);
        PhoneNumber telefoneValidado = ValidationUtils.validateAndGet(() -> new PhoneNumber(telefoneStr), violations);

        CPF cpfValidado = null;
        CNPJ cnpjValidado = null;
        if (this.tipoPessoa == TipoPessoa.FISICA) {
            cpfValidado = ValidationUtils.validateAndGet(() -> new CPF(cpfStr), violations);
        } else {
            cnpjValidado = ValidationUtils.validateAndGet(() -> new CNPJ(cnpjStr), violations);
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("Pessoa", violations);
        }

        this.nome = nomeValidado;
        this.email = emailValidado;
        this.telefone = telefoneValidado;

        if (this.tipoPessoa == TipoPessoa.FISICA) {
            this.cpf = cpfValidado;
            this.dataNascimento = dataNascimentoArg;
        } else {
            this.cnpj = cnpjValidado;
            this.razaoSocial = razaoSocialStr;
            this.inscricaoEstadual = inscricaoEstadualStr;
        }
    }

    public void adicionarObservacao(String observacao) {
        if (this.observacoes == null) {
            this.observacoes = observacao;
        } else {
            this.observacoes += "\n" + observacao;
        }
    }

    public void ativar() {
        this.ativa = true;
    }

    public void inativar() {
        this.ativa = false;
    }

    // Getters
    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public String getNome() {
        return nome != null ? nome.getValue() : null;
    }

    public String getEmail() {
        return email != null ? email.getValue() : null;
    }

    public String getTelefone() {
        return telefone != null ? telefone.getValue() : null;
    }

    public String getCpf() {
        return cpf != null ? cpf.getValue() : null;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getCnpj() {
        return cnpj != null ? cnpj.getValue() : null;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public boolean isAtiva() {
        return ativa != null && ativa;
    }

    public boolean isPessoaFisica() {
        return tipoPessoa == TipoPessoa.FISICA;
    }

    public boolean isPessoaJuridica() {
        return tipoPessoa == TipoPessoa.JURIDICA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pessoa)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Pessoa pessoa = (Pessoa) o;
        return Objects.equals(getNome(), pessoa.getNome())
                && Objects.equals(getCpf(), pessoa.getCpf())
                && Objects.equals(getCnpj(), pessoa.getCnpj());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getNome(), getCpf(), getCnpj());
    }
}
