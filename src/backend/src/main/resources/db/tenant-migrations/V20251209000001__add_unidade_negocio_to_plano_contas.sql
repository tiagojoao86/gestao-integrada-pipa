-- Migration: Adicionar unidade_negocio_id em plano_contas
-- Author: Sistema
-- Date: 2025-12-09
-- Description: Adiciona coluna unidade_negocio_id e torna obrigatória na tabela plano_contas

-- Adiciona a coluna permitindo NULL temporariamente
ALTER TABLE plano_contas ADD COLUMN unidade_negocio_id UUID;

-- Atualiza os planos de contas existentes com a unidade padrão
UPDATE plano_contas
SET
    unidade_negocio_id = '019b010e-6348-74cb-acf7-2aebca002b44'
WHERE
    unidade_negocio_id IS NULL;

-- Agora torna a coluna NOT NULL
ALTER TABLE plano_contas
ALTER COLUMN unidade_negocio_id
SET
    NOT NULL;

-- Adiciona a foreign key
ALTER TABLE plano_contas
ADD CONSTRAINT fk_plano_contas_unidade_negocio FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio (id);

-- Adiciona índice para melhorar performance de queries
CREATE INDEX idx_plano_contas_unidade_negocio ON plano_contas (unidade_negocio_id);

-- Adiciona comentário
COMMENT ON COLUMN plano_contas.unidade_negocio_id IS 'Unidade de negócio à qual o plano de contas pertence (obrigatório)';