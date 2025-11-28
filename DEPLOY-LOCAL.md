# 🏠 Deploy Local com HTTPS (Certificado Autoassinado)

Este guia descreve como fazer o deploy da aplicação **Gestão Integrada** em um servidor local (rede privada) usando **imagens do Docker Hub** com HTTPS e certificado autoassinado para **testes e homologação**.

## 🎯 Cenário

- **Servidor:** Debian/Ubuntu na rede local (ex: 192.168.3.200)
- **HTTPS:** Certificado SSL autoassinado (não é Let's Encrypt)
- **Imagens:** Baixadas do Docker Hub (build feito pelo GitHub Actions)
- **Banco de Dados:** PostgreSQL (local ou externo)
- **Acesso:** Via IP do servidor (ex: https://192.168.3.200)

## ⚠️ IMPORTANTE

- Este setup é para **ambiente de desenvolvimento/testes**
- O navegador mostrará aviso de segurança (certificado autoassinado é esperado)
- Para produção com domínio real, altere `USE_SELF_SIGNED=false` no `.env`

## 🔄 Fluxo de Deploy

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Commit     │ ──> │   GitHub     │ ──> │  Docker Hub  │
│   no main    │     │   Actions    │     │   (imagens)  │
└──────────────┘     └──────────────┘     └──────────────┘
                                                   │
                                                   ↓ pull
                                           ┌──────────────┐
                                           │   Servidor   │
                                           │  192.168.x.x │
                                           └──────────────┘
```

---

## 🔧 Pré-requisitos

### 1. Configurar Docker Hub e GitHub Actions

Antes de fazer o deploy, você precisa configurar o CI/CD para gerar as imagens:

1. **Criar conta no Docker Hub:** https://hub.docker.com
2. **Criar Access Token:**
   - Docker Hub → Account Settings → Security → New Access Token
   - Copiar o token gerado
3. **Configurar Secrets no GitHub:**
   - Repositório → Settings → Secrets and variables → Actions
   - Adicionar: `DOCKERHUB_USERNAME` e `DOCKERHUB_TOKEN`
4. **Fazer push para gerar imagens:**
   ```bash
   git push origin main
   ```
   - Aguarde o GitHub Actions finalizar (veja em **Actions** no GitHub)

**Veja detalhes completos em:** `CI-CD.md`

---

## 📋 Pré-requisitos no Servidor

### 1. Sistema Operacional
- Debian/Ubuntu Linux
- Acesso SSH configurado

### 2. Software Necessário
```bash
# Atualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Docker
sudo apt install -y docker.io docker-compose git

# Adicionar usuário ao grupo docker (para não precisar de sudo)
sudo usermod -aG docker $USER
# Logout e login novamente para aplicar
```

### 3. Firewall (se habilitado)
```bash
# Liberar portas
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
```

---

## 🚀 Passo a Passo - Deploy

### 1️⃣ Baixar Arquivos de Deploy

```bash
# No servidor Debian
sudo mkdir -p /opt/gestao-integrada
sudo chown $USER:$USER /opt/gestao-integrada
cd /opt/gestao-integrada

# Baixar arquivos necessários
wget https://raw.githubusercontent.com/tiagojoao86/gestao-integrada/main/docker-compose.deploy.yml
wget https://raw.githubusercontent.com/tiagojoao86/gestao-integrada/main/deploy.sh
wget https://raw.githubusercontent.com/tiagojoao86/gestao-integrada/main/.env.example

# Dar permissão de execução
chmod +x deploy.sh
```

### 2️⃣ Configurar Variáveis de Ambiente

```bash
# Copiar arquivo de exemplo
cp .env.example .env

# Editar configurações
nano .env
```

**Configuração para servidor local com certificado autoassinado:**

```bash
# ===== DOCKER HUB =====
DOCKERHUB_USERNAME=seu_username_dockerhub  # ← Seu username do Docker Hub
IMAGE_TAG=latest                           # ← Versão da imagem (latest, v1.0.0, etc)

# ===== SSL / DOMÍNIO =====
DOMAIN=gestaointegrada.local               # ← Domínio local (pode ser qualquer)
EMAIL=admin@example.com                    # ← Email qualquer
USE_SELF_SIGNED=true                       # ← true = certificado autoassinado

# ===== BANCO DE DADOS =====
DATABASE_URL=jdbc:postgresql://192.168.3.200:5432/gestao_integrada_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=sua_senha_forte
```

**Importante:** Substitua `seu_username_dockerhub` pelo seu username real do Docker Hub!

### 3️⃣ Preparar Banco de Dados

```bash
# Instalar PostgreSQL (se não tiver)
sudo apt install -y postgresql

# Criar database e usuário
sudo -u postgres psql << 'EOF'
CREATE DATABASE gestao_integrada_db;
CREATE USER gestao_user WITH PASSWORD 'sua_senha_forte';
GRANT ALL PRIVILEGES ON DATABASE gestao_integrada_db TO gestao_user;
\q
EOF
```

### 4️⃣ Executar Deploy

```bash
# No servidor
cd /opt/gestao-integrada

# Executar script de deploy
./deploy.sh
docker-compose -f docker-compose.local.yml build

# Iniciar os containers
docker-compose -f docker-compose.local.yml up -d
```

**Tempo esperado:** 5-10 minutos na primeira vez (download de imagens e build).

### 6️⃣ Verificar Status

```bash
# Ver status dos containers
docker-compose -f docker-compose.local.yml ps

# Ver logs
docker-compose -f docker-compose.local.yml logs -f

# Parar de ver logs: Ctrl+C
```

**Containers esperados:**
- ✅ `gestao-integrada-nginx-proxy` (running)
- ✅ `gestao-integrada-frontend` (running)
- ✅ `gestao-integrada-backend` (running)
- ✅ `gestao-integrada-postgres` (running)

### 7️⃣ Aguardar Inicialização

O backend demora um pouco para iniciar (Flyway executa migrations). Acompanhe:

```bash
# Ver logs do backend
docker logs -f gestao-integrada-backend
```

**Aguarde até ver:**
```
Started GestaoIntegradaApplication in X.XXX seconds
```

### 8️⃣ Acessar a Aplicação

**Opção 1: Via hostname (se configurou /etc/hosts):**
```
https://gestao-integrada.local
```

**Opção 2: Via IP:**
```
https://192.168.3.200
```

**⚠️ O navegador irá mostrar aviso de segurança!**
- Chrome: Clique em "Avançado" → "Prosseguir para gestao-integrada.local (não seguro)"
- Firefox: Clique em "Avançado" → "Aceitar o risco e continuar"

### 9️⃣ Login Inicial

**Credenciais padrão:**
- Usuário: `admin`
- Senha: `@RLthotr$&u=Huge1e-r`

**⚠️ Altere a senha imediatamente!**

---

## 🔄 Comandos Úteis

### Parar aplicação
```bash
docker-compose -f docker-compose.local.yml down
```

### Reiniciar aplicação
```bash
docker-compose -f docker-compose.local.yml restart
```

### Ver logs de um container específico
```bash
docker logs -f gestao-integrada-backend
docker logs -f gestao-integrada-frontend
docker logs -f gestao-integrada-nginx-proxy
docker logs -f gestao-integrada-postgres
```

### Rebuild após alterações no código
```bash
# Pull das alterações
git pull

# Rebuild e restart
docker-compose -f docker-compose.local.yml build
docker-compose -f docker-compose.local.yml up -d
```

### Limpar tudo (cuidado: apaga dados!)
```bash
docker-compose -f docker-compose.local.yml down -v
rm -rf nginx-proxy/certs/*
```

---

## 🗄️ Banco de Dados

### Conectar ao PostgreSQL

**Via container:**
```bash
docker exec -it gestao-integrada-postgres psql -U gestao_user -d gestao_integrada_db
```

**Via cliente externo:**
- Host: 192.168.3.200
- Porta: 5432
- Database: gestao_integrada_db
- Usuário: gestao_user
- Senha: (definida no .env)

### Backup
```bash
docker exec -t gestao-integrada-postgres pg_dump -U gestao_user gestao_integrada_db > backup_$(date +%Y%m%d).sql
```

### Restaurar backup
```bash
cat backup_20250128.sql | docker exec -i gestao-integrada-postgres psql -U gestao_user gestao_integrada_db
```

---

## 🐛 Troubleshooting

### Erro: "Certificados SSL não encontrados"
**Solução:** Execute `./generate-ssl-cert.sh` antes do `docker-compose up`

### Erro: "porta 80 ou 443 já em uso"
**Verificar:** 
```bash
sudo netstat -tulpn | grep -E ':(80|443)'
```
**Solução:** Pare o serviço que está usando a porta (nginx, apache, etc.)

### Backend não inicia / erro de conexão com banco
**Verificar logs:**
```bash
docker logs gestao-integrada-backend
```
**Causas comuns:**
- Senha do banco incorreta no `.env`
- Banco ainda não iniciou completamente
- Erro nas migrations do Flyway

**Solução:** Verificar variáveis no `.env` e aguardar mais tempo

### Frontend mostra erro 502 Bad Gateway
**Causa:** Backend ainda não iniciou completamente.
**Solução:** Aguardar backend finalizar (ver logs)

### Aviso de segurança no navegador persiste
**Normal!** Certificado autoassinado sempre mostra aviso. É esperado.
Para remover o aviso, use domínio real + Let's Encrypt (docker-compose.prod.yml).

---

## 🔐 Segurança

### ⚠️ Para ambiente de testes local:
- ✅ Certificado autoassinado é aceitável
- ✅ Senhas simples no `.env` são OK (mas altere a senha do admin!)
- ✅ Portas expostas na rede local são OK

### ⚠️ Para produção (internet pública):
- ❌ NÃO use certificado autoassinado
- ❌ NÃO use senhas fracas
- ❌ NÃO exponha PostgreSQL (porta 5432) publicamente
- ✅ Use `docker-compose.prod.yml` com Let's Encrypt
- ✅ Use senhas fortes
- ✅ Configure firewall restritivo

---

## 📊 Monitoramento

### Ver uso de recursos
```bash
docker stats
```

### Ver todos os logs
```bash
docker-compose -f docker-compose.local.yml logs -f
```

### Ver logs dos últimos 100 linhas
```bash
docker-compose -f docker-compose.local.yml logs --tail=100
```

---

## 🚀 Migração para Produção

Quando estiver pronto para produção com domínio real:

1. **Registrar domínio** (ex: suaempresa.com.br)
2. **Configurar DNS** para apontar para IP público do servidor
3. **Usar `docker-compose.prod.yml`** (tem Let's Encrypt automático)
4. **Configurar `.env` de produção** com dados reais
5. **Deploy:** `docker-compose -f docker-compose.prod.yml up -d`

Veja `DEPLOY.md` para instruções completas de produção.

---

## 📞 Comandos Executados (Resumo)

```bash
# 1. Preparação
cd /opt
sudo git clone https://github.com/tiagojoao86/gestao-integrada.git
cd gestao-integrada
sudo chown -R $USER:$USER /opt/gestao-integrada

# 2. Certificado SSL
chmod +x generate-ssl-cert.sh
./generate-ssl-cert.sh

# 3. Configuração
cat > .env << 'EOF'
DOMAIN=gestao-integrada.local
DATABASE_URL=jdbc:postgresql://postgres:5432/gestao_integrada_db
DATABASE_USERNAME=gestao_user
DATABASE_PASSWORD=gestao_pass_123
DATABASE_NAME=gestao_integrada_db
EOF

# 4. Deploy
docker-compose -f docker-compose.local.yml build
docker-compose -f docker-compose.local.yml up -d

# 5. Verificar
docker-compose -f docker-compose.local.yml ps
docker logs -f gestao-integrada-backend

# 6. Acessar
# https://192.168.3.200
# Usuário: admin
# Senha: @RLthotr$&u=Huge1e-r
```

---

**Desenvolvido com ❤️ pela equipe Gestão Integrada**
