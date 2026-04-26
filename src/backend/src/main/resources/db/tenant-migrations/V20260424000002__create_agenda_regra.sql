DO $$ BEGIN

  -- Tabela principal
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'agenda_regra') THEN
    CREATE TABLE agenda_regra (
      id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      agenda_id                UUID NOT NULL,
      data_inicio              DATE NOT NULL,
      data_fim                 DATE,
      hora_inicio              TIME NOT NULL,
      hora_fim                 TIME NOT NULL,
      duracao_sessao_minutos   INTEGER NOT NULL,
      deleted                  BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at               TIMESTAMP,
      deleted_by               VARCHAR(255),
      created_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at               TIMESTAMP,
      created_by               VARCHAR(255),
      updated_by               VARCHAR(255),
      CONSTRAINT fk_agenda_regra_agenda FOREIGN KEY (agenda_id) REFERENCES agenda(id)
    );
    CREATE INDEX idx_agenda_regra_deleted ON agenda_regra (deleted);
    CREATE INDEX idx_agenda_regra_agenda_id ON agenda_regra (agenda_id);
  END IF;

  -- Tabela de dias da semana
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'agenda_regra_dia_semana') THEN
    CREATE TABLE agenda_regra_dia_semana (
      agenda_regra_id  UUID NOT NULL,
      dia_semana       VARCHAR(3) NOT NULL,
      CONSTRAINT fk_agenda_regra_ds_regra FOREIGN KEY (agenda_regra_id) REFERENCES agenda_regra(id)
    );
  END IF;

  -- Tabela de convênios
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'agenda_regra_convenio') THEN
    CREATE TABLE agenda_regra_convenio (
      agenda_regra_id  UUID NOT NULL,
      convenio_id      UUID NOT NULL,
      PRIMARY KEY (agenda_regra_id, convenio_id),
      CONSTRAINT fk_agenda_regra_conv_regra    FOREIGN KEY (agenda_regra_id) REFERENCES agenda_regra(id),
      CONSTRAINT fk_agenda_regra_conv_convenio FOREIGN KEY (convenio_id)     REFERENCES convenio(id)
    );
  END IF;

  -- Tabela de procedimentos
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'agenda_regra_procedimento') THEN
    CREATE TABLE agenda_regra_procedimento (
      agenda_regra_id  UUID NOT NULL,
      procedimento_id  UUID NOT NULL,
      PRIMARY KEY (agenda_regra_id, procedimento_id),
      CONSTRAINT fk_agenda_regra_proc_regra        FOREIGN KEY (agenda_regra_id) REFERENCES agenda_regra(id),
      CONSTRAINT fk_agenda_regra_proc_procedimento FOREIGN KEY (procedimento_id) REFERENCES procedimento(id)
    );
  END IF;

  -- Módulo
  INSERT INTO modulo (id, chave, nome, grupo)
  SELECT gen_random_uuid(), 'AGENDAMENTO_AGENDA_REGRA', 'Regras de Agenda', 'AGENDAMENTO'
  WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'AGENDAMENTO_AGENDA_REGRA');

  -- Permissões para Administrador Geral
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
    AND m.chave = 'AGENDAMENTO_AGENDA_REGRA'
    AND NOT EXISTS (
      SELECT 1 FROM perfil_modulo pm
      WHERE pm.perfil_id = p.id AND pm.modulo_id = m.id
    );

END $$;
