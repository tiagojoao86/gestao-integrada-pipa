-- Tenant migration: create centro_custo table (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'centro_custo') THEN
        CREATE TABLE centro_custo (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            nome VARCHAR(200) NOT NULL,
            centro_resultado BOOLEAN,
            unidade_negocio_id UUID NOT NULL,
            created_at TIMESTAMP WITHOUT TIME ZONE,
            updated_at TIMESTAMP WITHOUT TIME ZONE,
            created_by VARCHAR(255),
            updated_by VARCHAR(255)
        );
        ALTER TABLE centro_custo
          ADD CONSTRAINT uk_centro_custo_nome UNIQUE (nome);
        ALTER TABLE centro_custo
          ADD CONSTRAINT fk_centro_custo_unidade_negocio FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio(id);
    END IF;
EXCEPTION WHEN others THEN
    RAISE NOTICE 'tenant migration create_centro_custo_table: %', SQLERRM;
END $$;

INSERT INTO
    modulo (id, chave, nome, grupo)
SELECT gen_random_uuid (), 'FINANCEIRO_CENTRO_CUSTO', 'Cadastro de Centro de Custo', 'FINANCEIRO'
WHERE
    NOT EXISTS (
        SELECT 1
        FROM modulo
        WHERE
            chave = 'FINANCEIRO_CENTRO_CUSTO'
    );

INSERT INTO
    perfil_modulo (
        id,
        perfil_id,
        modulo_id,
        pode_listar,
        pode_visualizar,
        pode_editar,
        pode_deletar,
        created_at,
        created_by
    )
SELECT
    gen_random_uuid (),
    p.id,
    m.id,
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    'migration'
FROM perfil p
    CROSS JOIN modulo m
WHERE
    p.nome = 'Administrador Geral'
    AND m.chave = 'FINANCEIRO_CENTRO_CUSTO'
    AND NOT EXISTS (
        SELECT 1
        FROM perfil_modulo pm
        WHERE
            pm.perfil_id = p.id
            AND pm.modulo_id = m.id
    );