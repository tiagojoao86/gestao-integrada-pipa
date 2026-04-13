DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'profissional' AND column_name = 'uf'
  ) THEN
    ALTER TABLE profissional ADD COLUMN uf VARCHAR(2);
  END IF;
END $$;
