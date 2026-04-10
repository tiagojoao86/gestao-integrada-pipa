DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'convenio_categoria') THEN
    CREATE TABLE convenio_categoria (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      convenio_id UUID NOT NULL,
      nome VARCHAR(100) NOT NULL,
      codigo_ans_plano VARCHAR(20),
      ativo BOOLEAN NOT NULL DEFAULT TRUE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT fk_convenio_categoria_convenio FOREIGN KEY (convenio_id) REFERENCES convenio(id),
      CONSTRAINT uk_convenio_categoria_nome_convenio UNIQUE (convenio_id, nome)
    );
    CREATE INDEX idx_convenio_categoria_deleted ON convenio_categoria (deleted);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO_CONVENIO_CATEGORIA', 'Categoria de Convênio', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO_CONVENIO_CATEGORIA');

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
  AND m.chave = 'ATENDIMENTO_CONVENIO_CATEGORIA'
  AND NOT EXISTS (
    SELECT 1 FROM perfil_modulo pm WHERE pm.perfil_id = p.id AND pm.modulo_id = m.id
  );
