-- Migration para adicionar campos de soft delete (auditoria de exclusão) em todas as tabelas

-- Tabela usuario
ALTER TABLE usuario
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela perfil
ALTER TABLE perfil
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela perfil_modulo
ALTER TABLE perfil_modulo
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela pessoa
ALTER TABLE pessoa
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela unidade_negocio
ALTER TABLE unidade_negocio
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela plano_contas
ALTER TABLE plano_contas
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela conta_bancaria
ALTER TABLE conta_bancaria
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela titulo
ALTER TABLE titulo
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela movimentacao_financeira
ALTER TABLE movimentacao_financeira
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela centro_custo
ALTER TABLE centro_custo
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela setor
ALTER TABLE setor
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela titulo_setor
ALTER TABLE titulo_setor
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Tabela titulo_categoria
ALTER TABLE titulo_categoria
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(255);

-- Criar índices para melhorar performance das consultas com filtro de deleted
CREATE INDEX idx_usuario_deleted ON usuario(deleted);
CREATE INDEX idx_perfil_deleted ON perfil(deleted);
CREATE INDEX idx_pessoa_deleted ON pessoa(deleted);
CREATE INDEX idx_unidade_negocio_deleted ON unidade_negocio(deleted);
CREATE INDEX idx_plano_contas_deleted ON plano_contas(deleted);
CREATE INDEX idx_conta_bancaria_deleted ON conta_bancaria(deleted);
CREATE INDEX idx_titulo_deleted ON titulo(deleted);
CREATE INDEX idx_movimentacao_financeira_deleted ON movimentacao_financeira(deleted);
CREATE INDEX idx_centro_custo_deleted ON centro_custo(deleted);
CREATE INDEX idx_setor_deleted ON setor(deleted);
CREATE INDEX idx_titulo_categoria_deleted ON titulo_categoria(deleted);
