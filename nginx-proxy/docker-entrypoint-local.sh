#!/bin/sh
set -e

echo "=== Nginx Proxy SSL Local Entrypoint ==="

# Domínio padrão
DOMAIN="${DOMAIN:-gestao-integrada.local}"
echo "Domínio: $DOMAIN"

# Substituir variáveis no template nginx
export DOMAIN
envsubst '$DOMAIN' < /etc/nginx/templates/app-local.conf.template > /etc/nginx/conf.d/app.conf

# Verificar se certificados existem
if [ ! -f "/etc/nginx/certs/$DOMAIN.crt" ] || [ ! -f "/etc/nginx/certs/$DOMAIN.key" ]; then
    echo "❌ ERRO: Certificados SSL não encontrados!"
    echo ""
    echo "Execute no host antes de iniciar:"
    echo "  ./generate-ssl-cert.sh $DOMAIN"
    echo ""
    exit 1
fi

echo "✓ Certificados SSL encontrados"
echo ""
echo "⚠️  ATENÇÃO: Certificado autoassinado detectado!"
echo "   Navegadores mostrarão aviso de segurança."
echo "   Para produção, use docker-compose.prod.yml com Let's Encrypt"
echo ""

echo "=== Iniciando Nginx ==="
# Executar comando do container (nginx)
exec "$@"
