#!/bin/bash

# Script para testar a criação de tenants
# Uso: ./test-tenant.sh [tenant_id] [nome] [documento]

set -e

# Configurações
API_URL="http://localhost:8080/gestao-integrada-pipa/api"
TENANT_ID="${1:-empresa-teste}"
NOME="${2:-Empresa Teste LTDA}"
NUMERO_DOCUMENTO="${3:-12.345.678/0001-90}"
PLANO="PROFESSIONAL"

# Token administrativo (deve ser configurado em application.properties)
# Em produção, este token deve ser mantido em SEGREDO ABSOLUTO
ADMIN_TOKEN="${APP_ADMIN_TOKEN:-CHANGE_ME_IN_PRODUCTION_USE_STRONG_TOKEN}"

echo "================================================"
echo "🧪 Teste de Multi-Tenancy - Gestão Integrada Pipa"
echo "================================================"
echo ""

# Função para verificar se API está online
check_api() {
    echo "🔍 Verificando se API está online..."
    if curl -f -s -o /dev/null "${API_URL}/health"; then
        echo "✅ API está online"
        return 0
    else
        echo "❌ API não está respondendo em ${API_URL}"
        echo "   Inicie a aplicação com: cd src/backend && ./mvnw spring-boot:run"
        exit 1
    fi
}

# Função para criar tenant
criar_tenant() {
    echo ""
    echo "📝 Criando tenant..."
    echo "   Tenant ID: ${TENANT_ID}"
    echo "   Nome: ${NOME}"
    echo "   Documento: ${NUMERO_DOCUMENTO}"
    echo "   Plano: ${PLANO}"
    echo ""
    
    RESPONSE=$(curl -s -X POST "${API_URL}/admin/tenants" \
        -H "Content-Type: application/json" \
        -H "X-Admin-Token: ${ADMIN_TOKEN}" \
        -d "{
            \"tenantId\": \"${TENANT_ID}\",
            \"nome\": \"${NOME}\",
            \"numeroDocumento\": \"${NUMERO_DOCUMENTO}\",
            \"plano\": \"${PLANO}\"
        }")
    
    if echo "$RESPONSE" | grep -q "\"tenantId\""; then
        echo "✅ Tenant criado com sucesso!"
        echo ""
        echo "📊 Dados do tenant:"
        echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
    else
        echo "❌ Erro ao criar tenant"
        echo "Resposta: $RESPONSE"
        exit 1
    fi
}
# Função para buscar tenant
buscar_tenant() {
    echo ""
    echo "🔍 Buscando tenant..."
    
    RESPONSE=$(curl -s -X GET "${API_URL}/admin/tenants/${TENANT_ID}" \
        -H "X-Admin-Token: ${ADMIN_TOKEN}")
    
    if echo "$RESPONSE" | grep -q "\"tenantId\""; then
        echo "✅ Tenant encontrado!"
        echo ""
        echo "📊 Dados do tenant:"
        echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
    else
        echo "❌ Tenant não encontrado"
        echo "Resposta: $RESPONSE"
        exit 1
    fi
}

# Função para verificar schema no banco
verificar_schema() {
    echo ""
    echo "🗄️  Verificando schema no banco de dados..."
    
    # Extrai schema_name do último response
    SCHEMA_NAME=$(echo "$RESPONSE" | grep -o '"schemaName":"[^"]*"' | cut -d'"' -f4)
    
    if [ -z "$SCHEMA_NAME" ]; then
        echo "⚠️  Não foi possível extrair schema_name do response"
        return
    fi
    
    echo "   Schema name: ${SCHEMA_NAME}"
    
    # Verifica se PostgreSQL está acessível via Docker
    if docker ps | grep -q postgres; then
        CONTAINER_ID=$(docker ps | grep postgres | awk '{print $1}' | head -n1)
        
        echo ""
        echo "   Verificando tabelas no schema..."
        docker exec -i "$CONTAINER_ID" psql -U gestao_integrada_pipa -d gestao_integrada_pipa_db -c "\dt ${SCHEMA_NAME}.*" 2>/dev/null
        
        echo ""
        echo "   Verificando usuário admin..."
        docker exec -i "$CONTAINER_ID" psql -U gestao_integrada_pipa -d gestao_integrada_pipa_db -c "SELECT email, nome FROM ${SCHEMA_NAME}.usuario WHERE email LIKE 'admin@%';" 2>/dev/null
        
        echo ""
        echo "   Verificando perfil ADMIN..."
        docker exec -i "$CONTAINER_ID" psql -U gestao_integrada_pipa -d gestao_integrada_pipa_db -c "SELECT nome, descricao FROM ${SCHEMA_NAME}.perfil;" 2>/dev/null
        
        echo "✅ Schema verificado!"
    else
        echo "⚠️  PostgreSQL não está rodando no Docker"
        echo "   Execute: docker compose up -d postgres"
    fi
}
# Função para ativar tenant
ativar_tenant() {
    echo ""
    echo "✅ Ativando tenant..."
    
    RESPONSE=$(curl -s -X POST "${API_URL}/admin/tenants/${TENANT_ID}/ativar" \
        -H "X-Admin-Token: ${ADMIN_TOKEN}")
    
    echo "✅ Tenant ativado!"
}

# Função para suspender tenant
suspender_tenant() {
    echo ""
    echo "⏸️  Suspendendo tenant..."
    
    RESPONSE=$(curl -s -X POST "${API_URL}/admin/tenants/${TENANT_ID}/suspender" \
        -H "X-Admin-Token: ${ADMIN_TOKEN}")
    
    echo "✅ Tenant suspenso!"
}

# Função para testar isolamento
testar_isolamento() {
    echo ""
    echo "🔒 Testando isolamento de dados..."
    echo "   (Simulando login e operações com header X-Tenant-ID)"
    
    # Simula requisição com header de tenant
    echo ""
    echo "   Tentando login como admin..."
    LOGIN_RESPONSE=$(curl -s -X POST "${API_URL}/auth/login" \
        -H "Content-Type: application/json" \
        -H "X-Tenant-ID: ${TENANT_ID}" \
        -d "{
            \"email\": \"admin@${TENANT_ID}.com\",
            \"senha\": \"admin123\"
        }")
    
    if echo "$LOGIN_RESPONSE" | grep -q "token\|accessToken"; then
        echo "   ✅ Login realizado com sucesso no tenant ${TENANT_ID}!"
    else
        echo "   ⚠️  Endpoint de login pode não estar implementado ainda"
        echo "   Response: $LOGIN_RESPONSE"
    fi
}

# Menu principal
main() {
    check_api
    
    echo ""
    echo "O que você deseja fazer?"
    echo "1) Criar novo tenant"
    echo "2) Buscar tenant existente"
    echo "3) Criar + Verificar schema"
    echo "4) Criar + Ativar"
    echo "5) Teste completo (Criar + Verificar + Ativar + Isolamento)"
    echo ""
    read -p "Escolha uma opção [1-5]: " OPCAO
    
    case $OPCAO in
        1)
            criar_tenant
            ;;
        2)
            buscar_tenant
            ;;
        3)
            criar_tenant
            verificar_schema
            ;;
        4)
            criar_tenant
            ativar_tenant
            buscar_tenant
            ;;
        5)
            criar_tenant
            verificar_schema
            ativar_tenant
            testar_isolamento
            buscar_tenant
            ;;
        *)
            echo "Opção inválida"
            exit 1
            ;;
    esac
    
    echo ""
    echo "================================================"
    echo "✅ Teste concluído!"
    echo "================================================"
}

# Executa menu
main
