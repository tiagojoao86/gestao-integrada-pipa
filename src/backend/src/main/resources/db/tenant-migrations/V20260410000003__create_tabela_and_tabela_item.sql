DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela') THEN
    CREATE TABLE tabela (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      nome VARCHAR(100) NOT NULL,
      tipo VARCHAR(20) NOT NULL,
      ativo BOOLEAN NOT NULL DEFAULT TRUE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT uk_tabela_nome UNIQUE (nome)
    );
    CREATE INDEX idx_tabela_deleted ON tabela (deleted);
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_item') THEN
    CREATE TABLE tabela_item (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      tabela_id UUID NOT NULL,
      procedimento_id UUID NOT NULL,
      valor NUMERIC(12,2) NOT NULL,
      vigencia_inicio DATE NOT NULL,
      vigencia_fim DATE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT fk_tabela_item_tabela FOREIGN KEY (tabela_id) REFERENCES tabela(id),
      CONSTRAINT fk_tabela_item_procedimento FOREIGN KEY (procedimento_id) REFERENCES procedimento(id),
      CONSTRAINT uk_tabela_item_tabela_procedimento_inicio
        UNIQUE (tabela_id, procedimento_id, vigencia_inicio)
    );
    CREATE INDEX idx_tabela_item_deleted ON tabela_item (deleted);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO_TABELA', 'Tabela de Preços', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO_TABELA');

INSERT INTO perfil_modulo (id, perfil_id, modulo_id, pode_listar, pode_visualizar, pode_editar,
    pode_deletar, pode_auditar)
SELECT gen_random_uuid(), p.id, m.id, TRUE, TRUE, TRUE, TRUE, TRUE
FROM perfil p, modulo m
WHERE p.admin = TRUE AND m.chave = 'ATENDIMENTO_TABELA'
AND NOT EXISTS (
    SELECT 1 FROM perfil_modulo pm WHERE pm.perfil_id = p.id AND pm.modulo_id = m.id
);
