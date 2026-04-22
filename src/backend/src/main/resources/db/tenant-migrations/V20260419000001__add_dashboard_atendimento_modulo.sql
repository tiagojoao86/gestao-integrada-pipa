-- Módulo: Dashboard Atendimento - Atendimentos por Mês
INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'DASHBOARD_ATENDIMENTO_POR_MES', 'Atendimentos por Mês', 'DASHBOARDS'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'DASHBOARD_ATENDIMENTO_POR_MES');

-- Permissão de LISTAR para o perfil 'Administrador Geral'
INSERT INTO perfil_modulo (
    id, perfil_id, modulo_id,
    pode_listar, pode_visualizar, pode_editar, pode_deletar, pode_auditar,
    created_at, created_by
)
SELECT
    gen_random_uuid(),
    '019a7fc4-ab0c-7002-8944-8e0ef009139b'::uuid,
    m.id,
    TRUE, FALSE, FALSE, FALSE, FALSE,
    CURRENT_TIMESTAMP, 'migration'
FROM modulo m
WHERE m.chave = 'DASHBOARD_ATENDIMENTO_POR_MES'
  AND NOT EXISTS (
    SELECT 1 FROM perfil_modulo pm
    WHERE pm.perfil_id = '019a7fc4-ab0c-7002-8944-8e0ef009139b'::uuid
      AND pm.modulo_id = m.id
  );
