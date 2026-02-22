package br.com.grupopipa.gestaointegrada.core.dao;

/**
 * Enum que mapeia nomes de constraints do banco de dados para mensagens de erro
 * legíveis pelo usuário.
 *
 * <p>
 * Quando uma constraint é violada, o RestExceptionHandler captura a exceção,
 * busca o nome da constraint neste enum e retorna a mensagem correspondente
 * diretamente na resposta HTTP.
 *
 * <p>
 * IMPORTANTE: Sempre que criar uma constraint na migration, adicione-a aqui!
 */
public enum DatabaseConstraintsEnum {
    DEFAULT("errors.internalServerError", "Erro interno do servidor."),

    // ========================================
    // MÓDULO CADASTRO
    // ========================================

    // -------- Usuario --------
    UK_USUARIO_LOGIN("usuario.login.unique", "Este login já está cadastrado."),
    FK_USUARIO_PERFIL_USUARIO("usuarioPerfil.usuario.foreignKey", "Usuário inválido."),
    FK_USUARIO_PERFIL_PERFIL("usuarioPerfil.perfil.foreignKey", "Perfil inválido."),

    // -------- Usuario Unidade Negocio --------
    UK_USUARIO_UNIDADE_NEGOCIO("usuario.unidadeNegocio.unique",
            "Este usuário já está vinculado a esta unidade de negócio."),
    FK_USUARIO_UNIDADE_NEGOCIO_USUARIO("usuarioUnidadeNegocio.usuario.foreignKey", "Usuário inválido."),
    FK_USUARIO_UNIDADE_NEGOCIO_UNIDADE("usuarioUnidadeNegocio.unidadeNegocio.foreignKey",
            "Unidade de negócio inválida."),

    // -------- Perfil --------
    UK_PERFIL_NOME("perfil.nome.unique", "Já existe um perfil com este nome."),
    FK_PERFIL_MODULO_PERFIL("perfilModulo.perfil.foreignKey", "Perfil inválido."),
    FK_PERFIL_MODULO_MODULO("perfilModulo.modulo.foreignKey", "Módulo inválido."),

    // -------- Pessoa --------
    UK_PESSOA_CPF("pessoa.cpf.unique", "Este CPF já está cadastrado."),
    UK_PESSOA_CNPJ("pessoa.cnpj.unique", "Este CNPJ já está cadastrado."),
    UK_PESSOA_FISICA_CPF("pessoaFisica.cpf.unique", "Este CPF já está cadastrado."),
    UK_PESSOA_JURIDICA_CNPJ("pessoaJuridica.cnpj.unique", "Este CNPJ já está cadastrado."),

    // -------- Unidade de Negócio --------
    UK_UNIDADE_NEGOCIO_CODIGO("unidadeNegocio.codigo.unique", "Este código já está cadastrado."),

    // -------- Setor --------
    UK_SETOR_NOME("setor.nome.unique", "Já existe um setor com este nome."),
    FK_SETOR_CENTRO_CUSTO("setor.centroCusto.foreignKey", "Centro de custo inválido."),

    // ========================================
    // MÓDULO FINANCEIRO
    // ========================================

    // -------- Plano de Contas --------
    UK_PLANO_CONTAS_CODIGO("planoContas.codigo.unique", "Este código já está cadastrado."),
    CHK_PLANO_CONTAS_TIPO("planoContas.tipo.invalid", "Tipo de plano de contas inválido."),
    FK_PLANO_CONTAS_PAI("planoContas.pai.foreignKey", "Plano de contas pai inválido."),
    FK_PLANO_CONTAS_UNIDADE_NEGOCIO("planoContas.unidadeNegocio.foreignKey", "Unidade de negócio inválida."),

    // -------- Conta Bancária --------
    CHK_CONTA_TIPO("contaBancaria.tipo.invalid", "Tipo de conta bancária inválido."),
    FK_CONTA_BANCARIA_UNIDADE_NEGOCIO("contaBancaria.unidadeNegocio.foreignKey", "Unidade de negócio inválida."),

