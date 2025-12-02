#!/bin/bash

###############################################################################
# Script de Setup do Banco de Dados - Gestão Integrada Pipa
# 
# Este script cria o banco de dados, usuário e permissões necessárias
###############################################################################

set -e  # Parar em caso de erro

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funções auxiliares
print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

###############################################################################
# Configurações
###############################################################################

DB_NAME="gestao_integrada_pipa_db"
DB_USER="gestao_integrada_pipa"
DB_PASSWORD="Gsolar#4590@"
POSTGRES_CONTAINER="postgres"

###############################################################################
# Verificar se o container PostgreSQL está rodando
###############################################################################

print_info "Verificando container PostgreSQL..."

if ! docker ps --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}$"; then
    print_error "Container PostgreSQL não está rodando!"
    exit 1
fi

print_success "Container PostgreSQL encontrado"

###############################################################################
# Criar usuário
###############################################################################

print_info "Criando usuário ${DB_USER}..."

docker exec -i ${POSTGRES_CONTAINER} psql -U postgres -c "
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = '${DB_USER}') THEN
        CREATE USER ${DB_USER} WITH PASSWORD '${DB_PASSWORD}';
    END IF;
END
\$\$;
" 2>&1 | grep -v "NOTICE" || true

print_success "Usuário ${DB_USER} criado/verificado"

###############################################################################
# Criar banco de dados
###############################################################################

print_info "Criando banco de dados ${DB_NAME}..."

docker exec -i ${POSTGRES_CONTAINER} psql -U postgres -c "
SELECT 'CREATE DATABASE ${DB_NAME}'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${DB_NAME}')\gexec
" 2>&1 | grep -v "NOTICE" || true

print_success "Banco de dados ${DB_NAME} criado/verificado"

###############################################################################
# Configurar permissões
###############################################################################

print_info "Configurando permissões..."

# Conceder privilégios no banco de dados
docker exec -i ${POSTGRES_CONTAINER} psql -U postgres -c "
GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};
" 2>&1 | grep -v "NOTICE" || true

# Conectar ao banco e conceder privilégios no schema public
docker exec -i ${POSTGRES_CONTAINER} psql -U postgres -d ${DB_NAME} -c "
GRANT ALL PRIVILEGES ON SCHEMA public TO ${DB_USER};
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${DB_USER};
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO ${DB_USER};
" 2>&1 | grep -v "NOTICE" || true

print_success "Permissões configuradas"

###############################################################################
# Verificar configuração
###############################################################################

print_info "Verificando configuração..."

# Testar conexão com o novo usuário
if docker exec -i ${POSTGRES_CONTAINER} psql -U ${DB_USER} -d ${DB_NAME} -c "SELECT 1;" > /dev/null 2>&1; then
    print_success "Conexão verificada com sucesso!"
else
    print_error "Falha ao conectar com o usuário ${DB_USER}"
    exit 1
fi

###############################################################################
# Resumo
###############################################################################

echo ""
print_success "=========================================="
print_success "  Setup concluído com sucesso!"
print_success "=========================================="
echo ""
print_info "Configurações do banco de dados:"
echo "  Database: ${DB_NAME}"
echo "  Usuário: ${DB_USER}"
echo "  Senha: ${DB_PASSWORD}"
echo ""
print_warning "IMPORTANTE: Atualize o arquivo .env com estas credenciais:"
echo "  DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/${DB_NAME}"
echo "  DATABASE_USERNAME=${DB_USER}"
echo "  DATABASE_PASSWORD=${DB_PASSWORD}"
echo ""
