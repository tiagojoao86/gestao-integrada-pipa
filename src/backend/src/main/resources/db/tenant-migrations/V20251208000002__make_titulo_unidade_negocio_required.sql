-- Migration: Tornar unidade_negocio_id obrigatória em titulo
-- Author: Sistema
-- Date: 2025-12-08
-- Description: Altera a coluna unidade_negocio_id para NOT NULL na tabela titulo

-- Primeiro, cria uma unidade de negócio padrão caso não exista nenhuma
INSERT INTO
    unidade_negocio (
        id,
        codigo,
        nome,
        descricao,
        cnpj,
        ativa,
        created_at,
        created_by
    )
VALUES (
        '019b010e-6348-74cb-acf7-2aebca002b44',
        'UNIDADE1',
        'Unidade 1 (Padrão Sistema)',
        'Unidade Padrão do Sistema, favor alterar no primeiro acesso',
        '22076133000184',
        TRUE,
        CURRENT_TIMESTAMP,
        'migration'
    ) ON CONFLICT (id) DO NOTHING;

-- Atualiza os títulos que não têm unidade definida com a unidade padrão
UPDATE titulo
SET
    unidade_negocio_id = '019b010e-6348-74cb-acf7-2aebca002b44'
WHERE
    unidade_negocio_id IS NULL;

-- Agora altera a coluna para NOT NULL
ALTER TABLE titulo ALTER COLUMN unidade_negocio_id SET NOT NULL;

-- Adiciona comentário
COMMENT ON COLUMN titulo.unidade_negocio_id IS 'Unidade de negócio à qual o título pertence (obrigatório)';