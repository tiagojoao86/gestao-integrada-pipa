-- Tenant migration: create titulo_setor table (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'titulo_setor') THEN
        CREATE TABLE titulo_setor (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            titulo_id UUID NOT NULL,
            setor_id UUID NOT NULL,
            percentual_rateio DECIMAL(5, 2) NOT NULL,
            created_at TIMESTAMP WITHOUT TIME ZONE,
            updated_at TIMESTAMP WITHOUT TIME ZONE,
            created_by VARCHAR(255),
            updated_by VARCHAR(255),
            CONSTRAINT chk_titulo_setor_percentual CHECK (percentual_rateio > 0 AND percentual_rateio <= 100)
        );

        ALTER TABLE titulo_setor
          ADD CONSTRAINT uk_titulo_setor UNIQUE (titulo_id, setor_id);

        ALTER TABLE titulo_setor
          ADD CONSTRAINT fk_titulo_setor_titulo FOREIGN KEY (titulo_id) REFERENCES titulo(id) ON DELETE CASCADE;

        ALTER TABLE titulo_setor
          ADD CONSTRAINT fk_titulo_setor_setor FOREIGN KEY (setor_id) REFERENCES setor(id);

        CREATE INDEX idx_titulo_setor_titulo ON titulo_setor(titulo_id);
        CREATE INDEX idx_titulo_setor_setor ON titulo_setor(setor_id);
    END IF;
EXCEPTION WHEN others THEN
    RAISE NOTICE 'tenant migration create_titulo_setor_table: %', SQLERRM;
END $$;
