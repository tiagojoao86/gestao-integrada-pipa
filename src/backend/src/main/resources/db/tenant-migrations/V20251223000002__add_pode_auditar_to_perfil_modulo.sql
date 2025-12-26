-- Adiciona a coluna pode_auditar à tabela perfil_modulo
ALTER TABLE perfil_modulo
ADD COLUMN pode_auditar BOOLEAN NOT NULL DEFAULT FALSE;

-- Comentário para a coluna
COMMENT ON COLUMN perfil_modulo.pode_auditar IS 'Indica se o perfil pode auditar (visualizar registros excluídos) neste módulo';

-- Atualiza todos os registros do perfil 'Administrador Geral' para ter permissão de auditoria
UPDATE perfil_modulo
SET pode_auditar = TRUE
WHERE perfil_id = '019a7fc4-ab0c-7002-8944-8e0ef009139b'::uuid;
