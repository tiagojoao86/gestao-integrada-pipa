DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'agendamento' AND column_name = 'atendimento_id'
    ) THEN
        ALTER TABLE agendamento ADD COLUMN atendimento_id UUID;
    END IF;
END $$;
