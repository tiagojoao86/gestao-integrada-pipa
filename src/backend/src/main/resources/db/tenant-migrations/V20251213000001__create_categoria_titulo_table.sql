-- Migration: create categoria_titulo table for tenant schemas
-- Date: 2025-12-12 (moved version to run last)

CREATE TABLE IF NOT EXISTS categoria_titulo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(400),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Optional: create index on nome for faster searches
CREATE INDEX IF NOT EXISTS idx_categoria_titulo_nome ON categoria_titulo (nome);

-- Criação do módulo para Categoria de Títulos (idempotente)
INSERT INTO
    modulo (id, chave, nome, grupo)
SELECT gen_random_uuid (), 'FINANCEIRO_TITULO_CATEGORIA', 'Categoria de Títulos', 'FINANCEIRO'
WHERE
    NOT EXISTS (
        SELECT 1
        FROM modulo
        WHERE
            chave = 'FINANCEIRO_TITULO_CATEGORIA'
    );

-- Vincular o módulo 'FINANCEIRO_TITULO_CATEGORIA' ao perfil 'Administrador Geral' com todas as permissões (idempotente)
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
    AND m.chave = 'FINANCEIRO_TITULO_CATEGORIA'
    AND NOT EXISTS (
        SELECT 1
        FROM perfil_modulo pm
        WHERE
            pm.perfil_id = p.id
            AND pm.modulo_id = m.id
    );