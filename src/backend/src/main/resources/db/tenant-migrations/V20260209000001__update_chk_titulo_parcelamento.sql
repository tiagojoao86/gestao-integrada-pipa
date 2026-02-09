-- Atualiza CHECK constraint de parcelamento para permitir título pai
-- (totalParcelas definido, numeroParcela null, tituloOrigem null)
ALTER TABLE titulo DROP CONSTRAINT IF EXISTS chk_titulo_parcelamento;

ALTER TABLE titulo ADD CONSTRAINT chk_titulo_parcelamento CHECK (
    (
        -- Caso 1: Título normal (sem parcelamento)
        numero_parcela IS NULL
        AND total_parcelas IS NULL
        AND titulo_origem_id IS NULL
    )
    OR (
        -- Caso 2: Título filho (parcela)
        numero_parcela IS NOT NULL
        AND total_parcelas IS NOT NULL
        AND numero_parcela <= total_parcelas
    )
    OR (
        -- Caso 3: Título pai (origem de parcelamento)
        numero_parcela IS NULL
        AND total_parcelas IS NOT NULL
        AND total_parcelas > 1
        AND titulo_origem_id IS NULL
    )
);
