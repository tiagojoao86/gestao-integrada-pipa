DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'pessoa' AND column_name = 'endereco_cep'
  ) THEN
    ALTER TABLE pessoa
      ADD COLUMN endereco_cep          VARCHAR(8),
      ADD COLUMN endereco_logradouro   VARCHAR(200),
      ADD COLUMN endereco_numero       VARCHAR(20),
      ADD COLUMN endereco_complemento  VARCHAR(100),
      ADD COLUMN endereco_bairro       VARCHAR(100),
      ADD COLUMN endereco_cidade       VARCHAR(100),
      ADD COLUMN endereco_uf           CHAR(2);
  END IF;
END $$;
