package br.com.grupopipa.gestaointegrada.core.dao;

/**
 * Enum que mapeia nomes de constraints do banco de dados para chaves de
 * mensagem i18n.
 *
 * <p>
 * Quando uma constraint é violada, o RestExceptionHandler captura a exceção,
 * busca o nome da
 * constraint neste enum e retorna a userMessageKey correspondente para que o
 * frontend exiba uma
 * mensagem traduzida ao usuário.
 *
 * <p>
 * IMPORTANTE: Sempre que criar uma constraint na migration, adicione-a aqui!
 */
public enum DatabaseConstraintsEnum {
    DEFAULT("errors.internalServerError"),

    // ========================================
    // MÓDULO CADASTRO
    // ========================================

    // -------- Usuario --------
    UK_USUARIO_LOGIN("usuario.login.unique"),
    FK_USUARIO_PERFIL_USUARIO("usuarioPerfil.usuario.foreignKey"),
    FK_USUARIO_PERFIL_PERFIL("usuarioPerfil.perfil.foreignKey"),

    // -------- Usuario Unidade Negocio --------
    UK_USUARIO_UNIDADE_NEGOCIO("usuario.unidadeNegocio.unique"),
    FK_USUARIO_UNIDADE_NEGOCIO_USUARIO("usuarioUnidadeNegocio.usuario.foreignKey"),
    FK_USUARIO_UNIDADE_NEGOCIO_UNIDADE("usuarioUnidadeNegocio.unidadeNegocio.foreignKey"),

    // -------- Perfil --------
    UK_PERFIL_NOME("perfil.nome.unique"),
    FK_PERFIL_MODULO_PERFIL("perfilModulo.perfil.foreignKey"),
    FK_PERFIL_MODULO_MODULO("perfilModulo.modulo.foreignKey"),

    // -------- Pessoa --------
    UK_PESSOA_CPF("pessoa.cpf.unique"),
    UK_PESSOA_CNPJ("pessoa.cnpj.unique"),
    UK_PESSOA_FISICA_CPF("pessoaFisica.cpf.unique"),
    UK_PESSOA_JURIDICA_CNPJ("pessoaJuridica.cnpj.unique"),

    // -------- Unidade de Negócio --------
    UK_UNIDADE_NEGOCIO_CODIGO("unidadeNegocio.codigo.unique"),

    // -------- Setor --------
    UK_SETOR_NOME("setor.nome.unique"),
    FK_SETOR_CENTRO_CUSTO("setor.centroCusto.foreignKey"),

    // ========================================
    // MÓDULO FINANCEIRO
    // ========================================

    // -------- Plano de Contas --------
    UK_PLANO_CONTAS_CODIGO("planoContas.codigo.unique"),
    CHK_PLANO_CONTAS_TIPO("planoContas.tipo.invalid"),
    FK_PLANO_CONTAS_PAI("planoContas.pai.foreignKey"),
    FK_PLANO_CONTAS_UNIDADE_NEGOCIO("planoContas.unidadeNegocio.foreignKey"),

    // -------- Conta Bancária --------
    CHK_CONTA_TIPO("contaBancaria.tipo.invalid"),
    FK_CONTA_BANCARIA_UNIDADE_NEGOCIO("contaBancaria.unidadeNegocio.foreignKey"),

    // -------- Título --------
    CHK_TITULO_TIPO("titulo.tipo.invalid"),
    CHK_TITULO_STATUS("titulo.status.invalid"),
    CHK_TITULO_VALOR_ORIGINAL("titulo.valorOriginal.positive"),
    CHK_TITULO_VALOR_PAGO("titulo.valorPago.positive"),
    CHK_TITULO_VALOR_DESCONTO("titulo.valorDesconto.positive"),
    CHK_TITULO_VALOR_JUROS("titulo.valorJuros.positive"),
    CHK_TITULO_VALOR_MULTA("titulo.valorMulta.positive"),
    CHK_TITULO_DATAS("titulo.dataVencimento.afterEmissao"),
    CHK_TITULO_PARCELAMENTO("titulo.parcelamento.invalid"),
    FK_TITULO_PESSOA("titulo.pessoa.foreignKey"),
    FK_TITULO_PLANO_CONTAS("titulo.planoContas.foreignKey"),
    FK_TITULO_UNIDADE_NEGOCIO("titulo.unidadeNegocio.foreignKey"),
    FK_TITULO_ORIGEM("titulo.origem.foreignKey"),

    // -------- Título Categoria --------
    UK_TITULO_CATEGORIA_CODIGO("tituloCategoria.codigo.unique"),
    UK_TITULO_CATEGORIA_NOME("tituloCategoria.nome.unique"),
    CHK_TITULO_CATEGORIA_TIPO("tituloCategoria.tipo.invalid"),
    FK_TITULO_CATEGORIA_AGRUPADOR("tituloCategoria.agrupador.foreignKey"),

    // -------- Centro de Custo --------
    UK_CENTRO_CUSTO_NOME("centroCusto.nome.unique"),
    FK_CENTRO_CUSTO_UNIDADE_NEGOCIO("centroCusto.unidadeNegocio.foreignKey"),

    // -------- Movimentação Financeira --------
    CHK_MOVIMENTACAO_TIPO("movimentacaoFinanceira.tipo.invalid"),
    CHK_MOVIMENTACAO_FORMA("movimentacaoFinanceira.formaPagamento.invalid"),
    CHK_MOVIMENTACAO_VALOR("movimentacaoFinanceira.valor.positive"),
    FK_MOVIMENTACAO_FINANCEIRA_UNIDADE_NEGOCIO("movimentacaoFinanceira.unidadeNegocio.foreignKey"),
    FK_MOVIMENTACAO_TITULO("movimentacaoFinanceira.titulo.foreignKey"),
    FK_MOVIMENTACAO_CONTA("movimentacaoFinanceira.contaBancaria.foreignKey"),
    FK_MOVIMENTACAO_TITULO_MOV("movimentacaoFinanceira.titulo.foreignKey"),
    FK_MOVIMENTACAO_TITULO_TIT("movimentacaoFinanceira.titulo.foreignKey");

    private final String userMessageKey;

    DatabaseConstraintsEnum(String userMessageKey) {
        this.userMessageKey = userMessageKey;
    }

    public String getUserMessageKey() {
        return userMessageKey;
    }

    /**
     * Busca a constraint pelo nome (case-insensitive). Retorna DEFAULT se não
     * encontrar.
     *
     * @param key Nome da constraint do banco de dados
     * @return Enum correspondente ou DEFAULT
     */
    public static DatabaseConstraintsEnum getByKey(String key) {
        if (key == null) {
            return DEFAULT;
        }

        for (DatabaseConstraintsEnum constraint : values()) {
            if (constraint.name().equalsIgnoreCase(key)) {
                return constraint;
            }
        }

        return DEFAULT;
    }
}
