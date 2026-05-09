-- Adiciona tabela de formas de pagamento por conta bancária
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'conta_bancaria_forma_pagamento'
    ) THEN
        CREATE TABLE conta_bancaria_forma_pagamento (
            conta_bancaria_id UUID NOT NULL,
            forma_pagamento   VARCHAR(30) NOT NULL,
            CONSTRAINT fk_cbfp_conta_bancaria
                FOREIGN KEY (conta_bancaria_id) REFERENCES conta_bancaria(id)
        );
        CREATE INDEX idx_cbfp_conta_bancaria
            ON conta_bancaria_forma_pagamento (conta_bancaria_id);
    END IF;
END $$;

-- Adiciona rastreabilidade de origem caixa na movimentação financeira
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'movimentacao_financeira'
          AND column_name = 'movimentacao_caixa_id'
    ) THEN
        ALTER TABLE movimentacao_financeira
            ADD COLUMN movimentacao_caixa_id UUID;
    END IF;
END $$;

-- Remove coluna valor_pago_caixa do titulo (não mais utilizada)
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'titulo'
          AND column_name = 'valor_pago_caixa'
    ) THEN
        ALTER TABLE titulo DROP COLUMN valor_pago_caixa;
    END IF;
END $$;
