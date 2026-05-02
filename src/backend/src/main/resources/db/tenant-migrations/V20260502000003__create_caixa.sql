DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'caixa') THEN
        CREATE TABLE caixa (
            id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            nome                            VARCHAR(150) NOT NULL,
            valor_padrao_abertura           NUMERIC(15,2) NOT NULL DEFAULT 0,
            percentual_pagamento_parcial    NUMERIC(5,2),
            valor_minimo_parcela            NUMERIC(15,2),
            ativo                           BOOLEAN NOT NULL DEFAULT TRUE,
            deleted                         BOOLEAN NOT NULL DEFAULT FALSE,
            deleted_at                      TIMESTAMP,
            deleted_by                      VARCHAR(255),
            created_at                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            created_by                      VARCHAR(255),
            updated_at                      TIMESTAMP,
            updated_by                      VARCHAR(255),
            CONSTRAINT uk_caixa_nome UNIQUE (nome),
            CONSTRAINT chk_caixa_valor_padrao
                CHECK (valor_padrao_abertura >= 0),
            CONSTRAINT chk_caixa_percentual_parcial
                CHECK (percentual_pagamento_parcial IS NULL
                    OR (percentual_pagamento_parcial >= 0 AND percentual_pagamento_parcial <= 100)),
            CONSTRAINT chk_caixa_valor_minimo_parcela
                CHECK (valor_minimo_parcela IS NULL OR valor_minimo_parcela >= 0)
        );
        CREATE INDEX idx_caixa_deleted ON caixa (deleted);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'usuario_caixa') THEN
        CREATE TABLE usuario_caixa (
            usuario_id  UUID NOT NULL,
            caixa_id    UUID NOT NULL REFERENCES caixa(id),
            CONSTRAINT pk_usuario_caixa PRIMARY KEY (usuario_id, caixa_id)
        );
        CREATE INDEX idx_usuario_caixa_caixa   ON usuario_caixa (caixa_id);
        CREATE INDEX idx_usuario_caixa_usuario ON usuario_caixa (usuario_id);
    END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'CADASTRO_CAIXA', 'Cadastro de Caixas', 'FINANCEIRO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'CADASTRO_CAIXA');

INSERT INTO perfil_modulo (
    id, perfil_id, modulo_id,
    pode_listar, pode_visualizar, pode_editar, pode_deletar, pode_auditar,
    created_at, created_by
)
SELECT
    gen_random_uuid(),
    '019a7fc4-ab0c-7002-8944-8e0ef009139b'::uuid,
    m.id,
    TRUE, TRUE, TRUE, TRUE, TRUE,
    CURRENT_TIMESTAMP, 'migration'
FROM modulo m
WHERE m.chave = 'CADASTRO_CAIXA'
  AND NOT EXISTS (
      SELECT 1 FROM perfil_modulo pm
      WHERE pm.perfil_id = '019a7fc4-ab0c-7002-8944-8e0ef009139b'::uuid
        AND pm.modulo_id = m.id
  );
