# 🚀 CI/CD e Deploy com Docker Hub

Este documento descreve o processo completo de **CI/CD** (Integração Contínua e Deploy Contínuo) usando **GitHub Actions** e **Docker Hub**.

## 📋 Visão Geral

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐      ┌──────────────┐
│   Commit    │ ───> │   GitHub     │ ───> │ Docker Hub  │ ───> │   Servidor   │
│   no main   │      │   Actions    │      │  (imagens)  │      │  (produção)  │
└─────────────┘      └──────────────┘      └─────────────┘      └──────────────┘
                         Build                  Push               Pull + Deploy
```

## 🏗️ Arquitetura do CI/CD

### 1. GitHub Actions (Build e Push)
Quando você faz **push** para a branch `main` ou cria uma **tag**:
1. GitHub Actions dispara automaticamente
2. Faz build de 3 imagens Docker:
   - Frontend (Angular + Nginx)
   - Backend (Spring Boot)
   - Nginx Proxy (SSL/HTTPS)
3. Envia (push) as imagens para o Docker Hub
4. As imagens ficam disponíveis publicamente (ou privadamente)

### 2. Servidor (Pull e Deploy)
No servidor de produção/homologação:
1. Executa o script `deploy.sh`
2. Baixa (pull) as imagens mais recentes do Docker Hub
3. Para os containers antigos
4. Inicia os novos containers
5. Aplicação fica disponível

## ⚙️ Configuração Inicial

### 1️⃣ Criar Conta no Docker Hub

1. Acesse https://hub.docker.com
2. Crie uma conta gratuita
3. Anote seu **username** (ex: `tiagojoao86`)

### 2️⃣ Criar Token de Acesso

1. No Docker Hub, vá em **Account Settings** → **Security**
2. Clique em **New Access Token**
3. Nome: `github-actions-gestao-integrada`
4. Permissions: **Read, Write, Delete**
5. Copie o token gerado (só aparece uma vez!)

### 3️⃣ Configurar Secrets no GitHub

1. No repositório GitHub, vá em **Settings** → **Secrets and variables** → **Actions**
2. Clique em **New repository secret**
3. Adicione os seguintes secrets:

| Nome | Valor | Descrição |
|------|-------|-----------|
| `DOCKERHUB_USERNAME` | `seu_username` | Seu username do Docker Hub |
| `DOCKERHUB_TOKEN` | `token_copiado` | Token de acesso gerado |

### 4️⃣ Fazer Push para Testar

```bash
git add .
git commit -m "feat: adiciona CI/CD com Docker Hub"
git push origin main
```

Acesse **Actions** no GitHub para acompanhar o build.

## 📦 Workflow GitHub Actions

### Quando é Executado

- ✅ Push para branch `main`
- ✅ Criação de tags (ex: `v1.0.0`)
- ✅ Pull Requests (apenas build, sem push)

### O Que Faz

```yaml
1. Checkout do código
2. Setup do Docker Buildx
3. Login no Docker Hub
4. Build da imagem Frontend
5. Push Frontend para Docker Hub
6. Build da imagem Backend
7. Push Backend para Docker Hub
8. Build da imagem Nginx Proxy
9. Push Nginx Proxy para Docker Hub
10. Exibe resumo no GitHub
```

### Tags das Imagens

| Evento | Tag da Imagem |
|--------|---------------|
| Push no `main` | `latest` |
| Tag `v1.0.0` | `v1.0.0` + `latest` |
| Pull Request #42 | `pr-42` |

## 🖥️ Deploy no Servidor

### Pré-requisitos no Servidor

```bash
# Instalar Docker (se não tiver)
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### Configurar Variáveis de Ambiente

```bash
# No servidor
cd /opt
git clone https://github.com/tiagojoao86/gestao-integrada-pipa.git
cd gestao-integrada

# Copiar e editar .env
cp .env.example .env
nano .env
```

**Preencha o `.env`:**

```bash
# Docker Hub
DOCKERHUB_USERNAME=seu_username

# Versão da imagem (latest, v1.0.0, etc)
IMAGE_TAG=latest

# Domínio (ou deixe como está para usar certificado autoassinado)
DOMAIN=gestaointegrada.local
EMAIL=admin@example.com
USE_SELF_SIGNED=true

# Banco de Dados
DATABASE_URL=jdbc:postgresql://localhost:5432/gestao_integrada_db
DATABASE_USERNAME=seu_usuario
DATABASE_PASSWORD=sua_senha
```

### Executar Deploy

```bash
# Dar permissão de execução
chmod +x deploy.sh

# Executar deploy
./deploy.sh
```

