DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'atendimento') THEN
    CREATE TABLE atendimento (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      data_hora TIMESTAMP NOT NULL,
      setor_id UUID NOT NULL,
      paciente_id UUID NOT NULL,
      responsavel_id UUID,
      convenio_id UUID,
      convenio_categoria_id UUID,
      profissional_atendimento_id UUID NOT NULL,
      profissional_responsavel_id UUID NOT NULL,
      procedimento_id UUID NOT NULL,
      tabela_item_id UUID,
      status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
      observacoes TEXT,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT fk_atendimento_setor FOREIGN KEY (setor_id) REFERENCES setor(id),
      CONSTRAINT fk_atendimento_paciente FOREIGN KEY (paciente_id) REFERENCES pessoa(id),
      CONSTRAINT fk_atendimento_responsavel FOREIGN KEY (responsavel_id) REFERENCES pessoa(id),
      CONSTRAINT fk_atendimento_convenio FOREIGN KEY (convenio_id) REFERENCES convenio(id),
      CONSTRAINT fk_atendimento_convenio_categoria
        FOREIGN KEY (convenio_categoria_id) REFERENCES convenio_categoria(id),
      CONSTRAINT fk_atendimento_prof_atendimento
        FOREIGN KEY (profissional_atendimento_id) REFERENCES profissional(id),
      CONSTRAINT fk_atendimento_prof_responsavel
        FOREIGN KEY (profissional_responsavel_id) REFERENCES profissional(id),
      CONSTRAINT fk_atendimento_procedimento FOREIGN KEY (procedimento_id) REFERENCES procedimento(id),
      CONSTRAINT fk_atendimento_tabela_item FOREIGN KEY (tabela_item_id) REFERENCES tabela_item(id),
      CONSTRAINT ck_atendimento_status
        CHECK (status IN ('AGENDADO','REALIZADO','CANCELADO','FALTOU'))
    );
    CREATE INDEX idx_atendimento_deleted ON atendimento (deleted);
    CREATE INDEX idx_atendimento_data_hora ON atendimento (data_hora);
    CREATE INDEX idx_atendimento_paciente ON atendimento (paciente_id);
    CREATE INDEX idx_atendimento_profissional_atendimento ON atendimento (profissional_atendimento_id);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO', 'Atendimento', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO');

INSERT INTO perfil_modulo (id, perfil_id, modulo_id, pode_listar, pode_visualizar, pode_editar,
    pode_deletar, pode_auditar)
SELECT gen_random_uuid(), p.id, m.id, TRUE, TRUE, TRUE, TRUE, TRUE
FROM perfil p, modulo m
WHERE p.admin = TRUE AND m.chave = 'ATENDIMENTO'
AND NOT EXISTS (
    SELECT 1 FROM perfil_modulo pm WHERE pm.perfil_id = p.id AND pm.modulo_id = m.id
);
