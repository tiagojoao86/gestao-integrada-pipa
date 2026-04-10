DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'convenio') THEN
    CREATE TABLE convenio (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      nome VARCHAR(100) NOT NULL,
      pessoa_id UUID NOT NULL,
      registro_ans VARCHAR(20),
      ativo BOOLEAN NOT NULL DEFAULT TRUE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT uk_convenio_nome UNIQUE (nome),
      CONSTRAINT fk_convenio_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
    );
    CREATE UNIQUE INDEX uk_convenio_registro_ans ON convenio (registro_ans)
      WHERE registro_ans IS NOT NULL;
    CREATE INDEX idx_convenio_deleted ON convenio (deleted);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO_CONVENIO', 'Convênio', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO_CONVENIO');
