DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'caixa' AND column_name = 'unidade_negocio_id'
  ) THEN
    ALTER TABLE caixa
      ADD COLUMN unidade_negocio_id UUID,
      ADD CONSTRAINT fk_caixa_unidade_negocio
        FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio(id);
  END IF;
END $$;
