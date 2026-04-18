-- CPF passa a ser opcional para Pessoa Física (ex: crianças sem CPF)
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_name = 'pessoa' AND constraint_name = 'chk_pessoa_tipo'
  ) THEN
    ALTER TABLE pessoa DROP CONSTRAINT chk_pessoa_tipo;
  END IF;
END $$;

ALTER TABLE pessoa ADD CONSTRAINT chk_pessoa_tipo CHECK (
    (tipo_pessoa = 'FISICA' AND cnpj IS NULL AND razao_social IS NULL AND inscricao_estadual IS NULL) OR
    (tipo_pessoa = 'JURIDICA' AND cnpj IS NOT NULL AND cpf IS NULL AND data_nascimento IS NULL)
);
