-- Migration: Remove valor_pago column from titulo table
-- Reason: valor_pago is now calculated from movimentacoes (transient field)
-- This improves data consistency and eliminates redundancy

-- Remove the valor_pago column
ALTER TABLE titulo DROP COLUMN IF EXISTS valor_pago;

-- Add comment to document the change
COMMENT ON TABLE titulo IS 'Títulos a pagar/receber. valor_pago é calculado a partir das movimentações financeiras.';
