DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_schema = current_schema()
                   AND table_name = 'agenda') THEN

        CREATE TABLE agenda (
            id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            nome            VARCHAR(255) NOT NULL,
            profissional_id UUID NOT NULL,
            setor_id        UUID NOT NULL,
            ativo           BOOLEAN NOT NULL DEFAULT TRUE,

            created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at  TIMESTAMP,
            created_by  VARCHAR(255),
            updated_by  VARCHAR(255),

            deleted     BOOLEAN NOT NULL DEFAULT FALSE,
            deleted_at  TIMESTAMP,
            deleted_by  VARCHAR(255)
        );

        ALTER TABLE agenda
            ADD CONSTRAINT fk_agenda_profissional
            FOREIGN KEY (profissional_id) REFERENCES profissional(id);

        ALTER TABLE agenda
            ADD CONSTRAINT fk_agenda_setor
            FOREIGN KEY (setor_id) REFERENCES setor(id);

        CREATE INDEX idx_agenda_deleted       ON agenda (deleted);
        CREATE INDEX idx_agenda_profissional  ON agenda (profissional_id);
        CREATE INDEX idx_agenda_setor         ON agenda (setor_id);

    END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'AGENDAMENTO_AGENDA', 'Agendas', 'AGENDAMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'AGENDAMENTO_AGENDA');

INSERT INTO perfil_modulo (
    id, perfil_id, modulo_id,
    pode_listar, pode_visualizar, pode_editar, pode_deletar, pode_auditar,
    created_at, created_by
)
SELECT
    gen_random_uuid(), p.id, m.id,
    TRUE, TRUE, TRUE, TRUE, TRUE,
    CURRENT_TIMESTAMP, 'migration'
FROM perfil p
CROSS JOIN modulo m
WHERE p.nome = 'Administrador Geral'
  AND m.chave = 'AGENDAMENTO_AGENDA'
  AND NOT EXISTS (
    SELECT 1 FROM perfil_modulo pm
    WHERE pm.perfil_id = p.id AND pm.modulo_id = m.id
  );
