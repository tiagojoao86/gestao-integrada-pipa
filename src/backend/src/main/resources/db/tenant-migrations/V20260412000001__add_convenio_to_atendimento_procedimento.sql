DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'atendimento_procedimento' AND column_name = 'convenio_id'
  ) THEN
    ALTER TABLE atendimento_procedimento
      ADD COLUMN convenio_id UUID,
      ADD CONSTRAINT fk_atend_proc_convenio FOREIGN KEY (convenio_id) REFERENCES convenio(id);
  END IF;
END $$;