    // -------- Título --------
    CHK_TITULO_TIPO("titulo.tipo.invalid", "Tipo de título inválido."),
    CHK_TITULO_STATUS("titulo.status.invalid", "Status de título inválido."),
    CHK_TITULO_VALOR_ORIGINAL("titulo.valorOriginal.positive", "O valor original deve ser positivo."),
    CHK_TITULO_VALOR_PAGO("titulo.valorPago.positive", "O valor pago deve ser positivo."),
    CHK_TITULO_VALOR_DESCONTO("titulo.valorDesconto.positive", "O valor de desconto deve ser positivo."),
    CHK_TITULO_VALOR_JUROS("titulo.valorJuros.positive", "O valor de juros deve ser positivo."),
    CHK_TITULO_VALOR_MULTA("titulo.valorMulta.positive", "O valor de multa deve ser positivo."),
    CHK_TITULO_DATAS("titulo.dataVencimento.afterEmissao",
            "A data de vencimento deve ser posterior à data de emissão."),
    CHK_TITULO_PARCELAMENTO("titulo.parcelamento.invalid", "Configuração de parcelamento inválida."),
    FK_TITULO_PESSOA("titulo.pessoa.foreignKey", "Pessoa inválida."),
    FK_TITULO_PLANO_CONTAS("titulo.planoContas.foreignKey", "Plano de contas inválido."),
    FK_TITULO_UNIDADE_NEGOCIO("titulo.unidadeNegocio.foreignKey", "Unidade de negócio inválida."),
    FK_TITULO_ORIGEM("titulo.origem.foreignKey", "Título de origem inválido."),
    FK_TITULO_CONDICAO_PAGAMENTO("titulo.condicaoPagamento.foreignKey", "Condição de pagamento inválida."),

    // -------- Título Categoria --------
    UK_TITULO_CATEGORIA_CODIGO("tituloCategoria.codigo.unique", "Este código já está cadastrado."),
    UK_TITULO_CATEGORIA_NOME("tituloCategoria.nome.unique", "Já existe uma categoria com este nome."),
    CHK_TITULO_CATEGORIA_TIPO("tituloCategoria.tipo.invalid", "Tipo de categoria inválido."),
    FK_TITULO_CATEGORIA_AGRUPADOR("tituloCategoria.agrupador.foreignKey", "Categoria agrupadora inválida."),

    // -------- Condição de Pagamento --------
    UK_CONDICAO_PAGAMENTO_CONDICAO("condicaoPagamento.condicao.unique",
            "Já existe uma condição de pagamento com esta descrição."),

    // -------- Centro de Custo --------
    UK_CENTRO_CUSTO_NOME("centroCusto.nome.unique", "Já existe um centro de custo com este nome."),
    FK_CENTRO_CUSTO_UNIDADE_NEGOCIO("centroCusto.unidadeNegocio.foreignKey", "Unidade de negócio inválida."),

    // -------- Movimentação Financeira --------
    CHK_MOVIMENTACAO_TIPO("movimentacaoFinanceira.tipo.invalid", "Tipo de movimentação inválido."),
    CHK_MOVIMENTACAO_FORMA("movimentacaoFinanceira.formaPagamento.invalid", "Forma de pagamento inválida."),
    CHK_MOVIMENTACAO_VALOR("movimentacaoFinanceira.valor.positive", "O valor da movimentação deve ser positivo."),
    FK_MOVIMENTACAO_FINANCEIRA_UNIDADE_NEGOCIO("movimentacaoFinanceira.unidadeNegocio.foreignKey",
            "Unidade de negócio inválida."),
    FK_MOVIMENTACAO_TITULO("movimentacaoFinanceira.titulo.foreignKey", "Título inválido."),
    FK_MOVIMENTACAO_CONTA("movimentacaoFinanceira.contaBancaria.foreignKey", "Conta bancária inválida."),
    FK_MOVIMENTACAO_TITULO_MOV("movimentacaoFinanceira.titulo.foreignKey", "Título inválido."),
    FK_MOVIMENTACAO_TITULO_TIT("movimentacaoFinanceira.titulo.foreignKey", "Título inválido.");

    /** @deprecated Use {@link #message} instead. Will be removed in a future version. */
    @Deprecated
    private final String userMessageKey;
    private final String message;

    DatabaseConstraintsEnum(String userMessageKey, String message) {
        this.userMessageKey = userMessageKey;
        this.message = message;
    }

    /** @deprecated Use {@link #getMessage()} instead. Will be removed in a future version. */
    @Deprecated
    public String getUserMessageKey() {
        return userMessageKey;
    }

    public String getMessage() {
        return message;
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
