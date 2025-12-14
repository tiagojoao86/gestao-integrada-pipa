-- Criação das tabelas de estrutura financeira
CREATE TABLE plano_contas (
    id UUID NOT NULL DEFAULT gen_random_uuid (),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    codigo VARCHAR(20) NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    plano_pai_id UUID,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    unidade_negocio_id UUID NOT NULL,
    CONSTRAINT pk_plano_contas PRIMARY KEY (id),
    CONSTRAINT fk_plano_contas_pai FOREIGN KEY (plano_pai_id) REFERENCES plano_contas (id),
    CONSTRAINT uk_plano_contas_codigo UNIQUE (codigo),
    CONSTRAINT chk_plano_contas_tipo CHECK (
        tipo IN (
            'RECEITA',
            'DESPESA',
            'ATIVO',
            'PASSIVO'
        )
    ),
    CONSTRAINT fk_plano_contas_unidade FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio (id)
);

COMMENT ON
TABLE plano_contas IS 'Plano de contas com estrutura hierárquica.';

COMMENT ON COLUMN plano_contas.unidade_negocio_id IS 'Unidade de negócio à qual o plano de contas pertence (obrigatório)';

CREATE INDEX idx_plano_contas_codigo ON plano_contas (codigo);

CREATE INDEX idx_plano_contas_tipo ON plano_contas (tipo);

CREATE INDEX idx_plano_contas_pai ON plano_contas (plano_pai_id);

CREATE INDEX idx_plano_contas_ativo ON plano_contas (ativo);

CREATE INDEX idx_plano_contas_unidade_negocio ON plano_contas (unidade_negocio_id);

-- Tabela Conta Bancária
CREATE TABLE conta_bancaria (
    id UUID NOT NULL DEFAULT gen_random_uuid (),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    nome VARCHAR(100) NOT NULL,
    banco VARCHAR(100),
    agencia VARCHAR(10),
    numero_conta VARCHAR(20),
    tipo VARCHAR(20) NOT NULL,
    saldo_inicial NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    unidade_negocio_id UUID NOT NULL,
    CONSTRAINT pk_conta_bancaria PRIMARY KEY (id),
    CONSTRAINT fk_conta_bancaria_unidade FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio (id),
    CONSTRAINT chk_conta_tipo CHECK (
        tipo IN (
            'CORRENTE',
            'POUPANCA',
            'CAIXA',
            'INVESTIMENTO'
        )
    )
);

CREATE INDEX idx_conta_bancaria_unidade_negocio ON conta_bancaria (unidade_negocio_id);

COMMENT ON
TABLE conta_bancaria IS 'Contas bancárias e caixas da organização.';

CREATE INDEX idx_conta_bancaria_nome ON conta_bancaria (nome);

CREATE INDEX idx_conta_bancaria_tipo ON conta_bancaria (tipo);

CREATE INDEX idx_conta_bancaria_ativa ON conta_bancaria (ativa);

-- Criação dos módulos no sistema de permissões
INSERT INTO
    modulo (id, chave, nome, grupo)
VALUES (
        gen_random_uuid (),
        'FINANCEIRO_PLANO_CONTAS',
        'Plano de Contas',
        'FINANCEIRO'
    ),
    (
        gen_random_uuid (),
        'FINANCEIRO_CONTA_BANCARIA',
        'Contas Bancárias',
        'FINANCEIRO'
    );

-- Vincular módulos ao perfil 'Administrador Geral' com todas as permissões
INSERT INTO
    perfil_modulo (
        id,
        perfil_id,
        modulo_id,
        pode_listar,
        pode_visualizar,
        pode_editar,
        pode_deletar,
        created_at,
        created_by
    )
SELECT
    gen_random_uuid (),
    p.id,
    m.id,
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    'migration'
FROM perfil p
    CROSS JOIN modulo m
WHERE
    p.nome = 'Administrador Geral'
    AND m.chave IN (
        'FINANCEIRO_PLANO_CONTAS',
        'FINANCEIRO_CONTA_BANCARIA'
    );