DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'codigo_convenio') THEN
    CREATE TABLE codigo_convenio (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      convenio_id UUID NOT NULL,
      procedimento_id UUID NOT NULL,
      codigo VARCHAR(30) NOT NULL,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT fk_codigo_convenio_convenio FOREIGN KEY (convenio_id) REFERENCES convenio(id),
      CONSTRAINT fk_codigo_convenio_procedimento FOREIGN KEY (procedimento_id) REFERENCES procedimento(id),
      CONSTRAINT uk_codigo_convenio_convenio_procedimento UNIQUE (convenio_id, procedimento_id)
    );
    CREATE INDEX idx_codigo_convenio_deleted ON codigo_convenio (deleted);
  END IF;
END $$;
