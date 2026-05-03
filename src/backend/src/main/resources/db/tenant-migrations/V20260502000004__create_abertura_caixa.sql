DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'abertura_caixa') THEN
        CREATE TABLE abertura_caixa (
            id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            caixa_id            UUID NOT NULL,
            usuario_id          UUID NOT NULL,
            usuario_nome        VARCHAR(255) NOT NULL,
            data_abertura       TIMESTAMP NOT NULL,
            data_fechamento     TIMESTAMP,
            valor_abertura      NUMERIC(15,2) NOT NULL DEFAULT 0,
            valor_conferencia   NUMERIC(15,2),
            status              VARCHAR(20) NOT NULL,
            observacoes         TEXT,
            deleted             BOOLEAN NOT NULL DEFAULT FALSE,
            deleted_at          TIMESTAMP,
            deleted_by          VARCHAR(255),
            created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            created_by          VARCHAR(255),
            updated_at          TIMESTAMP,
            updated_by          VARCHAR(255),
            CONSTRAINT fk_abertura_caixa_caixa
                FOREIGN KEY (caixa_id) REFERENCES caixa(id),
            CONSTRAINT chk_abertura_caixa_status
                CHECK (status IN ('ABERTO', 'FECHADO')),
            CONSTRAINT chk_abertura_caixa_valor_abertura
                CHECK (valor_abertura >= 0),
            CONSTRAINT chk_abertura_caixa_valor_conf
                CHECK (valor_conferencia IS NULL OR valor_conferencia >= 0)
        );
        CREATE INDEX idx_abertura_caixa_caixa   ON abertura_caixa (caixa_id);
        CREATE INDEX idx_abertura_caixa_deleted ON abertura_caixa (deleted);
        CREATE UNIQUE INDEX uk_abertura_caixa_ativa ON abertura_caixa (caixa_id)
            WHERE status = 'ABERTO';
    END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'OPERACAO_CAIXA', 'Operação de Caixa', 'FINANCEIRO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'OPERACAO_CAIXA');

INSERT INTO perfil_modulo (
    id, perfil_id, modulo_id,
    pode_listar, pode_visualizar, pode_editar, pode_deletar, pode_auditar,
    created_at, created_by
)
SELECT
    gen_random_uuid(),
    '019a7fc4-ab0c-7002-8944-8e0ef009139b'::uuid,
    m.id,
    TRUE, TRUE, TRUE, TRUE, FALSE,
    CURRENT_TIMESTAMP, 'migration'
FROM modulo m
WHERE m.chave = 'OPERACAO_CAIXA'
  AND NOT EXISTS (
      SELECT 1 FROM perfil_modulo pm
      WHERE pm.perfil_id = '019a7fc4-ab0c-7002-8944-8e0ef009139b'::uuid
        AND pm.modulo_id = m.id
  );
