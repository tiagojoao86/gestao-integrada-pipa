-- Permite convênio sem pessoa (ex: Particular — tipo PAGO_NO_ATO)
ALTER TABLE convenio ALTER COLUMN pessoa_id DROP NOT NULL;

-- Adiciona tipo de cobrança ao convênio
ALTER TABLE convenio ADD COLUMN IF NOT EXISTS tipo_cobranca VARCHAR(20) NOT NULL DEFAULT 'FATURADO';

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'chk_convenio_tipo_cobranca'
      AND conrelid = 'convenio'::regclass
  ) THEN
    ALTER TABLE convenio
      ADD CONSTRAINT chk_convenio_tipo_cobranca
        CHECK (tipo_cobranca IN ('PAGO_NO_ATO', 'FATURADO'));
  END IF;
END $$;

-- Seed convênio "Particular" para novas instalações
INSERT INTO convenio (id, nome, pessoa_id, tipo_cobranca, ativo, deleted, created_at, created_by, updated_at, updated_by)
SELECT gen_random_uuid(), 'Particular', NULL, 'PAGO_NO_ATO', TRUE, FALSE,
       CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration'
WHERE NOT EXISTS (SELECT 1 FROM convenio WHERE nome = 'Particular');

-- Adiciona snapshot do tipo de cobrança no lançamento financeiro
ALTER TABLE lancamento_financeiro
  ADD COLUMN IF NOT EXISTS convenio_tipo_cobranca VARCHAR(20);

-- Retroalimenta registros existentes via join com convenio
UPDATE lancamento_financeiro lf
SET convenio_tipo_cobranca = c.tipo_cobranca
FROM convenio c
WHERE lf.convenio_id = c.id
  AND lf.convenio_tipo_cobranca IS NULL;

-- Lançamentos sem convênio (particular antigo) = PAGO_NO_ATO
UPDATE lancamento_financeiro
SET convenio_tipo_cobranca = 'PAGO_NO_ATO'
WHERE convenio_id IS NULL
  AND convenio_tipo_cobranca IS NULL;
