-- Migration: Add titulo_categoria_id to titulo table
-- Date: 2025-12-21

-- Add titulo_categoria_id column to titulo table
ALTER TABLE titulo ADD COLUMN IF NOT EXISTS titulo_categoria_id UUID;

-- Add foreign key constraint
ALTER TABLE titulo DROP CONSTRAINT IF EXISTS fk_titulo_categoria;

ALTER TABLE titulo
ADD CONSTRAINT fk_titulo_categoria FOREIGN KEY (titulo_categoria_id) REFERENCES titulo_categoria (id);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_titulo_categoria ON titulo (titulo_categoria_id);

COMMENT ON COLUMN titulo.titulo_categoria_id IS 'Categoria do título para classificação de receitas/despesas';