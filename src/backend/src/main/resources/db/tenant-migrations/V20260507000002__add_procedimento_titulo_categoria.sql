ALTER TABLE procedimento ADD COLUMN IF NOT EXISTS titulo_categoria_id UUID;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_procedimento_titulo_categoria'
    ) THEN
        ALTER TABLE procedimento
            ADD CONSTRAINT fk_procedimento_titulo_categoria
            FOREIGN KEY (titulo_categoria_id) REFERENCES titulo_categoria(id);
    END IF;
END $$;
