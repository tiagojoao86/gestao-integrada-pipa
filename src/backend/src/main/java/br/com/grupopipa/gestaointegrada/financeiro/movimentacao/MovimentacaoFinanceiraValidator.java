package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;

/**
 * Validador responsável por centralizar as regras de criação de MovimentacaoFinanceira.
 * Chamado exclusivamente pelo {@code MovimentacaoFinanceira.Builder}.
 */
public class MovimentacaoFinanceiraValidator {

    private MovimentacaoFinanceiraValidator() {
    }

    public static ValidatedData validate(
            Set<Titulo> titulos,
            ContaBancaria contaBancaria,
            TipoMovimentacao tipo,
            FormaPagamento formaPagamento,
            BigDecimal valor,
            LocalDate data,
            UnidadeNegocio unidadeNegocio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (titulos == null || titulos.isEmpty()) {
            violations.add(new BeanValidationMessage(
                    "validation.movimentacao.titulosObrigatorio",
                    "Pelo menos um título é obrigatório."));
        }
        Validator.of(contaBancaria, "conta bancária", violations).notNull();
        Validator.of(tipo, "tipo de movimentação", violations).notNull();
        Validator.of(formaPagamento, "forma de pagamento", violations).notNull();
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            violations.add(new BeanValidationMessage(
                    "validation.movimentacao.valorPositivo",
                    "Valor deve ser maior que zero."));
        }
        Validator.of(data, "data", violations).notNull();
        Validator.of(unidadeNegocio, "unidade de negócio", violations).notNull();

        Money money = ValidationUtils.validateAndGet(() -> Money.of(valor), violations);

        // Validar regras de negócio para cada título
        if (titulos != null && !titulos.isEmpty() && money != null) {
            // Validar que o valor total da movimentação é igual à soma dos saldos dos títulos
            Money somaSaldos = titulos.stream()
                    .map(Titulo::calcularSaldo)
                    .reduce(Money.zero(), Money::add);

            if (!money.equals(somaSaldos)) {
                violations.add(new BeanValidationMessage(
                        "validation.movimentacao.valorDivergente",
                        "Valor da movimentação (" + money + ") deve ser igual à soma dos saldos "
                                + "dos títulos selecionados (" + somaSaldos + ")."));
            }

            for (Titulo titulo : titulos) {
                if (!titulo.getStatus().permiteMovimentacao()) {
                    violations.add(new BeanValidationMessage(
                            "validation.movimentacao.statusNaoPermite",
                            "Não é possível criar movimentação para título "
                                    + titulo.getStatus().getDescricao() + "."));
                }

                if (titulo.isOrigemParcelamento()) {
                    violations.add(new BeanValidationMessage(
                            "validation.movimentacao.origemParcelamento",
                            "Não é possível criar movimentação para título origem de parcelamento. "
                                    + "Utilize as parcelas."));
                }

                if (!titulo.calcularSaldo().isPositive()) {
                    violations.add(new BeanValidationMessage(
                            "validation.movimentacao.tituloSemSaldo",
                            "Título '" + titulo.getDescricao() + "' não possui saldo para pagamento."));
                }
            }

            // Validar que todos os títulos são do mesmo tipo (A_PAGAR ou A_RECEBER)
            Set<TipoTitulo> tiposTitulo = titulos.stream()
                    .map(Titulo::getTipo)
                    .collect(Collectors.toSet());
            if (tiposTitulo.size() > 1) {
                violations.add(new BeanValidationMessage(
                        "validation.movimentacao.tiposMistos",
                        "Não é possível misturar títulos A Pagar e A Receber na mesma movimentação."));
            }
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("movimentacaoFinanceira", violations);
        }

        return new ValidatedData(titulos, contaBancaria, tipo, formaPagamento, money, data, unidadeNegocio);
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code MovimentacaoFinanceira.Builder}.
     */
    public static class ValidatedData {
        public final Set<Titulo> titulos;
        public final ContaBancaria contaBancaria;
        public final TipoMovimentacao tipo;
        public final FormaPagamento formaPagamento;
        public final Money valor;
        public final LocalDate data;
        public final UnidadeNegocio unidadeNegocio;

        ValidatedData(
                Set<Titulo> titulos,
                ContaBancaria contaBancaria,
                TipoMovimentacao tipo,
                FormaPagamento formaPagamento,
                Money valor,
                LocalDate data,
                UnidadeNegocio unidadeNegocio) {
            this.titulos = titulos;
            this.contaBancaria = contaBancaria;
            this.tipo = tipo;
            this.formaPagamento = formaPagamento;
            this.valor = valor;
            this.data = data;
            this.unidadeNegocio = unidadeNegocio;
        }
    }
}
