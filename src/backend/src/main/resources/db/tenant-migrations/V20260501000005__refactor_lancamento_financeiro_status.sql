-- Adiciona colunas novas
ALTER TABLE lancamento_financeiro
    ADD COLUMN IF NOT EXISTS situacao VARCHAR(20) NOT NULL DEFAULT 'ABERTO',
    ADD COLUMN IF NOT EXISTS status_financeiro VARCHAR(20) NOT NULL DEFAULT 'PENDENTE';

-- Migra dados existentes
UPDATE lancamento_financeiro SET situacao = 'ABERTO',   status_financeiro = 'PENDENTE'
    WHERE status = 'PENDENTE';
UPDATE lancamento_financeiro SET situacao = 'FECHADO',  status_financeiro = 'PAGO'
    WHERE status = 'PAGO';
UPDATE lancamento_financeiro SET situacao = 'FECHADO',  status_financeiro = 'FATURADO'
    WHERE status = 'FATURADO';
UPDATE lancamento_financeiro SET situacao = 'CANCELADO', status_financeiro = 'PENDENTE'
    WHERE status = 'CANCELADO';

-- Adiciona constraints de validação
ALTER TABLE lancamento_financeiro
    DROP CONSTRAINT IF EXISTS chk_lancamento_financeiro_status,
    ADD CONSTRAINT chk_lancamento_financeiro_situacao
        CHECK (situacao IN ('ABERTO', 'FECHADO', 'CANCELADO')),
    ADD CONSTRAINT chk_lancamento_financeiro_status_fin
        CHECK (status_financeiro IN ('PENDENTE', 'PAGO', 'FATURADO'));

-- Remove coluna antiga
ALTER TABLE lancamento_financeiro DROP COLUMN IF EXISTS status;
