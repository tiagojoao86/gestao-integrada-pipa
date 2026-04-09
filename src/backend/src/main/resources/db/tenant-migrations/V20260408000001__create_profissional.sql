DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'profissional') THEN
    CREATE TABLE profissional (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      pessoa_id UUID NOT NULL,
      conselho VARCHAR(20) NOT NULL,
      codigo_conselho VARCHAR(30) NOT NULL,
      tipo_remuneracao VARCHAR(20) NOT NULL,
      banco VARCHAR(100),
      conta VARCHAR(50),
      chave_pix VARCHAR(150),
      ativo BOOLEAN NOT NULL DEFAULT TRUE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT fk_profissional_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
      CONSTRAINT uk_profissional_pessoa UNIQUE (pessoa_id)
    );
    CREATE INDEX idx_profissional_deleted ON profissional (deleted);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO_PROFISSIONAL', 'Profissional', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO_PROFISSIONAL');
