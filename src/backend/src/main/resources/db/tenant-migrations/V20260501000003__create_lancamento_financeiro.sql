DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'lancamento_financeiro') THEN
        CREATE TABLE lancamento_financeiro (
            id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            atendimento_id      UUID NOT NULL,
            atendimento_numero  BIGINT,
            data_atendimento    DATE,
            paciente_id         UUID NOT NULL,
            paciente_nome       VARCHAR(255),
            convenio_id         UUID,
            convenio_nome       VARCHAR(255),
            valor_total         NUMERIC(15, 2) NOT NULL DEFAULT 0,
            status              VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
            observacoes         TEXT,
            deleted             BOOLEAN NOT NULL DEFAULT FALSE,
            deleted_at          TIMESTAMP,
            deleted_by          VARCHAR(255),
            created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            created_by          VARCHAR(255),
            updated_at          TIMESTAMP,
            updated_by          VARCHAR(255),
            CONSTRAINT chk_lancamento_financeiro_status
                CHECK (status IN ('PENDENTE', 'FATURADO', 'CANCELADO'))
        );

        CREATE INDEX idx_lancamento_financeiro_deleted
            ON lancamento_financeiro (deleted);

        CREATE INDEX idx_lancamento_financeiro_atendimento
            ON lancamento_financeiro (atendimento_id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'lancamento_financeiro_procedimento') THEN
        CREATE TABLE lancamento_financeiro_procedimento (
            id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            lancamento_financeiro_id UUID NOT NULL,
            procedimento_id         UUID,
            procedimento_codigo     VARCHAR(50),
            procedimento_descricao  VARCHAR(255),
            convenio_id             UUID,
            convenio_nome           VARCHAR(255),
            tabela_item_id          UUID,
            valor                   NUMERIC(15, 2) NOT NULL DEFAULT 0,
            CONSTRAINT fk_lanc_proc_lancamento
                FOREIGN KEY (lancamento_financeiro_id)
                REFERENCES lancamento_financeiro (id)
        );

        CREATE INDEX idx_lancamento_financeiro_procedimento_lancamento
            ON lancamento_financeiro_procedimento (lancamento_financeiro_id);
    END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'LANCAMENTO_FINANCEIRO', 'Lançamento Financeiro', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'LANCAMENTO_FINANCEIRO');

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
WHERE m.chave = 'LANCAMENTO_FINANCEIRO'
  AND NOT EXISTS (
      SELECT 1 FROM perfil_modulo pm
      WHERE pm.perfil_id = '019a7fc4-ab0c-7002-8944-8e0ef009139b'::uuid
        AND pm.modulo_id = m.id
  );