**O script faz:**
1. ✅ Verifica pré-requisitos (Docker, .env)
2. ✅ Baixa imagens do Docker Hub
3. ✅ Para containers antigos
4. ✅ Inicia novos containers
5. ✅ Exibe status e instruções

## 🔄 Fluxo Completo de Deploy

### Desenvolvimento → Produção

```bash
# 1. Desenvolver localmente
git checkout -b feature/nova-funcionalidade
# ... fazer alterações ...
git commit -m "feat: adiciona nova funcionalidade"
git push origin feature/nova-funcionalidade

# 2. Criar Pull Request no GitHub
# ... revisar código ...

# 3. Merge para main
git checkout main
git merge feature/nova-funcionalidade
git push origin main

# 4. GitHub Actions roda automaticamente
# ✓ Build das imagens
# ✓ Push para Docker Hub

# 5. No servidor de produção
ssh user@192.168.3.200
cd /opt/gestao-integrada
./deploy.sh

# 6. Aplicação atualizada! 🎉
```

## 🏷️ Versionamento com Tags

### Criar Release

```bash
# Criar tag
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# GitHub Actions builda e pusha com tag v1.0.0
```

### Deploy de Versão Específica

```bash
# No servidor, editar .env
IMAGE_TAG=v1.0.0

# Deploy
./deploy.sh
```

### Rollback para Versão Anterior

```bash
# Mudar para versão antiga
IMAGE_TAG=v0.9.0
./deploy.sh
```

## 📊 Monitoramento

### Ver Logs do Deploy

```bash
# Logs de todos os containers
docker-compose -f docker-compose.deploy.yml logs -f

# Log específico
docker logs -f gestao-integrada-pipa-backend
docker logs -f gestao-integrada-pipa-frontend
docker logs -f gestao-integrada-pipa-nginx-proxy
```

### Verificar Status

```bash
# Status dos containers
docker-compose -f docker-compose.deploy.yml ps

# Uso de recursos
docker stats
```

### Reiniciar um Container

```bash
# Reiniciar apenas o backend
docker-compose -f docker-compose.deploy.yml restart backend

# Reiniciar tudo
docker-compose -f docker-compose.deploy.yml restart
```

## 🔒 Boas Práticas

### Segurança

- ✅ **Nunca** commite o arquivo `.env`
- ✅ Use secrets do GitHub para credenciais
- ✅ Mantenha tokens com permissões mínimas necessárias
- ✅ Revogue tokens antigos quando não forem mais necessários

### Versionamento

- ✅ Use **Semantic Versioning** (v1.0.0, v1.1.0, v2.0.0)
- ✅ Tag `latest` sempre aponta para o último build do `main`
- ✅ Tags com versão são imutáveis (nunca sobrescreva)

### Deploy

- ✅ Teste em ambiente de homologação antes de produção
- ✅ Faça backup do banco antes de deploy com breaking changes
- ✅ Monitore logs após deploy
- ✅ Tenha plano de rollback pronto

## 🐛 Troubleshooting

### Erro: "denied: requested access to the resource is denied"

**Causa:** Token do Docker Hub inválido ou sem permissões.

**Solução:**
1. Gere novo token no Docker Hub
2. Atualize secret `DOCKERHUB_TOKEN` no GitHub

### Erro: "image not found"

**Causa:** Imagem não existe no Docker Hub.

**Solução:**
1. Verifique se o workflow rodou com sucesso
2. Verifique se `DOCKERHUB_USERNAME` está correto no `.env`

### Build falha no GitHub Actions

**Solução:**
1. Veja os logs no Actions
2. Teste build localmente: `docker build -t teste ./src/backend`

### Container não inicia no servidor

**Solução:**
```bash
# Ver logs de erro
docker logs gestao-integrada-pipa-backend

# Verificar configuração
docker inspect gestao-integrada-pipa-backend
```

## 📚 Comandos Úteis

```bash
# Listar imagens disponíveis no Docker Hub
docker search seu_username/gestao-integrada

# Ver tags de uma imagem
curl "https://registry.hub.docker.com/v2/repositories/seu_username/gestao-integrada-pipa-backend/tags"

# Limpar imagens antigas no servidor
docker image prune -a

# Atualizar para última versão
IMAGE_TAG=latest ./deploy.sh

# Deploy de versão específica
IMAGE_TAG=v1.2.0 ./deploy.sh
```

## 🎯 Próximos Passos

1. ✅ Configurar ambiente de **staging** (homologação)
2. ✅ Adicionar **testes automatizados** no workflow
3. ✅ Configurar **health checks** nos containers
4. ✅ Implementar **blue-green deployment**
5. ✅ Adicionar **notificações** (Slack, email) de deploy

---

**Documentação atualizada em:** 28/11/2025
