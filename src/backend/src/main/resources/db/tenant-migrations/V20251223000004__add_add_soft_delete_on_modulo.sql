-- Tabela modulo
ALTER TABLE modulo
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Criar índices para melhorar performance das consultas com filtro de deleted
CREATE INDEX idx_modulo_deleted ON modulo (deleted);