-- Criação das tabelas de títulos e movimentações financeiras
CREATE TABLE titulo (
    id UUID NOT NULL DEFAULT gen_random_uuid (),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tipo VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(50),
    descricao VARCHAR(500) NOT NULL,
    pessoa_id UUID NOT NULL,
    unidade_negocio_id UUID NOT NULL,
    valor_original NUMERIC(15, 2) NOT NULL,
    valor_pago NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_juros NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_multa NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    data_emissao DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    data_pagamento DATE,
    observacoes TEXT,
    numero_parcela INTEGER,
    total_parcelas INTEGER,
    titulo_origem_id UUID,
    CONSTRAINT pk_titulo PRIMARY KEY (id),
    CONSTRAINT fk_titulo_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id),
    CONSTRAINT fk_titulo_unidade_negocio FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio (id),
    CONSTRAINT fk_titulo_origem FOREIGN KEY (titulo_origem_id) REFERENCES titulo (id),
    CONSTRAINT chk_titulo_tipo CHECK (
        tipo IN ('A_PAGAR', 'A_RECEBER')
    ),
    CONSTRAINT chk_titulo_status CHECK (
        status IN (
            'ABERTO',
            'PARCIAL',
            'PAGO',
            'CANCELADO',
            'VENCIDO'
        )
    ),
    CONSTRAINT chk_titulo_valor_original CHECK (valor_original > 0),
    CONSTRAINT chk_titulo_valor_pago CHECK (valor_pago >= 0),
    CONSTRAINT chk_titulo_valor_desconto CHECK (valor_desconto >= 0),
    CONSTRAINT chk_titulo_valor_juros CHECK (valor_juros >= 0),
    CONSTRAINT chk_titulo_valor_multa CHECK (valor_multa >= 0),
    CONSTRAINT chk_titulo_datas CHECK (
        data_vencimento >= data_emissao
    ),
    CONSTRAINT chk_titulo_parcelamento CHECK (
        (
            numero_parcela IS NULL
            AND total_parcelas IS NULL
            AND titulo_origem_id IS NULL
        )
        OR (
            numero_parcela IS NOT NULL
            AND total_parcelas IS NOT NULL
            AND numero_parcela <= total_parcelas
        )
    )
);

COMMENT ON
TABLE titulo IS 'Títulos financeiros - regime de competência (compromissos a pagar/receber).';

-- Índices para melhorar performance
CREATE INDEX idx_titulo_tipo_status ON titulo (tipo, status);

CREATE INDEX idx_titulo_vencimento ON titulo (data_vencimento);

CREATE INDEX idx_titulo_emissao ON titulo (data_emissao);

CREATE INDEX idx_titulo_pessoa ON titulo (pessoa_id);

CREATE INDEX idx_titulo_unidade_negocio ON titulo (unidade_negocio_id);

CREATE INDEX idx_titulo_numero_documento ON titulo (numero_documento);

CREATE INDEX idx_titulo_origem ON titulo (titulo_origem_id);

-- Tabela Movimentação Financeira
CREATE TABLE movimentacao_financeira (
    id UUID NOT NULL DEFAULT gen_random_uuid (),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    conta_bancaria_id UUID NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    forma_pagamento VARCHAR(20) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    data DATE NOT NULL,
    observacoes TEXT,
    unidade_negocio_id UUID NOT NULL,
    CONSTRAINT pk_movimentacao_financeira PRIMARY KEY (id),
    CONSTRAINT fk_movimentacao_conta FOREIGN KEY (conta_bancaria_id) REFERENCES conta_bancaria (id),
    CONSTRAINT chk_movimentacao_tipo CHECK (
        tipo IN (
            'PAGAMENTO',
            'RECEBIMENTO',
            'ESTORNO',
            'TRANSFERENCIA'
        )
    ),
    CONSTRAINT chk_movimentacao_forma CHECK (
        forma_pagamento IN (
            'PIX',
            'DINHEIRO',
            'BOLETO',
            'CARTAO_CREDITO',
            'CARTAO_DEBITO',
            'TED',
            'DOC',
            'CHEQUE',
            'DEPOSITO'
        )
    ),
    CONSTRAINT chk_movimentacao_valor CHECK (valor > 0),
    CONSTRAINT fk_movimentacao_unidade FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio (id)
);

COMMENT ON
TABLE movimentacao_financeira IS 'Movimentações financeiras - regime de caixa (dinheiro real nas contas).';

CREATE INDEX idx_movimentacao_data ON movimentacao_financeira (data);

CREATE INDEX idx_movimentacao_conta ON movimentacao_financeira (conta_bancaria_id);

CREATE INDEX idx_movimentacao_tipo ON movimentacao_financeira (tipo);

CREATE INDEX idx_movimentacao_forma ON movimentacao_financeira (forma_pagamento);

-- Cria índice para melhorar performance de queries por unidade de negócio
CREATE INDEX idx_movimentacao_financeira_unidade_negocio ON movimentacao_financeira (unidade_negocio_id);

-- Comentário na coluna
COMMENT ON COLUMN movimentacao_financeira.unidade_negocio_id IS 'Unidade de negócio responsável pela movimentação (obrigatório)';

-- Migration: Many-to-Many MovimentacaoFinanceira <-> Titulo
CREATE TABLE movimentacao_financeira_titulo (
    movimentacao_financeira_id UUID NOT NULL,
    titulo_id UUID NOT NULL,
    PRIMARY KEY (
        movimentacao_financeira_id,
        titulo_id
    ),
    CONSTRAINT fk_movimentacao_titulo_mov FOREIGN KEY (movimentacao_financeira_id) REFERENCES movimentacao_financeira (id) ON DELETE CASCADE,
    CONSTRAINT fk_movimentacao_titulo_tit FOREIGN KEY (titulo_id) REFERENCES titulo (id) ON DELETE CASCADE
);

-- Indexes para performance
CREATE INDEX idx_movimentacao_titulo_mov ON movimentacao_financeira_titulo (movimentacao_financeira_id);

CREATE INDEX idx_movimentacao_titulo_tit ON movimentacao_financeira_titulo (titulo_id);

-- Criação dos módulos no sistema de permissões
INSERT INTO
    modulo (id, chave, nome, grupo)
VALUES (
        gen_random_uuid (),
        'FINANCEIRO_TITULO',
        'Títulos Financeiros',
        'FINANCEIRO'
    ),
    (
        gen_random_uuid (),
        'FINANCEIRO_MOVIMENTACAO_FINANCEIRA',
        'Movimentações Financeiras',
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
        'FINANCEIRO_TITULO',
        'FINANCEIRO_MOVIMENTACAO_FINANCEIRA'
    );