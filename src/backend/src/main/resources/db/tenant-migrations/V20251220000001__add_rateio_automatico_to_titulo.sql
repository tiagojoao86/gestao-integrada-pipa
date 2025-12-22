-- Add rateio_automatico column to titulo table
-- When true, the titulo's setores represent the cost origin,
-- but during cost apportionment, the value will be distributed among all active setores

ALTER TABLE titulo
ADD COLUMN rateio_automatico BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN titulo.rateio_automatico IS 'Indica se o custo deste título deve ser rateado automaticamente entre todos os setores ativos durante a apuração de custos. Os setores cadastrados no título representam a origem do custo para rastreabilidade.';
