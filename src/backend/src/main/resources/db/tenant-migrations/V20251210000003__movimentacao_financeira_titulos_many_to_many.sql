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

-- Remover coluna antiga
ALTER TABLE movimentacao_financeira DROP COLUMN IF EXISTS titulo_id;

-- Indexes para performance
CREATE INDEX idx_movimentacao_titulo_mov ON movimentacao_financeira_titulo (movimentacao_financeira_id);

CREATE INDEX idx_movimentacao_titulo_tit ON movimentacao_financeira_titulo (titulo_id);