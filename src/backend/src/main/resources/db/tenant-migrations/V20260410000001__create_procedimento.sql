DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'procedimento') THEN
    CREATE TABLE procedimento (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      codigo VARCHAR(30) NOT NULL,
      codigo_tiss VARCHAR(20),
      codigo_tuss VARCHAR(20),
      descricao VARCHAR(200) NOT NULL,
      ativo BOOLEAN NOT NULL DEFAULT TRUE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT uk_procedimento_codigo UNIQUE (codigo)
    );
    CREATE INDEX idx_procedimento_deleted ON procedimento (deleted);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO_PROCEDIMENTO', 'Procedimento', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO_PROCEDIMENTO');

INSERT INTO perfil_modulo (
    id, perfil_id, modulo_id,
    pode_listar, pode_visualizar, pode_editar, pode_deletar, pode_auditar,
    created_at, created_by
)
SELECT
    gen_random_uuid(), p.id, m.id,
    TRUE, TRUE, TRUE, TRUE, TRUE,
    CURRENT_TIMESTAMP, 'migration'
FROM perfil p
CROSS JOIN modulo m
WHERE p.nome = 'Administrador Geral'
  AND m.chave = 'ATENDIMENTO_PROCEDIMENTO'
  AND NOT EXISTS (
    SELECT 1 FROM perfil_modulo pm WHERE pm.perfil_id = p.id AND pm.modulo_id = m.id
  );
