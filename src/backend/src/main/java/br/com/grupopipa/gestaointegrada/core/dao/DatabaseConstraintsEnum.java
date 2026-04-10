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
    DEFAULT("Erro interno do servidor."),

    // ========================================
    // MÓDULO CADASTRO
    // ========================================

    // -------- Usuario --------
    UK_USUARIO_LOGIN("Este login já está cadastrado."),
    FK_USUARIO_PERFIL_USUARIO("Usuário inválido."),
    FK_USUARIO_PERFIL_PERFIL("Perfil inválido."),

    // -------- Usuario Unidade Negocio --------
    UK_USUARIO_UNIDADE_NEGOCIO("Este usuário já está vinculado a esta unidade de negócio."),
    FK_USUARIO_UNIDADE_NEGOCIO_USUARIO("Usuário inválido."),
    FK_USUARIO_UNIDADE_NEGOCIO_UNIDADE("Unidade de negócio inválida."),

    // -------- Perfil --------
    UK_PERFIL_NOME("Já existe um perfil com este nome."),
    FK_PERFIL_MODULO_PERFIL("Perfil inválido."),
    FK_PERFIL_MODULO_MODULO("Módulo inválido."),

    // -------- Pessoa --------
    UK_PESSOA_CPF("Este CPF já está cadastrado."),
    UK_PESSOA_CNPJ("Este CNPJ já está cadastrado."),
    UK_PESSOA_FISICA_CPF("Este CPF já está cadastrado."),
    UK_PESSOA_JURIDICA_CNPJ("Este CNPJ já está cadastrado."),
    FK_PESSOA_RESPONSAVEL("Responsável inválido."),

    // -------- Unidade de Negócio --------
    UK_UNIDADE_NEGOCIO_CODIGO("Este código já está cadastrado."),

    // -------- Setor --------
    UK_SETOR_NOME("Já existe um setor com este nome."),
    FK_SETOR_CENTRO_CUSTO("Centro de custo inválido."),

    // ========================================
    // MÓDULO FINANCEIRO
    // ========================================

    // -------- Plano de Contas --------
    UK_PLANO_CONTAS_CODIGO("Este código já está cadastrado."),
    CHK_PLANO_CONTAS_TIPO("Tipo de plano de contas inválido."),
    FK_PLANO_CONTAS_PAI("Plano de contas pai inválido."),
    FK_PLANO_CONTAS_UNIDADE_NEGOCIO("Unidade de negócio inválida."),

    // -------- Conta Bancária --------
    CHK_CONTA_TIPO("Tipo de conta bancária inválido."),
    FK_CONTA_BANCARIA_UNIDADE_NEGOCIO("Unidade de negócio inválida."),

    // -------- Título --------
    CHK_TITULO_TIPO("Tipo de título inválido."),
    CHK_TITULO_STATUS("Status de título inválido."),
    CHK_TITULO_VALOR_ORIGINAL("O valor original deve ser positivo."),
    CHK_TITULO_VALOR_PAGO("O valor pago deve ser positivo."),
    CHK_TITULO_VALOR_DESCONTO("O valor de desconto deve ser positivo."),
    CHK_TITULO_VALOR_JUROS("O valor de juros deve ser positivo."),
    CHK_TITULO_VALOR_MULTA("O valor de multa deve ser positivo."),
    CHK_TITULO_DATAS("A data de vencimento deve ser posterior à data de emissão."),
    CHK_TITULO_PARCELAMENTO("Configuração de parcelamento inválida."),
    FK_TITULO_PESSOA("Pessoa inválida."),
    FK_TITULO_PLANO_CONTAS("Plano de contas inválido."),
    FK_TITULO_UNIDADE_NEGOCIO("Unidade de negócio inválida."),
    FK_TITULO_ORIGEM("Título de origem inválido."),
    FK_TITULO_CONDICAO_PAGAMENTO("Condição de pagamento inválida."),

    // -------- Título Categoria --------
    UK_TITULO_CATEGORIA_CODIGO("Este código já está cadastrado."),
    UK_TITULO_CATEGORIA_NOME("Já existe uma categoria com este nome."),
    CHK_TITULO_CATEGORIA_TIPO("Tipo de categoria inválido."),
    FK_TITULO_CATEGORIA_AGRUPADOR("Categoria agrupadora inválida."),

    // -------- Condição de Pagamento --------
    UK_CONDICAO_PAGAMENTO_CONDICAO("Já existe uma condição de pagamento com esta descrição."),

    // -------- Centro de Custo --------
    UK_CENTRO_CUSTO_NOME("Já existe um centro de custo com este nome."),
    FK_CENTRO_CUSTO_UNIDADE_NEGOCIO("Unidade de negócio inválida."),

    // -------- Movimentação Financeira --------
    CHK_MOVIMENTACAO_TIPO("Tipo de movimentação inválido."),
    CHK_MOVIMENTACAO_FORMA("Forma de pagamento inválida."),
    CHK_MOVIMENTACAO_VALOR("O valor da movimentação deve ser positivo."),
    FK_MOVIMENTACAO_FINANCEIRA_UNIDADE_NEGOCIO("Unidade de negócio inválida."),
    FK_MOVIMENTACAO_TITULO("Título inválido."),
    FK_MOVIMENTACAO_CONTA("Conta bancária inválida."),
    FK_MOVIMENTACAO_TITULO_MOV("Título inválido."),
    FK_MOVIMENTACAO_TITULO_TIT("Título inválido."),
    UK_MOVIMENTACAO_FINANCEIRA_TITULO("Este título já está associado a esta movimentação."),

    // ========================================
    // MÓDULO ATENDIMENTO
    // ========================================

    // -------- Profissional --------
    FK_PROFISSIONAL_PESSOA("Pessoa inválida."),
    UK_PROFISSIONAL_PESSOA("Esta pessoa já está cadastrada como profissional."),

    // -------- Convênio --------
    UK_CONVENIO_NOME("Já existe um convênio com este nome."),
    UK_CONVENIO_REGISTRO_ANS("Este registro ANS já está cadastrado."),
    FK_CONVENIO_PESSOA("Pessoa inválida."),

    // -------- Convenio Categoria --------
    FK_CONVENIO_CATEGORIA_CONVENIO("Convênio inválido."),
    UK_CONVENIO_CATEGORIA_NOME_CONVENIO("Já existe uma categoria com este nome neste convênio."),

    // -------- Procedimento --------
    UK_PROCEDIMENTO_CODIGO("Já existe um procedimento com este código."),

    // -------- Código Convênio --------
    FK_CODIGO_CONVENIO_CONVENIO("Convênio inválido."),
    FK_CODIGO_CONVENIO_PROCEDIMENTO("Procedimento inválido."),
    UK_CODIGO_CONVENIO_CONVENIO_PROCEDIMENTO("Este procedimento já possui um código cadastrado para este convênio."),

    // -------- Tabela --------
    UK_TABELA_NOME("Já existe uma tabela com este nome."),

    // -------- Tabela Item --------
    FK_TABELA_ITEM_TABELA("Tabela inválida."),
    FK_TABELA_ITEM_PROCEDIMENTO("Procedimento inválido."),
    UK_TABELA_ITEM_TABELA_PROCEDIMENTO_INICIO("Já existe um item ativo para este procedimento nesta tabela."),

    // -------- Atendimento --------
    FK_ATENDIMENTO_SETOR("Setor inválido."),
    FK_ATENDIMENTO_PACIENTE("Paciente inválido."),
    FK_ATENDIMENTO_RESPONSAVEL("Responsável inválido."),
    FK_ATENDIMENTO_CONVENIO("Convênio inválido."),
    FK_ATENDIMENTO_CONVENIO_CATEGORIA("Categoria de convênio inválida."),
    FK_ATENDIMENTO_PROF_ATENDIMENTO("Profissional de atendimento inválido."),
    FK_ATENDIMENTO_PROF_RESPONSAVEL("Profissional responsável inválido."),
    FK_ATENDIMENTO_PROCEDIMENTO("Procedimento inválido."),
    FK_ATENDIMENTO_TABELA_ITEM("Item de tabela inválido."),
    CK_ATENDIMENTO_STATUS("Status de atendimento inválido.");

    private final String message;

    DatabaseConstraintsEnum(String message) {
        this.message = message;
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
