-- Migration: Adicionar coluna unidade_negocio_id em movimentacao_financeira
-- Author: Sistema
-- Date: 2025-12-10
-- Description: Adiciona coluna unidade_negocio_id, popula registros existentes,
-- torna NOT NULL, adiciona foreign key e índice.

-- Adiciona a coluna permitindo NULL temporariamente
ALTER TABLE movimentacao_financeira
ADD COLUMN unidade_negocio_id UUID;

-- Atualiza registros existentes definindo a primeira unidade de negócio disponível
UPDATE movimentacao_financeira
SET
    unidade_negocio_id = (
        SELECT id
        FROM unidade_negocio
        LIMIT 1
    )
WHERE
    unidade_negocio_id IS NULL;

-- Torna a coluna NOT NULL
ALTER TABLE movimentacao_financeira
ALTER COLUMN unidade_negocio_id
SET
    NOT NULL;

-- Adiciona a foreign key com nome seguindo convenção
ALTER TABLE movimentacao_financeira
ADD CONSTRAINT fk_movimentacao_financeira_unidade_negocio FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio (id);

-- Cria índice para melhorar performance de queries por unidade de negócio
CREATE INDEX idx_movimentacao_financeira_unidade_negocio ON movimentacao_financeira (unidade_negocio_id);

-- Comentário na coluna
COMMENT ON COLUMN movimentacao_financeira.unidade_negocio_id IS 'Unidade de negócio responsável pela movimentação (obrigatório)';