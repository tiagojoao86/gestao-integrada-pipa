DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'pessoa' AND column_name = 'responsavel_id'
  ) THEN
    ALTER TABLE pessoa ADD COLUMN responsavel_id UUID;
    ALTER TABLE pessoa ADD CONSTRAINT fk_pessoa_responsavel
      FOREIGN KEY (responsavel_id) REFERENCES pessoa(id);
  END IF;
END $$;
