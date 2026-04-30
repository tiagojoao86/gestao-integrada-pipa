DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'agendamento' AND column_name = 'categoria_id'
  ) THEN
    ALTER TABLE agendamento
      ADD COLUMN categoria_id UUID,
      ADD CONSTRAINT fk_agendamento_categoria
        FOREIGN KEY (categoria_id) REFERENCES convenio_categoria(id);
  END IF;
END $$;
