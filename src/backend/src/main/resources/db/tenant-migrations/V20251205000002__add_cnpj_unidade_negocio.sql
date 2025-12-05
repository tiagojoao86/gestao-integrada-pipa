-- Migration: Adiciona coluna CNPJ na tabela unidade_negocio
-- Data: 2025-12-05
-- Descrição: Adiciona campo para armazenar o CNPJ da unidade de negócio

-- CNPJ ValueObject armazena apenas os 14 dígitos
ALTER TABLE unidade_negocio ADD COLUMN cnpj VARCHAR(14);

COMMENT ON COLUMN unidade_negocio.cnpj IS 'CNPJ da unidade de negócio (14 dígitos)';