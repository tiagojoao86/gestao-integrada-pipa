package br.com.grupopipa.gestaointegrada.atendimento.profissional;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;

public class ProfissionalValidator {

    private ProfissionalValidator() {
    }

    public static ValidatedData validate(
            Pessoa pessoa,
            String conselho,
            String codigoConselho,
            TipoRemuneracao tipoRemuneracao,
            String banco,
            String conta,
            String chavePix,
            String uf,
            Boolean ativo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(pessoa, "pessoa", violations).notNull();
        Validator.of(conselho, "conselho", violations).notBlank();
        Validator.of(codigoConselho, "codigoConselho", violations).notBlank();
        Validator.of(tipoRemuneracao, "tipoRemuneracao", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException("profissional", violations);
        }

        return new ValidatedData(
            pessoa, conselho, codigoConselho, tipoRemuneracao, banco, conta, chavePix, uf, ativo);
    }

    public static class ValidatedData {
        public final Pessoa pessoa;
        public final String conselho;
        public final String codigoConselho;
        public final TipoRemuneracao tipoRemuneracao;
        public final String banco;
        public final String conta;
        public final String chavePix;
        public final String uf;
        public final Boolean ativo;

        ValidatedData(
                Pessoa pessoa,
                String conselho,
                String codigoConselho,
                TipoRemuneracao tipoRemuneracao,
                String banco,
                String conta,
                String chavePix,
                String uf,
                Boolean ativo) {
            this.pessoa = pessoa;
            this.conselho = conselho;
            this.codigoConselho = codigoConselho;
            this.tipoRemuneracao = tipoRemuneracao;
            this.banco = banco;
            this.conta = conta;
            this.chavePix = chavePix;
            this.uf = uf;
            this.ativo = ativo;
        }
    }
}
