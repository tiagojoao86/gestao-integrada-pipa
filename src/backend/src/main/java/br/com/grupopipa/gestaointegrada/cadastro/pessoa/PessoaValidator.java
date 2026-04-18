package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.CNPJ;
import br.com.grupopipa.gestaointegrada.core.valueobject.CPF;
import br.com.grupopipa.gestaointegrada.core.valueobject.Email;
import br.com.grupopipa.gestaointegrada.core.valueobject.Endereco;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import br.com.grupopipa.gestaointegrada.core.valueobject.PhoneNumber;

/**
 * Validador responsável por centralizar as regras de criação de Pessoa.
 * Chamado exclusivamente pelo {@code Pessoa.Builder}.
 */
public class PessoaValidator {

    private PessoaValidator() {
    }

    public static ValidatedData validate(
            TipoPessoa tipoPessoa,
            String nomeStr,
            String emailStr,
            String telefoneStr,
            String cpfStr,
            LocalDate dataNascimento,
            String cnpjStr,
            String razaoSocial,
            String inscricaoEstadual,
            String observacoes,
            Boolean ativa,
            String enderecoCEP,
            String enderecoLogradouro,
            String enderecoNumero,
            String enderecoComplemento,
            String enderecoBairro,
            String enderecoCidade,
            String enderecoUF) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (tipoPessoa == null) {
            violations.add(new BeanValidationMessage("tipoPessoa", "Tipo de pessoa é obrigatório"));
        }

        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
        Email email = ValidationUtils.validateAndGet(() -> new Email(emailStr), violations);
        PhoneNumber telefone = ValidationUtils.validateAndGet(() -> new PhoneNumber(telefoneStr), violations);

        CPF cpf = null;
        CNPJ cnpj = null;
        if (tipoPessoa == TipoPessoa.FISICA) {
            if (cpfStr != null && !cpfStr.isBlank()) {
                cpf = ValidationUtils.validateAndGet(() -> new CPF(cpfStr), violations);
            }
        } else if (tipoPessoa == TipoPessoa.JURIDICA) {
            cnpj = ValidationUtils.validateAndGet(() -> new CNPJ(cnpjStr), violations);
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("Pessoa", violations);
        }

        String cepNormalizado = enderecoCEP != null ? enderecoCEP.replaceAll("[^0-9]", "") : null;
        Endereco endereco = new Endereco(
                cepNormalizado, enderecoLogradouro, enderecoNumero,
                enderecoComplemento, enderecoBairro, enderecoCidade, enderecoUF);

        return new ValidatedData(tipoPessoa, nome, email, telefone, cpf, dataNascimento,
                cnpj, razaoSocial, inscricaoEstadual, observacoes, ativa, endereco);
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code Pessoa.Builder}.
     */
    public static class ValidatedData {
        public final TipoPessoa tipoPessoa;
        public final Nome nome;
        public final Email email;
        public final PhoneNumber telefone;
        public final CPF cpf;
        public final LocalDate dataNascimento;
        public final CNPJ cnpj;
        public final String razaoSocial;
        public final String inscricaoEstadual;
        public final String observacoes;
        public final Boolean ativa;
        public final Endereco endereco;

        ValidatedData(
                TipoPessoa tipoPessoa,
                Nome nome,
                Email email,
                PhoneNumber telefone,
                CPF cpf,
                LocalDate dataNascimento,
                CNPJ cnpj,
                String razaoSocial,
                String inscricaoEstadual,
                String observacoes,
                Boolean ativa,
                Endereco endereco) {
            this.tipoPessoa = tipoPessoa;
            this.nome = nome;
            this.email = email;
            this.telefone = telefone;
            this.cpf = cpf;
            this.dataNascimento = dataNascimento;
            this.cnpj = cnpj;
            this.razaoSocial = razaoSocial;
            this.inscricaoEstadual = inscricaoEstadual;
            this.observacoes = observacoes;
            this.ativa = ativa;
            this.endereco = endereco;
        }
    }
}
