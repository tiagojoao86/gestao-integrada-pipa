-- Migration: Converter Pessoa de modelo JOINED para modelo FLAT
-- Data: 2025-12-05
-- Descrição: Remove herança JOINED (pessoa_fisica e pessoa_juridica) e consolida tudo em uma única tabela pessoa

-- Passo 1: Criar nova tabela pessoa com modelo flat
CREATE TABLE pessoa_new (
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

-- Passo 2: Migrar dados de Pessoa Física
INSERT INTO
    pessoa_new (
        id,
        tipo_pessoa,
        nome,
        email,
        telefone,
        cpf,
        data_nascimento,
        cnpj,
        razao_social,
        inscricao_estadual,
        observacoes,
        ativa,
        created_at,
        created_by,
        updated_at,
        updated_by
    )
SELECT
    p.id,
    'FISICA' as tipo_pessoa,
    p.nome,
    p.email,
    p.telefone,
    pf.cpf,
    pf.data_nascimento,
    NULL as cnpj,
    NULL as razao_social,
    NULL as inscricao_estadual,
    p.observacoes,
    p.ativa,
    p.created_at,
    p.created_by,
    p.updated_at,
    p.updated_by
FROM pessoa p
    INNER JOIN pessoa_fisica pf ON p.id = pf.id;

-- Passo 3: Migrar dados de Pessoa Jurídica
INSERT INTO
    pessoa_new (
        id,
        tipo_pessoa,
        nome,
        email,
        telefone,
        cpf,
        data_nascimento,
        cnpj,
        razao_social,
        inscricao_estadual,
        observacoes,
        ativa,
        created_at,
        created_by,
        updated_at,
        updated_by
    )
SELECT
    p.id,
    'JURIDICA' as tipo_pessoa,
    p.nome,
    p.email,
    p.telefone,
    NULL as cpf,
    NULL as data_nascimento,
    pj.cnpj,
    pj.razao_social,
    pj.inscricao_estadual,
    p.observacoes,
    p.ativa,
    p.created_at,
    p.created_by,
    p.updated_at,
    p.updated_by
FROM pessoa p
    INNER JOIN pessoa_juridica pj ON p.id = pj.id;

-- Passo 4: Verificar se todos os registros foram migrados
DO $$
DECLARE
    count_original INTEGER;
    count_migrado INTEGER;
BEGIN
    SELECT COUNT(*) INTO count_original FROM pessoa;
    SELECT COUNT(*) INTO count_migrado FROM pessoa_new;
    
    IF count_original != count_migrado THEN
        RAISE EXCEPTION 'Migração falhou: Original tem % registros, migrado tem %', count_original, count_migrado;
    END IF;
    
    RAISE NOTICE 'Migração OK: % registros migrados com sucesso', count_migrado;
END $$;

-- Passo 5: Atualizar referências de foreign keys (se existirem)
-- Exemplo: Se titulo tem FK para pessoa, não precisa alterar pois o ID permanece o mesmo

-- Passo 6: Drop tabelas antigas
DROP TABLE IF EXISTS pessoa_fisica CASCADE;

DROP TABLE IF EXISTS pessoa_juridica CASCADE;

DROP TABLE IF EXISTS pessoa CASCADE;

-- Passo 7: Renomear nova tabela
ALTER TABLE pessoa_new RENAME TO pessoa;

-- Passo 8: Criar índices para performance
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

-- Verificação final
SELECT tipo_pessoa, COUNT(*) as quantidade
FROM pessoa
GROUP BY
    tipo_pessoa
ORDER BY tipo_pessoa;