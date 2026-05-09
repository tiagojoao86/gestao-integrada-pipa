DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables WHERE table_name = 'movimentacao_caixa'
  ) THEN
    CREATE TABLE movimentacao_caixa (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      created_by VARCHAR(255),
      updated_at TIMESTAMP,
      updated_by VARCHAR(255),
      abertura_caixa_id UUID NOT NULL,
      lancamento_id UUID NOT NULL,
      titulo_id UUID,
      valor NUMERIC(15, 2) NOT NULL,
      forma_pagamento VARCHAR(30) NOT NULL,
      data_hora TIMESTAMP NOT NULL,
      observacoes TEXT,
      CONSTRAINT fk_movimentacao_caixa_abertura
        FOREIGN KEY (abertura_caixa_id) REFERENCES abertura_caixa(id),
      CONSTRAINT chk_movimentacao_caixa_forma
        CHECK (forma_pagamento IN (
          'PIX', 'DINHEIRO', 'BOLETO', 'CARTAO_CREDITO',
          'CARTAO_DEBITO', 'TED', 'DOC', 'CHEQUE', 'DEPOSITO'
        )),
      CONSTRAINT chk_movimentacao_caixa_valor CHECK (valor > 0)
    );

    CREATE INDEX idx_movimentacao_caixa_deleted ON movimentacao_caixa (deleted);
    CREATE INDEX idx_movimentacao_caixa_abertura ON movimentacao_caixa (abertura_caixa_id);
    CREATE INDEX idx_movimentacao_caixa_lancamento ON movimentacao_caixa (lancamento_id);
  END IF;
END $$;
