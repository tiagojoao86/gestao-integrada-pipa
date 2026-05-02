-- Remove módulos errados criados pela migration anterior (6 chaves individuais)
DELETE FROM perfil_modulo
WHERE modulo_id IN (
    SELECT id FROM modulo
    WHERE chave IN (
        'LANCAMENTO_FINANCEIRO_LISTAR',
        'LANCAMENTO_FINANCEIRO_SALVAR',
        'LANCAMENTO_FINANCEIRO_DELETAR',
        'LANCAMENTO_FINANCEIRO_AUDITAR',
        'LANCAMENTO_FINANCEIRO_FATURAR',
        'LANCAMENTO_FINANCEIRO_CANCELAR'
    )
);

DELETE FROM modulo
WHERE chave IN (
    'LANCAMENTO_FINANCEIRO_LISTAR',
    'LANCAMENTO_FINANCEIRO_SALVAR',
    'LANCAMENTO_FINANCEIRO_DELETAR',
    'LANCAMENTO_FINANCEIRO_AUDITAR',
    'LANCAMENTO_FINANCEIRO_FATURAR',
    'LANCAMENTO_FINANCEIRO_CANCELAR'
);

-- Insere o módulo correto (idempotente)
INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'LANCAMENTO_FINANCEIRO', 'Lançamento Financeiro', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'LANCAMENTO_FINANCEIRO');

-- Vincula ao perfil Administrador Geral (idempotente)
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
