-- Adicionar coluna unidade_negocio_id em conta_bancaria
ALTER TABLE conta_bancaria ADD COLUMN unidade_negocio_id UUID;

-- Adicionar constraint de foreign key
ALTER TABLE conta_bancaria
ADD CONSTRAINT fk_conta_bancaria_unidade_negocio FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio (id);

-- Atualizar registros existentes com a primeira unidade de negócio disponível
-- (necessário antes de tornar o campo NOT NULL)
UPDATE conta_bancaria
SET
    unidade_negocio_id = (
        SELECT id
        FROM unidade_negocio
        LIMIT 1
    )
WHERE
    unidade_negocio_id IS NULL;

-- Tornar a coluna NOT NULL
ALTER TABLE conta_bancaria
ALTER COLUMN unidade_negocio_id
SET
    NOT NULL;

-- Criar índice para melhorar performance de queries
CREATE INDEX idx_conta_bancaria_unidade_negocio ON conta_bancaria (unidade_negocio_id);