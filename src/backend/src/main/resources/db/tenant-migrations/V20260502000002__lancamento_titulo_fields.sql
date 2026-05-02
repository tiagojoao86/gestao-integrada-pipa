-- Adiciona campos de snapshot de setor/unidade e referência soft ao título gerado
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'lancamento_financeiro' AND column_name = 'setor_id'
    ) THEN
        ALTER TABLE lancamento_financeiro
            ADD COLUMN setor_id UUID,
            ADD COLUMN setor_nome VARCHAR(150),
            ADD COLUMN unidade_negocio_id UUID,
            ADD COLUMN unidade_negocio_nome VARCHAR(150),
            ADD COLUMN titulo_id UUID;
    END IF;
END $$;

-- Corrige convênio "Particular" que pode ter recebido FATURADO como default
UPDATE convenio SET tipo_cobranca = 'PAGO_NO_ATO' WHERE nome = 'Particular' AND tipo_cobranca = 'FATURADO';

-- Retroalimenta lançamentos do Particular que ficaram com tipo errado
UPDATE lancamento_financeiro lf
SET convenio_tipo_cobranca = 'PAGO_NO_ATO'
FROM convenio c
WHERE lf.convenio_id = c.id
  AND c.nome = 'Particular'
  AND lf.convenio_tipo_cobranca = 'FATURADO';

-- Seed TituloCategoria para lançamentos de atendimento
INSERT INTO titulo_categoria (id, codigo, nome, tipo, created_at, created_by, updated_at, updated_by)
SELECT gen_random_uuid(), 'ATEND', 'Atendimento', 'RECEITA',
       CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration'
WHERE NOT EXISTS (SELECT 1 FROM titulo_categoria WHERE codigo = 'ATEND');
