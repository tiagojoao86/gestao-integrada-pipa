-- Remove coluna nome_fantasia da tabela pessoa_juridica
ALTER TABLE pessoa_juridica DROP COLUMN nome_fantasia;

COMMENT ON COLUMN pessoa.nome IS 'Para Pessoa Física: nome da pessoa. Para Pessoa Jurídica: nome fantasia da empresa.';