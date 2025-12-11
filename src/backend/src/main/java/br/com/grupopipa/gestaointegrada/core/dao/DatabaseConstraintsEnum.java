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
    CHK_MOVIMENTACAO_VALOR("movimentacaoFinanceira.valor.positive"),

    // Constraints adicionais encontradas nas migrations (FK / UK)
    FK_MOVIMENTACAO_FINANCEIRA_UNIDADE_NEGOCIO("movimentacaoFinanceira.unidadeNegocio"),
    FK_PLANO_CONTAS_UNIDADE_NEGOCIO("planoContas.unidadeNegocio"),
    FK_CONTA_BANCARIA_UNIDADE_NEGOCIO("contaBancaria.unidadeNegocio"),

    UK_USUARIO_UNIDADE_NEGOCIO("usuario.unidadeNegocio.unique"),
    FK_USUARIO_UNIDADE_NEGOCIO_USUARIO("usuarioUnidadeNegocio.usuario.foreignKey"),
    FK_USUARIO_UNIDADE_NEGOCIO_UNIDADE("usuarioUnidadeNegocio.unidadeNegocio.foreignKey"),

    FK_TITULO_PESSOA("titulo.pessoa.foreignKey"),
    FK_TITULO_PLANO_CONTAS("titulo.planoContas.foreignKey"),
    FK_TITULO_UNIDADE_NEGOCIO("titulo.unidadeNegocio.foreignKey"),
    FK_TITULO_ORIGEM("titulo.origem.foreignKey"),

    FK_MOVIMENTACAO_TITULO("movimentacaoFinanceira.titulo.foreignKey"),
    FK_MOVIMENTACAO_CONTA("movimentacaoFinanceira.contaBancaria.foreignKey"),
    FK_MOVIMENTACAO_TITULO_MOV("movimentacaoFinanceira.titulo.foreignKey"),
    FK_MOVIMENTACAO_TITULO_TIT("movimentacaoFinanceira.titulo.foreignKey"),

    FK_PLANO_CONTAS_PAI("planoContas.pai.foreignKey"),

    FK_USUARIO_PERFIL_USUARIO("usuarioPerfil.usuario.foreignKey"),
    FK_USUARIO_PERFIL_PERFIL("usuarioPerfil.perfil.foreignKey"),
    FK_PERFIL_MODULO_PERFIL("perfilModulo.perfil.foreignKey"),
    FK_PERFIL_MODULO_MODULO("perfilModulo.modulo.foreignKey"),

    UK_PESSOA_CPF("pessoa.cpf.unique"),
    UK_PESSOA_CNPJ("pessoa.cnpj.unique");

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
