DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'titulo' AND column_name = 'valor_pago_caixa'
  ) THEN
    ALTER TABLE titulo ADD COLUMN valor_pago_caixa NUMERIC(15, 2) NOT NULL DEFAULT 0;
    ALTER TABLE titulo ADD CONSTRAINT chk_titulo_valor_pago_caixa
      CHECK (valor_pago_caixa >= 0);
  END IF;
END $$;
