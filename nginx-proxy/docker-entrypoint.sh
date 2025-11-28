#!/bin/sh
set -e

echo "=== Nginx Proxy SSL Entrypoint ==="

# Verificar variáveis de ambiente obrigatórias
if [ -z "$DOMAIN" ]; then
    echo "ERRO: Variável DOMAIN não definida!"
    exit 1
fi

if [ -z "$EMAIL" ]; then
    echo "ERRO: Variável EMAIL não definida!"
    exit 1
fi

echo "Domínio: $DOMAIN"
echo "Email: $EMAIL"

# Substituir variáveis no template nginx
export DOMAIN
envsubst '$DOMAIN' < /etc/nginx/templates/app.conf.template > /etc/nginx/conf.d/app.conf

# Verificar se já existem certificados
if [ -f "/etc/letsencrypt/live/$DOMAIN/fullchain.pem" ]; then
    echo "✓ Certificados SSL já existem para $DOMAIN"
else
    echo "⚠ Certificados SSL não encontrados. Gerando certificados..."
    
    # Verificar se deve usar certificado autoassinado
    if [ "$USE_SELF_SIGNED" = "true" ]; then
        echo "Gerando certificado autoassinado para $DOMAIN..."
        
        # Criar diretório para certificados
        mkdir -p /etc/letsencrypt/live/$DOMAIN
        
        # Gerar certificado autoassinado
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout /etc/letsencrypt/live/$DOMAIN/privkey.pem \
            -out /etc/letsencrypt/live/$DOMAIN/fullchain.pem \
            -subj "/C=BR/ST=State/L=City/O=Organization/CN=$DOMAIN"
        
        echo "✓ Certificado autoassinado criado com sucesso!"
        
        # Aplicar configuração HTTPS
        envsubst '$DOMAIN' < /etc/nginx/templates/app.conf.template > /etc/nginx/conf.d/app.conf
    else
        echo "Solicitando certificado SSL do Let's Encrypt..."
        
        # Criar configuração temporária para HTTP apenas (para validação do certbot)
        cat > /etc/nginx/conf.d/app.conf <<EOF
server {
    listen 80;
    server_name $DOMAIN;
    
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }
    
    location / {
        return 200 'Aguardando certificado SSL...';
        add_header Content-Type text/plain;
    }
}
EOF
        
        # Iniciar nginx em background para validação do certbot
        nginx
        
        certbot certonly --webroot \
            --webroot-path=/var/www/certbot \
            --email "$EMAIL" \
            --agree-tos \
            --no-eff-email \
            --force-renewal \
            -d "$DOMAIN"
        
        if [ $? -eq 0 ]; then
            echo "✓ Certificado SSL obtido com sucesso!"
            
            # Parar nginx temporário
            nginx -s stop
            sleep 2
            
            # Recriar configuração com HTTPS
            envsubst '$DOMAIN' < /etc/nginx/templates/app.conf.template > /etc/nginx/conf.d/app.conf
        else
            echo "✗ Falha ao obter certificado SSL!"
            echo "Verifique se:"
            echo "  1. O domínio $DOMAIN está apontando para este servidor"
            echo "  2. As portas 80 e 443 estão abertas no firewall"
            echo "  3. O email $EMAIL é válido"
            exit 1
        fi
    fi
fi

# Agendar renovação automática de certificados (executar a cada 12 horas)
echo "Configurando renovação automática de certificados..."
(
    while :; do
        sleep 12h
        echo "Verificando renovação de certificados..."
        certbot renew --webroot --webroot-path=/var/www/certbot --quiet
        nginx -s reload
    done
) &

echo "=== Iniciando Nginx ==="
# Executar comando do container (nginx)
exec "$@"
