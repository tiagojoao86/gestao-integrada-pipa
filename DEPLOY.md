# 🚀 Guia de Deploy - Produção com HTTPS

Este guia descreve como fazer o deploy da aplicação **Gestão Integrada** em produção com HTTPS automático usando Let's Encrypt.

## 📋 Pré-requisitos

### 1. Servidor
- **Sistema Operacional:** Linux (Ubuntu/Debian recomendado)
- **Recursos Mínimos:** 2 CPU, 4GB RAM, 20GB disco
- **Software Necessário:**
  - Docker (versão 20.10+)
  - Docker Compose (versão 2.0+)
  - Git

### 2. Domínio
- Um domínio registrado (ex: `seudominio.com.br`)
- Acesso ao painel DNS do domínio

### 3. Banco de Dados
- PostgreSQL acessível (pode ser no mesmo servidor ou externo)
- Database criado previamente
- Usuário e senha configurados

## 🔧 Passo a Passo

### 1️⃣ Configurar DNS

Configure o DNS do seu domínio para apontar para o IP do servidor:

```
Tipo: A
Nome: @ (ou subdomínio, ex: app)
Valor: IP_DO_SEU_SERVIDOR
TTL: 3600
```

**Importante:** Aguarde a propagação do DNS (pode levar de minutos a horas). Verifique com:
```bash
nslookup seudominio.com.br
```

### 2️⃣ Configurar Firewall

Libere as portas necessárias:

```bash
# Ubuntu/Debian com ufw
sudo ufw allow 80/tcp    # HTTP (necessário para validação Let's Encrypt)
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable

# CentOS/RHEL com firewalld
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

### 3️⃣ Clonar o Repositório

```bash
cd /opt
sudo git clone https://github.com/tiagojoao86/gestao-integrada.git
cd gestao-integrada
```

### 4️⃣ Configurar Variáveis de Ambiente

Copie o arquivo de exemplo e edite com seus dados:

```bash
cp .env.example .env
nano .env
```

**Preencha as variáveis:**

```bash
# SSL / DOMÍNIO
DOMAIN=seudominio.com.br
EMAIL=seu-email@exemplo.com

# BANCO DE DADOS
DATABASE_URL=jdbc:postgresql://localhost:5432/gestao_integrada_db
DATABASE_USERNAME=seu_usuario
DATABASE_PASSWORD=sua_senha_forte
```

**⚠️ IMPORTANTE:** Nunca commite o arquivo `.env` no Git!

### 5️⃣ Preparar Banco de Dados

Se o PostgreSQL estiver no mesmo servidor:

```bash
# Criar database
sudo -u postgres psql -c "CREATE DATABASE gestao_integrada_db;"
sudo -u postgres psql -c "CREATE USER seu_usuario WITH PASSWORD 'sua_senha_forte';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE gestao_integrada_db TO seu_usuario;"
```

**Nota:** O Flyway irá criar as tabelas automaticamente na primeira execução.

### 6️⃣ Build e Deploy

```bash
# Build das imagens
docker-compose -f docker-compose.prod.yml build

# Iniciar os containers
docker-compose -f docker-compose.prod.yml up -d
```

### 7️⃣ Verificar Status

```bash
# Ver logs do nginx-proxy (certificado SSL)
docker logs gestao-integrada-nginx-proxy

# Ver logs do backend
docker logs gestao-integrada-backend

# Ver logs do frontend
docker logs gestao-integrada-frontend

# Ver status dos containers
docker-compose -f docker-compose.prod.yml ps
```

**Esperado no log do nginx-proxy:**
```
✓ Certificado SSL obtido com sucesso!
=== Iniciando Nginx ===
```

### 8️⃣ Testar a Aplicação

Acesse no navegador:
```
https://seudominio.com.br
```

**Credenciais padrão:**
- Usuário: `admin`
- Senha: `@RLthotr$&u=Huge1e-r`

**⚠️ Altere a senha imediatamente após o primeiro acesso!**

## 🔄 Atualizações

Para atualizar a aplicação:

```bash
cd /opt/gestao-integrada

# Baixar últimas mudanças
git pull

# Rebuild e restart
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d
```

## 🛑 Parar a Aplicação

```bash
docker-compose -f docker-compose.prod.yml down
```

**Para remover também os volumes (certificados e dados):**
```bash
docker-compose -f docker-compose.prod.yml down -v
```

## 🔐 Certificados SSL

### Renovação Automática
Os certificados são renovados automaticamente a cada 12 horas pelo certbot.

### Renovação Manual
Se necessário renovar manualmente:

```bash
docker exec gestao-integrada-nginx-proxy certbot renew
docker exec gestao-integrada-nginx-proxy nginx -s reload
```

### Verificar Certificados
```bash
docker exec gestao-integrada-nginx-proxy certbot certificates
```

## 🐛 Troubleshooting

### Erro: "Certificado SSL não pode ser obtido"

**Possíveis causas:**
1. DNS não está apontando corretamente
2. Portas 80/443 bloqueadas
3. Outro serviço usando as portas

**Solução:**
```bash
# Verificar DNS
nslookup seudominio.com.br

# Verificar portas
sudo netstat -tulpn | grep -E ':(80|443)'

# Verificar logs
docker logs gestao-integrada-nginx-proxy
```

### Erro: "Backend não responde"

**Verificar:**
```bash
# Ver logs do backend
docker logs gestao-integrada-backend

# Verificar conectividade com banco
docker exec gestao-integrada-backend nc -zv seu-servidor-db 5432
```

### Erro: "502 Bad Gateway"

**Causa:** Backend não iniciou corretamente.

**Solução:**
```bash
# Restart do backend
docker-compose -f docker-compose.prod.yml restart backend

# Verificar logs
docker logs -f gestao-integrada-backend
```

## 📊 Monitoramento

### Ver uso de recursos
```bash
docker stats
```

### Ver logs em tempo real
```bash
# Todos os containers
docker-compose -f docker-compose.prod.yml logs -f

# Container específico
docker logs -f gestao-integrada-backend
```

## 🔒 Segurança

### Recomendações:
1. ✅ Alterar senha do usuário `admin` após primeiro acesso
2. ✅ Usar senhas fortes para banco de dados
3. ✅ Manter servidor e Docker atualizados
4. ✅ Configurar backup automático do banco de dados
5. ✅ Monitorar logs regularmente
6. ✅ Usar firewall restritivo

### Backup do Banco de Dados
```bash
# Criar backup
docker exec -t gestao-integrada-backend pg_dump -U seu_usuario gestao_integrada_db > backup_$(date +%Y%m%d).sql

# Restaurar backup
cat backup_20250128.sql | docker exec -i gestao-integrada-backend psql -U seu_usuario gestao_integrada_db
```

## 📞 Suporte

Em caso de problemas, consulte:
- Logs dos containers: `docker logs <container_name>`
- Documentação do projeto: `README.md`
- Issues no GitHub: https://github.com/tiagojoao86/gestao-integrada/issues

---

**Desenvolvido com ❤️ pela equipe Gestão Integrada**
