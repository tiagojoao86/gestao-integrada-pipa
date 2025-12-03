package br.com.grupopipa.gestaointegrada.core.dao;

public enum DatabaseConstraintsEnum {

    DEFAULT("errors.internalServerError"),

    // Constraints de Usuario
    UK_USUARIO_LOGIN("usuario.login.unique"),

    // Constraints de Perfil
    UK_PERFIL_NOME("perfil.nome.unique"),

    // Constraints de Pessoa
    UK_PESSOA_FISICA_CPF("pessoaFisica.cpf.unique"),
    UK_PESSOA_JURIDICA_CNPJ("pessoaJuridica.cnpj.unique"),

    // Constraints de Unidade de Negócio
    UK_UNIDADE_NEGOCIO_CODIGO("unidadeNegocio.codigo.unique"),

    // Constraints de Plano de Contas
    UK_PLANO_CONTAS_CODIGO("planoContas.codigo.unique"),
    CHK_PLANO_CONTAS_TIPO("planoContas.tipo.invalid"),

    // Constraints de Conta Bancária
    CHK_CONTA_TIPO("contaBancaria.tipo.invalid"),

    // Constraints de Título
    CHK_TITULO_TIPO("titulo.tipo.invalid"),
    CHK_TITULO_STATUS("titulo.status.invalid"),
    CHK_TITULO_VALOR_ORIGINAL("titulo.valorOriginal.positive"),
    CHK_TITULO_VALOR_PAGO("titulo.valorPago.positive"),
    CHK_TITULO_VALOR_DESCONTO("titulo.valorDesconto.positive"),
    CHK_TITULO_VALOR_JUROS("titulo.valorJuros.positive"),
    CHK_TITULO_VALOR_MULTA("titulo.valorMulta.positive"),
    CHK_TITULO_DATAS("titulo.dataVencimento.afterEmissao"),
    CHK_TITULO_PARCELAMENTO("titulo.parcelamento.invalid"),

    // Constraints de Movimentação Financeira
    CHK_MOVIMENTACAO_TIPO("movimentacaoFinanceira.tipo.invalid"),
    CHK_MOVIMENTACAO_FORMA("movimentacaoFinanceira.formaPagamento.invalid"),
    CHK_MOVIMENTACAO_VALOR("movimentacaoFinanceira.valor.positive");

    String userMessageKey;

    DatabaseConstraintsEnum(String userMessageKey) {
        this.userMessageKey = userMessageKey;
    }

    public String getUserMessageKey() {
        return userMessageKey;
    }

    public static DatabaseConstraintsEnum getByKey(String key) {
        for (DatabaseConstraintsEnum constraint : values()) {
            if (constraint.name().equalsIgnoreCase(key)) {
                return constraint;
            }
        }
        return DEFAULT;
    }
}
