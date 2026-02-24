package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;

/**
 * Validador responsável por centralizar as regras de criação de um Título.
 * Chamado exclusivamente pelo {@code Titulo.Builder}.
 */
public class TituloValidator {

    private TituloValidator() {
    }

    public static ValidatedData validate(
            TipoTitulo tipo,
            String descricao,
            String numeroDocumento,
            Pessoa pessoa,
            TituloCategoria tituloCategoria,
            UnidadeNegocio unidadeNegocio,
            BigDecimal valorOriginal,
            LocalDate dataEmissao,
            LocalDate dataVencimento,
            LocalDate dataPagamento) {

        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(tipo, "tipo do título", violations).notNull();
        Validator.of(descricao, "descrição", violations).notBlank().maxLength(500);
        Validator.of(pessoa, "pessoa", violations).notNull();
        Validator.of(tituloCategoria, "categoria", violations).notNull();
        Validator.of(unidadeNegocio, "unidade de negócio", violations).notNull();
        Validator.of(dataEmissao, "data de emissão", violations).notNull();
        Validator.of(dataVencimento, "data de vencimento", violations).notNull();

        if (dataVencimento != null && dataEmissao != null && dataVencimento.isBefore(dataEmissao)) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.dataVencimentoInvalida",
                    "Data de vencimento não pode ser anterior à data de emissão."));
        }

        if (dataPagamento != null && dataEmissao != null && dataPagamento.isBefore(dataEmissao)) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.dataPagamentoInvalida",
                    "Data de pagamento não pode ser anterior à data de emissão."));
        }

        Money money = ValidationUtils.validateAndGet(() -> Money.positive(valorOriginal), violations);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        return new ValidatedData(tipo, descricao, numeroDocumento, pessoa,
                tituloCategoria, unidadeNegocio, money, dataEmissao, dataVencimento);
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code Titulo.Builder}.
     */
    public static class ValidatedData {
        public final TipoTitulo tipo;
        public final String descricao;
        public final String numeroDocumento;
        public final Pessoa pessoa;
        public final TituloCategoria tituloCategoria;
        public final UnidadeNegocio unidadeNegocio;
        public final Money valorOriginal;
        public final LocalDate dataEmissao;
        public final LocalDate dataVencimento;

        ValidatedData(
                TipoTitulo tipo,
                String descricao,
                String numeroDocumento,
                Pessoa pessoa,
                TituloCategoria tituloCategoria,
                UnidadeNegocio unidadeNegocio,
                Money valorOriginal,
                LocalDate dataEmissao,
                LocalDate dataVencimento) {
            this.tipo = tipo;
            this.descricao = descricao;
            this.numeroDocumento = numeroDocumento;
            this.pessoa = pessoa;
            this.tituloCategoria = tituloCategoria;
            this.unidadeNegocio = unidadeNegocio;
            this.valorOriginal = valorOriginal;
            this.dataEmissao = dataEmissao;
            this.dataVencimento = dataVencimento;
        }
    }
}
