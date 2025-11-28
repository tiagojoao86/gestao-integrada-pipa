#!/bin/bash

###############################################################################
# Script de Deploy - Gestão Integrada
# 
# Este script automatiza o deploy da aplicação usando imagens do Docker Hub
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
# Verificar pré-requisitos
###############################################################################

print_info "Verificando pré-requisitos..."

# Verificar se Docker está instalado
if ! command -v docker &> /dev/null; then
    print_error "Docker não está instalado!"
    exit 1
fi
print_success "Docker instalado"

# Verificar se Docker Compose está instalado
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    print_error "Docker Compose não está instalado!"
    exit 1
fi
print_success "Docker Compose instalado"

# Verificar se arquivo .env existe
if [ ! -f ".env" ]; then
    print_error "Arquivo .env não encontrado!"
    print_info "Copie .env.example para .env e configure as variáveis"
    exit 1
fi
print_success "Arquivo .env encontrado"

###############################################################################
# Carregar variáveis de ambiente
###############################################################################

print_info "Carregando variáveis de ambiente..."
source .env

# Verificar variáveis obrigatórias
if [ -z "$DOCKERHUB_USERNAME" ]; then
    print_error "DOCKERHUB_USERNAME não definido no .env"
    exit 1
fi

if [ -z "$DATABASE_URL" ]; then
    print_error "DATABASE_URL não definido no .env"
    exit 1
fi

print_success "Variáveis carregadas"

###############################################################################
# Pull das imagens
###############################################################################

print_info "Baixando imagens do Docker Hub..."

IMAGE_TAG=${IMAGE_TAG:-latest}

docker pull ${DOCKERHUB_USERNAME}/gestao-integrada-frontend:${IMAGE_TAG} || {
    print_error "Falha ao baixar imagem do frontend"
    exit 1
}

docker pull ${DOCKERHUB_USERNAME}/gestao-integrada-backend:${IMAGE_TAG} || {
    print_error "Falha ao baixar imagem do backend"
    exit 1
}

docker pull ${DOCKERHUB_USERNAME}/gestao-integrada-nginx-proxy:${IMAGE_TAG} || {
    print_error "Falha ao baixar imagem do nginx-proxy"
    exit 1
}

print_success "Imagens baixadas com sucesso"

###############################################################################
# Parar containers antigos
###############################################################################

print_info "Parando containers antigos (se existirem)..."

if docker-compose -f docker-compose.deploy.yml ps -q 2>/dev/null | grep -q .; then
    docker-compose -f docker-compose.deploy.yml down
    print_success "Containers antigos parados"
else
    print_info "Nenhum container rodando"
fi

###############################################################################
# Iniciar aplicação
###############################################################################

print_info "Iniciando aplicação..."

docker-compose -f docker-compose.deploy.yml up -d

print_success "Aplicação iniciada!"

###############################################################################
# Verificar status
###############################################################################

print_info "Aguardando containers iniciarem..."
sleep 5

print_info "Status dos containers:"
docker-compose -f docker-compose.deploy.yml ps

###############################################################################
# Exibir logs
###############################################################################

echo ""
print_info "Para ver os logs, execute:"
echo "  docker-compose -f docker-compose.deploy.yml logs -f"
echo ""

print_info "Para ver logs de um container específico:"
echo "  docker logs -f gestao-integrada-frontend"
echo "  docker logs -f gestao-integrada-backend"
echo "  docker logs -f gestao-integrada-nginx-proxy"
echo ""

###############################################################################
# Informações de acesso
###############################################################################

echo ""
print_success "=========================================="
print_success "  Deploy concluído com sucesso!"
print_success "=========================================="
echo ""

if [ "$USE_SELF_SIGNED" = "true" ] || [ -z "$DOMAIN" ] || [ "$DOMAIN" = "gestaointegrada.local" ]; then
    print_info "Acesse a aplicação em:"
    echo "  https://192.168.3.200 (ou IP do servidor)"
    echo ""
    print_warning "Certificado autoassinado! Aceite o aviso de segurança no navegador."
else
    print_info "Acesse a aplicação em:"
    echo "  https://$DOMAIN"
fi

echo ""
print_info "Credenciais padrão:"
echo "  Usuário: admin"
echo "  Senha: @RLthotr$&u=Huge1e-r"
echo ""
print_warning "IMPORTANTE: Altere a senha após o primeiro acesso!"
echo ""
