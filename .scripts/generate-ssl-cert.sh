#!/bin/bash
set -e

echo "=== Gerador de Certificado SSL Autoassinado ==="
echo ""

# Diretório de certificados
CERT_DIR="./nginx-proxy/certs"
mkdir -p "$CERT_DIR"

# Nome do domínio (padrão: gestao-integrada.local)
DOMAIN="${1:-gestao-integrada.local}"

echo "Domínio: $DOMAIN"
echo "Diretório: $CERT_DIR"
echo ""

# Verificar se já existe certificado
if [ -f "$CERT_DIR/$DOMAIN.crt" ] && [ -f "$CERT_DIR/$DOMAIN.key" ]; then
    echo "⚠️  Certificado já existe para $DOMAIN"
    read -p "Deseja recriar? (s/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo "Operação cancelada."
        exit 0
    fi
    echo "Removendo certificados antigos..."
    rm -f "$CERT_DIR/$DOMAIN.crt" "$CERT_DIR/$DOMAIN.key"
fi

echo "Gerando certificado SSL autoassinado..."

# Gerar chave privada e certificado
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout "$CERT_DIR/$DOMAIN.key" \
    -out "$CERT_DIR/$DOMAIN.crt" \
    -subj "/C=BR/ST=State/L=City/O=Organization/OU=IT/CN=$DOMAIN" \
    -addext "subjectAltName=DNS:$DOMAIN,DNS:*.$DOMAIN,IP:192.168.3.200"

echo ""
echo "✓ Certificado SSL criado com sucesso!"
echo ""
echo "Arquivos gerados:"
echo "  - Certificado: $CERT_DIR/$DOMAIN.crt"
echo "  - Chave privada: $CERT_DIR/$DOMAIN.key"
echo ""
echo "⚠️  ATENÇÃO: Este é um certificado autoassinado para TESTES."
echo "    Navegadores irão mostrar um aviso de segurança."
echo "    Para produção, use Let's Encrypt (docker-compose.prod.yml)"
echo ""
echo "Validade: 365 dias"
echo ""
