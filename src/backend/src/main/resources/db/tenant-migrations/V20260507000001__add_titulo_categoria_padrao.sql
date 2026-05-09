ALTER TABLE titulo_categoria ADD COLUMN IF NOT EXISTS padrao BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_titulo_categoria_padrao
    ON titulo_categoria (padrao)
    WHERE padrao = TRUE AND (deleted IS NULL OR deleted = FALSE);
