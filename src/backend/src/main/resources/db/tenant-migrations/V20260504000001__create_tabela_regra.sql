DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_regra'
  ) THEN
    CREATE TABLE tabela_regra (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      convenio_id UUID NOT NULL,
      convenio_categoria_id UUID,
      tabela_id UUID NOT NULL,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      created_by VARCHAR(255),
      updated_at TIMESTAMP,
      updated_by VARCHAR(255),
      CONSTRAINT fk_tabela_regra_convenio
        FOREIGN KEY (convenio_id) REFERENCES convenio(id),
      CONSTRAINT fk_tabela_regra_convenio_categoria
        FOREIGN KEY (convenio_categoria_id) REFERENCES convenio_categoria(id),
      CONSTRAINT fk_tabela_regra_tabela
        FOREIGN KEY (tabela_id) REFERENCES tabela(id)
    );

    CREATE UNIQUE INDEX uk_tabela_regra_convenio_geral
      ON tabela_regra (convenio_id)
      WHERE convenio_categoria_id IS NULL AND deleted = FALSE;

    CREATE UNIQUE INDEX uk_tabela_regra_convenio_categoria
      ON tabela_regra (convenio_id, convenio_categoria_id)
      WHERE convenio_categoria_id IS NOT NULL AND deleted = FALSE;

    CREATE INDEX idx_tabela_regra_deleted ON tabela_regra (deleted);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO_TABELA_REGRA', 'Regras de Tabela', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO_TABELA_REGRA');
