package br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaValidator;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.CNPJ;
import br.com.grupopipa.gestaointegrada.core.valueobject.CPF;
import br.com.grupopipa.gestaointegrada.core.valueobject.Email;
import br.com.grupopipa.gestaointegrada.core.valueobject.Endereco;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import br.com.grupopipa.gestaointegrada.core.valueobject.PhoneNumber;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @Embedded
    private Endereco endereco;

    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_pessoa_responsavel"))
    private Pessoa responsavel;

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
        this.endereco = data.endereco;
    }

    protected Pessoa() {
    }

    void setResponsavel(Pessoa responsavel) {
        this.responsavel = responsavel;
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
        private Pessoa responsavelObj;
        private String enderecoCEP;
        private String enderecoLogradouro;
        private String enderecoNumero;
        private String enderecoComplemento;
        private String enderecoBairro;
        private String enderecoCidade;
        private String enderecoUF;

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

        public Builder responsavel(Pessoa responsavel) {
            this.responsavelObj = responsavel;
            return this;
        }

        public Builder enderecoCEP(String cep) {
            this.enderecoCEP = cep;
            return this;
        }

        public Builder enderecoLogradouro(String logradouro) {
            this.enderecoLogradouro = logradouro;
            return this;
        }

        public Builder enderecoNumero(String numero) {
            this.enderecoNumero = numero;
            return this;
        }

        public Builder enderecoComplemento(String complemento) {
            this.enderecoComplemento = complemento;
            return this;
        }

        public Builder enderecoBairro(String bairro) {
            this.enderecoBairro = bairro;
            return this;
        }

        public Builder enderecoCidade(String cidade) {
            this.enderecoCidade = cidade;
            return this;
        }

        public Builder enderecoUF(String uf) {
            this.enderecoUF = uf;
            return this;
        }

        public Pessoa build() {
            PessoaValidator.ValidatedData data = PessoaValidator.validate(tipoPessoa, nome, email, telefone, cpf,
                    dataNascimento, cnpj, razaoSocial, inscricaoEstadual, observacoes, ativa,
                    enderecoCEP, enderecoLogradouro, enderecoNumero, enderecoComplemento,
                    enderecoBairro, enderecoCidade, enderecoUF);
            Pessoa pessoa = new Pessoa(data);
            pessoa.setResponsavel(this.responsavelObj);
            return pessoa;
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
            String inscricaoEstadualStr,
            Pessoa responsavelArg) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Nome nomeValidado = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);

        Email emailValidado = null;
        if (emailStr != null && !emailStr.isBlank()) {
            emailValidado = ValidationUtils.validateAndGet(() -> new Email(emailStr), violations);
        }

        PhoneNumber telefoneValidado = null;
        if (telefoneStr != null && !telefoneStr.isBlank()) {
            telefoneValidado = ValidationUtils.validateAndGet(() -> new PhoneNumber(telefoneStr), violations);
        }

        CPF cpfValidado = null;
        CNPJ cnpjValidado = null;
        if (this.tipoPessoa == TipoPessoa.FISICA) {
            if (cpfStr != null && !cpfStr.isBlank()) {
                cpfValidado = ValidationUtils.validateAndGet(() -> new CPF(cpfStr), violations);
            }
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

        this.responsavel = responsavelArg;
    }

    public void atualizarEndereco(
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf) {
        String cepNormalizado = cep != null ? cep.replaceAll("[^0-9]", "") : null;
        this.endereco = new Endereco(cepNormalizado, logradouro, numero, complemento, bairro, cidade, uf);
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

    public String getEnderecoCep() {
        return endereco != null ? endereco.getCep() : null;
    }

    public String getEnderecoLogradouro() {
        return endereco != null ? endereco.getLogradouro() : null;
    }

    public String getEnderecoNumero() {
        return endereco != null ? endereco.getNumero() : null;
    }

    public String getEnderecoComplemento() {
        return endereco != null ? endereco.getComplemento() : null;
    }

    public String getEnderecoBairro() {
        return endereco != null ? endereco.getBairro() : null;
    }

    public String getEnderecoCidade() {
        return endereco != null ? endereco.getCidade() : null;
    }

    public String getEnderecoUF() {
        return endereco != null ? endereco.getUf() : null;
    }

    public Pessoa getResponsavel() {
        return responsavel;
    }

    public UUID getResponsavelId() {
        return responsavel != null ? responsavel.getId() : null;
    }

    public String getResponsavelNome() {
        return responsavel != null ? responsavel.getNome() : null;
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
