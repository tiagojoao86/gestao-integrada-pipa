CREATE TABLE pessoa (
    id UUID PRIMARY KEY,
    tipo_pessoa VARCHAR(20) NOT NULL,
    nome VARCHAR(200) NOT NULL,
    email VARCHAR(255),
    telefone VARCHAR(20),

-- Campos específicos de Pessoa Física
cpf VARCHAR(11), data_nascimento DATE,

-- Campos específicos de Pessoa Jurídica
cnpj VARCHAR(14),
razao_social VARCHAR(200),
inscricao_estadual VARCHAR(20),

-- Campos comuns
observacoes TEXT, ativa BOOLEAN NOT NULL DEFAULT true,

-- Auditoria (herdado de BaseEntity)
created_at TIMESTAMP,
created_by VARCHAR(255),
updated_at TIMESTAMP,
updated_by VARCHAR(255),

-- Constraints
CONSTRAINT uk_pessoa_cpf UNIQUE (cpf),
    CONSTRAINT uk_pessoa_cnpj UNIQUE (cnpj),
    CONSTRAINT chk_pessoa_tipo CHECK (
        (tipo_pessoa = 'FISICA' AND cpf IS NOT NULL AND cnpj IS NULL AND razao_social IS NULL AND inscricao_estadual IS NULL) OR
        (tipo_pessoa = 'JURIDICA' AND cnpj IS NOT NULL AND cpf IS NULL AND data_nascimento IS NULL)
    )
);

CREATE INDEX idx_pessoa_tipo ON pessoa (tipo_pessoa);

CREATE INDEX idx_pessoa_cpf ON pessoa (cpf) WHERE cpf IS NOT NULL;

CREATE INDEX idx_pessoa_cnpj ON pessoa (cnpj) WHERE cnpj IS NOT NULL;

CREATE INDEX idx_pessoa_nome ON pessoa (nome);

CREATE INDEX idx_pessoa_ativa ON pessoa (ativa);

-- Passo 9: Comentários nas tabelas
COMMENT ON
TABLE pessoa IS 'Tabela de pessoas (físicas e jurídicas) - modelo flat';

COMMENT ON COLUMN pessoa.tipo_pessoa IS 'Tipo da pessoa: FISICA ou JURIDICA';

COMMENT ON COLUMN pessoa.cpf IS 'CPF (apenas para Pessoa Física)';

COMMENT ON COLUMN pessoa.cnpj IS 'CNPJ (apenas para Pessoa Jurídica)';

COMMENT ON COLUMN pessoa.razao_social IS 'Razão Social (apenas para Pessoa Jurídica)';

COMMENT ON COLUMN pessoa.inscricao_estadual IS 'Inscrição Estadual (apenas para Pessoa Jurídica)';

COMMENT ON COLUMN pessoa.data_nascimento IS 'Data de Nascimento (apenas para Pessoa Física)';

-- Tabela Unidade de Negócio
CREATE TABLE unidade_negocio (
    id UUID NOT NULL DEFAULT gen_random_uuid (),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    codigo VARCHAR(20) NOT NULL,
    nome VARCHAR(200) NOT NULL,
    descricao TEXT,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    cnpj VARCHAR(14) NOT NULL,
    CONSTRAINT pk_unidade_negocio PRIMARY KEY (id),
    CONSTRAINT uk_unidade_negocio_codigo UNIQUE (codigo)
);

COMMENT ON
TABLE unidade_negocio IS 'Unidades de negócio (centros de custo/receita).';

COMMENT ON COLUMN unidade_negocio.cnpj IS 'CNPJ da unidade de negócio (14 dígitos)';

CREATE INDEX idx_unidade_negocio_codigo ON unidade_negocio (codigo);

CREATE INDEX idx_unidade_negocio_ativa ON unidade_negocio (ativa);

-- Criação dos módulos no sistema de permissões
INSERT INTO
    modulo (id, chave, nome, grupo)
VALUES (
        gen_random_uuid (),
        'CADASTRO_PESSOA',
        'Cadastro de Pessoas',
        'CADASTROS'
    ),
    (
        gen_random_uuid (),
        'CADASTRO_UNIDADE_NEGOCIO',
        'Unidades de Negócio',
        'CADASTROS'
    );

-- Migration: Create usuario_unidade_negocio join table
-- Date: 2025-12-08
-- Description: Creates the join table for many-to-many relationship between Usuario and UnidadeNegocio with default flag

CREATE TABLE usuario_unidade_negocio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    usuario_id UUID NOT NULL,
    unidade_negocio_id UUID NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT uk_usuario_unidade_negocio UNIQUE (
        usuario_id,
        unidade_negocio_id
    ),
    CONSTRAINT fk_usuario_unidade_negocio_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_unidade_negocio_unidade FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio (id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_usuario_unidade_negocio_usuario ON usuario_unidade_negocio (usuario_id);

CREATE INDEX idx_usuario_unidade_negocio_unidade ON usuario_unidade_negocio (unidade_negocio_id);

CREATE INDEX idx_usuario_unidade_negocio_default ON usuario_unidade_negocio (usuario_id, is_default)
WHERE
    is_default = TRUE;

-- Add comment
COMMENT ON
TABLE usuario_unidade_negocio IS 'Join table for usuario and unidade_negocio many-to-many relationship with default flag';

COMMENT ON COLUMN usuario_unidade_negocio.is_default IS 'Indicates if this is the default unidade de negocio for the user';

-- Vincular módulos ao perfil 'Administrador Geral' com todas as permissões
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
    AND m.chave IN (
        'CADASTRO_PESSOA',
        'CADASTRO_UNIDADE_NEGOCIO'
    );