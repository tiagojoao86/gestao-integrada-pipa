-- Migration: create condicao_pagamento table for tenant schemas
-- Date: 2026-02-04

CREATE TABLE IF NOT EXISTS condicao_pagamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    condicao VARCHAR(100) NOT NULL,
    descricao VARCHAR(400),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    CONSTRAINT uk_condicao_pagamento_condicao UNIQUE (condicao)
);

CREATE INDEX IF NOT EXISTS idx_condicao_pagamento_deleted ON condicao_pagamento (deleted);

-- Criação do módulo para Condição de Pagamento (idempotente)
INSERT INTO
    modulo (id, chave, nome, grupo)
SELECT gen_random_uuid (), 'FINANCEIRO_CONDICAO_PAGAMENTO', 'Condição de Pagamento', 'FINANCEIRO'
WHERE
    NOT EXISTS (
        SELECT 1
        FROM modulo
        WHERE
            chave = 'FINANCEIRO_CONDICAO_PAGAMENTO'
    );

-- Vincular o módulo 'FINANCEIRO_CONDICAO_PAGAMENTO' ao perfil 'Administrador Geral' com todas as permissões (idempotente)
INSERT INTO
    perfil_modulo (
        id,
        perfil_id,
        modulo_id,
        pode_listar,
        pode_visualizar,
        pode_editar,
        pode_deletar,
        pode_auditar,
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
    TRUE,
    CURRENT_TIMESTAMP,
    'migration'
FROM perfil p
    CROSS JOIN modulo m
WHERE
    p.nome = 'Administrador Geral'
    AND m.chave = 'FINANCEIRO_CONDICAO_PAGAMENTO'
    AND NOT EXISTS (
        SELECT 1
        FROM perfil_modulo pm
        WHERE
            pm.perfil_id = p.id
            AND pm.modulo_id = m.id
    );
