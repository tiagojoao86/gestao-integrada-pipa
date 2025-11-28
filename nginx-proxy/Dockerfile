FROM nginx:stable-alpine

# Instalar certbot e dependências
RUN apk add --no-cache certbot certbot-nginx openssl

# Criar diretórios necessários
RUN mkdir -p /var/www/certbot
RUN mkdir -p /etc/letsencrypt

# Copiar template de configuração
COPY conf.d/app.conf.template /etc/nginx/templates/

# Copiar script de inicialização
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Expor portas
EXPOSE 80 443

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
