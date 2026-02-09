-- Migration: add condicao_pagamento_id to titulo table
-- Date: 2026-02-04

ALTER TABLE titulo
ADD COLUMN IF NOT EXISTS condicao_pagamento_id UUID;

ALTER TABLE titulo
ADD CONSTRAINT fk_titulo_condicao_pagamento
FOREIGN KEY (condicao_pagamento_id)
REFERENCES condicao_pagamento(id);
