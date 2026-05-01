DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_sequences WHERE sequencename = 'atendimento_numero_seq'
    ) THEN
        CREATE SEQUENCE atendimento_numero_seq START 1;
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'atendimento' AND column_name = 'numero'
    ) THEN
        ALTER TABLE atendimento
            ADD COLUMN numero BIGINT NOT NULL DEFAULT nextval('atendimento_numero_seq');
        ALTER SEQUENCE atendimento_numero_seq OWNED BY atendimento.numero;
        CREATE UNIQUE INDEX uk_atendimento_numero ON atendimento (numero);
    END IF;
END $$;
