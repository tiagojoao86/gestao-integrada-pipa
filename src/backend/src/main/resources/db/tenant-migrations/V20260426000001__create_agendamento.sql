DO $$ BEGIN

  -- Tabela principal de agendamentos
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'agendamento') THEN
    CREATE TABLE agendamento (
      id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      agenda_id        UUID NOT NULL,
      paciente_id      UUID NOT NULL,
      convenio_id      UUID,
      procedimento_id  UUID,
      observacao       TEXT,
      status           VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
      deleted          BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at       TIMESTAMP,
      deleted_by       VARCHAR(255),
      created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at       TIMESTAMP,
      created_by       VARCHAR(255),
      updated_by       VARCHAR(255),
      CONSTRAINT fk_agendamento_agenda       FOREIGN KEY (agenda_id)       REFERENCES agenda(id),
      CONSTRAINT fk_agendamento_paciente     FOREIGN KEY (paciente_id)     REFERENCES pessoa(id),
      CONSTRAINT fk_agendamento_convenio     FOREIGN KEY (convenio_id)     REFERENCES convenio(id),
      CONSTRAINT fk_agendamento_procedimento FOREIGN KEY (procedimento_id) REFERENCES procedimento(id),
      CONSTRAINT chk_agendamento_status      CHECK (status IN ('AGENDADO','CANCELADO','REALIZADO'))
    );
    CREATE INDEX idx_agendamento_deleted    ON agendamento (deleted);
    CREATE INDEX idx_agendamento_agenda_id  ON agendamento (agenda_id);
    CREATE INDEX idx_agendamento_paciente_id ON agendamento (paciente_id);
    CREATE INDEX idx_agendamento_status     ON agendamento (status);
  END IF;

  -- Tabela de horários do agendamento
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'agendamento_horario') THEN
    CREATE TABLE agendamento_horario (
      id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      agendamento_id   UUID NOT NULL,
      data_hora_inicio TIMESTAMP NOT NULL,
      data_hora_fim    TIMESTAMP NOT NULL,
      CONSTRAINT fk_agendamento_horario_agendamento
        FOREIGN KEY (agendamento_id) REFERENCES agendamento(id)
    );
    CREATE INDEX idx_agendamento_horario_agendamento_id
      ON agendamento_horario (agendamento_id);
    CREATE INDEX idx_agendamento_horario_inicio
      ON agendamento_horario (data_hora_inicio);
  END IF;

  -- Módulo
  INSERT INTO modulo (id, chave, nome, grupo)
  SELECT gen_random_uuid(), 'AGENDAMENTO_AGENDAMENTO', 'Agendamento', 'AGENDAMENTO'
  WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'AGENDAMENTO_AGENDAMENTO');

  -- Permissões para o perfil Administrador Geral
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
  WHERE m.chave = 'AGENDAMENTO_AGENDAMENTO'
    AND NOT EXISTS (
      SELECT 1 FROM perfil_modulo pm
      WHERE pm.perfil_id = '019a7fc4-ab0c-7002-8944-8e0ef009139b'::uuid
        AND pm.modulo_id = m.id
    );

END $$;
