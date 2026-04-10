INSERT INTO perfil_modulo (
    id, perfil_id, modulo_id,
    pode_listar, pode_visualizar, pode_editar, pode_deletar, pode_auditar,
    created_at, created_by
)
SELECT
    gen_random_uuid(),
    p.id,
    m.id,
    TRUE, TRUE, TRUE, TRUE, TRUE,
    CURRENT_TIMESTAMP, 'migration'
FROM perfil p
    CROSS JOIN modulo m
WHERE
    p.nome = 'Administrador Geral'
    AND m.chave = 'ATENDIMENTO_CONVENIO'
    AND NOT EXISTS (
        SELECT 1 FROM perfil_modulo pm
        WHERE pm.perfil_id = p.id
          AND pm.modulo_id = m.id
    );
