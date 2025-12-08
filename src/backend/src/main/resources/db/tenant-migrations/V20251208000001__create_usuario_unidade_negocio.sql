-- Migration: Create usuario_unidade_negocio join table
-- Date: 2025-12-08
-- Description: Creates the join table for many-to-many relationship between Usuario and UnidadeNegocio with default flag

CREATE TABLE usuario_unidade_negocio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    usuario_id UUID NOT NULL,
    unidade_negocio_id UUID NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT uk_usuario_unidade_negocio UNIQUE (
        usuario_id,
        unidade_negocio_id
    ),
    CONSTRAINT fk_usuario_unidade_negocio_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_unidade_negocio_unidade FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio (id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_usuario_unidade_negocio_usuario ON usuario_unidade_negocio (usuario_id);

CREATE INDEX idx_usuario_unidade_negocio_unidade ON usuario_unidade_negocio (unidade_negocio_id);

CREATE INDEX idx_usuario_unidade_negocio_default ON usuario_unidade_negocio (usuario_id, is_default)
WHERE
    is_default = TRUE;

-- Add comment
COMMENT ON
TABLE usuario_unidade_negocio IS 'Join table for usuario and unidade_negocio many-to-many relationship with default flag';

COMMENT ON COLUMN usuario_unidade_negocio.is_default IS 'Indicates if this is the default unidade de negocio for the user';