-- Tenant migration: create setor table (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'setor') THEN
        CREATE TABLE setor (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            nome VARCHAR(200) NOT NULL,
            descricao VARCHAR(500),
            centro_custo_id UUID NOT NULL,
            created_at TIMESTAMP WITHOUT TIME ZONE,
            updated_at TIMESTAMP WITHOUT TIME ZONE,
            created_by VARCHAR(255),
            updated_by VARCHAR(255)
        );
        ALTER TABLE setor
          ADD CONSTRAINT uk_setor_nome UNIQUE (nome);
        ALTER TABLE setor
          ADD CONSTRAINT fk_setor_centro_custo FOREIGN KEY (centro_custo_id) REFERENCES centro_custo(id);
    END IF;
EXCEPTION WHEN others THEN
    RAISE NOTICE 'tenant migration create_setor_table: %', SQLERRM;
END $$;

INSERT INTO
    modulo (id, chave, nome, grupo)
SELECT gen_random_uuid (), 'CADASTRO_SETOR', 'Cadastro de Setor', 'CADASTROS'
WHERE
    NOT EXISTS (
        SELECT 1
        FROM modulo
        WHERE
            chave = 'CADASTRO_SETOR'
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
    AND m.chave = 'CADASTRO_SETOR'
    AND NOT EXISTS (
        SELECT 1
        FROM perfil_modulo pm
        WHERE
            pm.perfil_id = p.id
            AND pm.modulo_id = m.id
    );
