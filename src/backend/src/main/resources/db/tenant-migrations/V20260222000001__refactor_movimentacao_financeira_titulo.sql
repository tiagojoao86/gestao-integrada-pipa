-- Refatoração de movimentacao_financeira_titulo:
-- Converte tabela de junção simples para entidade JPA com id, valor por título e auditoria.

-- Passo 1: Adicionar colunas do BaseEntity e coluna valor
ALTER TABLE movimentacao_financeira_titulo
    ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid(),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255) DEFAULT 'migration',
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS valor NUMERIC(15, 2);

-- Passo 2: Populara valor com o valor da movimentação para registros existentes
-- (em registros com múltiplos títulos o valor ficará incorreto;
--  isso reflete o bug existente que está sendo corrigido por esta migration)
UPDATE movimentacao_financeira_titulo mft
SET valor = (
    SELECT mf.valor
    FROM movimentacao_financeira mf
    WHERE mf.id = mft.movimentacao_financeira_id
)
WHERE valor IS NULL;

-- Passo 3: Garantir NOT NULL após UPDATE
ALTER TABLE movimentacao_financeira_titulo
    ALTER COLUMN id SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_by SET NOT NULL,
    ALTER COLUMN valor SET NOT NULL;

-- Passo 4: Remover a PRIMARY KEY composta antiga
ALTER TABLE movimentacao_financeira_titulo
    DROP CONSTRAINT IF EXISTS movimentacao_financeira_titulo_pkey;

-- Passo 5: Definir nova PRIMARY KEY no id
ALTER TABLE movimentacao_financeira_titulo
    ADD CONSTRAINT pk_movimentacao_financeira_titulo PRIMARY KEY (id);

-- Passo 6: Adicionar UNIQUE constraint no par (movimentacao_financeira_id, titulo_id)
ALTER TABLE movimentacao_financeira_titulo
    ADD CONSTRAINT uk_movimentacao_financeira_titulo
    UNIQUE (movimentacao_financeira_id, titulo_id);

-- Passo 7: Remover índices duplicados (o índice da PK composta foi removido)
-- Os índices existentes ainda são válidos
