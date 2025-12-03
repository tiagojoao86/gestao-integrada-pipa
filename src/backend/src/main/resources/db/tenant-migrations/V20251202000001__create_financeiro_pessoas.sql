-- Criação das tabelas de Pessoa (JOINED inheritance)
CREATE TABLE pessoa (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    nome VARCHAR(200) NOT NULL,
    email VARCHAR(255),
    telefone VARCHAR(11),
    observacoes TEXT,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_pessoa PRIMARY KEY (id)
);

COMMENT ON TABLE pessoa IS 'Tabela base para pessoas físicas e jurídicas (JOINED inheritance).';

CREATE TABLE pessoa_fisica (
    id UUID NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    data_nascimento DATE,
    CONSTRAINT pk_pessoa_fisica PRIMARY KEY (id),
    CONSTRAINT fk_pessoa_fisica_pessoa FOREIGN KEY (id) REFERENCES pessoa(id) ON DELETE CASCADE,
    CONSTRAINT uk_pessoa_fisica_cpf UNIQUE (cpf)
);

COMMENT ON TABLE pessoa_fisica IS 'Pessoas físicas com CPF.';

CREATE TABLE pessoa_juridica (
    id UUID NOT NULL,
    cnpj VARCHAR(14) NOT NULL,
    razao_social VARCHAR(200) NOT NULL,
    nome_fantasia VARCHAR(200),
    inscricao_estadual VARCHAR(20),
    CONSTRAINT pk_pessoa_juridica PRIMARY KEY (id),
    CONSTRAINT fk_pessoa_juridica_pessoa FOREIGN KEY (id) REFERENCES pessoa(id) ON DELETE CASCADE,
    CONSTRAINT uk_pessoa_juridica_cnpj UNIQUE (cnpj)
);

COMMENT ON TABLE pessoa_juridica IS 'Pessoas jurídicas com CNPJ.';

-- Índices para melhorar performance
CREATE INDEX idx_pessoa_nome ON pessoa(nome);
CREATE INDEX idx_pessoa_email ON pessoa(email);
CREATE INDEX idx_pessoa_ativa ON pessoa(ativa);
CREATE INDEX idx_pessoa_fisica_cpf ON pessoa_fisica(cpf);
CREATE INDEX idx_pessoa_juridica_cnpj ON pessoa_juridica(cnpj);

-- Tabela Unidade de Negócio
CREATE TABLE unidade_negocio (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    codigo VARCHAR(20) NOT NULL,
    nome VARCHAR(200) NOT NULL,
    descricao TEXT,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_unidade_negocio PRIMARY KEY (id),
    CONSTRAINT uk_unidade_negocio_codigo UNIQUE (codigo)
);

COMMENT ON TABLE unidade_negocio IS 'Unidades de negócio (centros de custo/receita).';

CREATE INDEX idx_unidade_negocio_codigo ON unidade_negocio(codigo);
CREATE INDEX idx_unidade_negocio_ativa ON unidade_negocio(ativa);

-- Criação dos módulos no sistema de permissões
INSERT INTO modulo (id, chave, nome, grupo)
VALUES 
    (gen_random_uuid(), 'CADASTRO_PESSOA', 'Cadastro de Pessoas', 'CADASTROS'),
    (gen_random_uuid(), 'CADASTRO_UNIDADE_NEGOCIO', 'Unidades de Negócio', 'CADASTROS');

-- Vincular módulos ao perfil 'Administrador Geral' com todas as permissões
INSERT INTO perfil_modulo (id, perfil_id, modulo_id, pode_listar, pode_visualizar, pode_editar, pode_deletar, created_at, created_by)
SELECT 
    gen_random_uuid(),
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
WHERE p.nome = 'Administrador Geral'
  AND m.chave IN ('CADASTRO_PESSOA', 'CADASTRO_UNIDADE_NEGOCIO');
