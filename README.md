# Projeto Gestão Integrada

Este é o projeto **Gestão Integrada**, um sistema completo que oferece funcionalidades de backend (API RESTful) e frontend (Single Page Application). O ambiente de desenvolvimento é orquestrado por um container Nginx que atua como proxy reverso, facilitando a comunicação entre o frontend e o backend.

## Stack Tecnológica

*   **Backend:** Java, Spring Boot, Maven, JPA/Hibernate, Flyway, **PostgreSQL**
*   **Frontend:** TypeScript, Angular, Angular CLI, CSS
*   **Infraestrutura Local:** Docker, Nginx

## Estrutura do Projeto

O projeto é dividido em duas partes principais:

*   `src/backend/`: Contém a API RESTful, lógica de negócios e persistência de dados.
*   `src/frontend/`: Contém a interface de usuário (SPA) que interage com a API do backend.
*   `.dev/nginx/`: Configurações do container Nginx para desenvolvimento local.

## Pré-requisitos

Para configurar e executar o projeto em ambiente de desenvolvimento, você precisará ter instalado:

*   **Docker** e **Docker Compose**: Para gerenciar o container Nginx (e opcionalmente o banco de dados PostgreSQL).
*   **Java Development Kit (JDK) 17+**: Para o backend Spring Boot.
*   **Node.js** e **npm** (ou Yarn): Para o desenvolvimento do frontend Angular.
*   **Banco de Dados PostgreSQL**: Uma instância do PostgreSQL deve estar acessível. Você pode rodá-lo localmente ou via Docker.

## Como Subir o Ambiente de Desenvolvimento

Siga os passos abaixo para colocar o sistema em funcionamento no seu ambiente local.

### 1. Configurar o Banco de Dados PostgreSQL

Certifique-se de que uma instância do PostgreSQL esteja rodando e acessível.

**Opções:**
*   **Localmente:** Instale e inicie o PostgreSQL diretamente na sua máquina.
*   **Via Docker:** Você pode criar um container PostgreSQL separado.

### 2. Configurar o Backend (application.properties)

Antes de iniciar o backend, você precisará configurar as credenciais e a URL de conexão com o banco de dados PostgreSQL.

Edite o arquivo `src/backend/src/main/resources/application.properties` e ajuste as propriedades de conexão com o banco de dados para o seu ambiente local.

**Exemplo de configuração no `application.properties`:**

```properties
# Configurações da Aplicação
spring.application.name=gestao-integrada
server.port=8080
server.servlet.context-path=/gestao-integrada/api

# Configurações do Banco de Dados PostgreSQL
# Ajuste 'localhost:5432', 'gestao_integrada_db', 'user' e 'password' conforme seu setup.
spring.datasource.url=jdbc:postgresql://localhost:5432/gestao_integrada_db
spring.datasource.username=user
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.type.descriptor.sql.BasicBinder=TRACE
spring.jpa.open-in-view=false
spring.jpa.hibernate.ddl-auto=validate

# Configurações do Flyway para Migrações de Banco de Dados
spring.flyway.enabled=true
spring.flyway.url=${spring.datasource.url}
spring.flyway.user=${spring.datasource.username}
spring.flyway.password=${spring.datasource.password}

# Configurações JWT (JSON Web Token) - mantenha como está para desenvolvimento
jwt.private.key=classpath:app.key
jwt.public.key=classpath:app.pub
```
*   **Atenção:** Certifique-se de que os valores `localhost`, `5432`, `gestao_integrada_db`, `user` e `password` na seção `spring.datasource` correspondam à sua configuração do PostgreSQL local.
*   A configuração do `server.servlet.context-path` (`/gestao-integrada/api`) é crucial e deve corresponder à configuração do Nginx.

### 3. Iniciar o Container Nginx

O Nginx atuará como um proxy reverso, direcionando as requisições do frontend para o backend (API) e servindo o próprio frontend.

Navegue até o diretório do Nginx e inicie o container:

```bash
cd .dev/nginx
docker-compose up -d
```

*   O Nginx estará disponível na porta `80` do seu `localhost`.
*   Ele irá direcionar requisições para `http://host.docker.internal:4200` (Frontend Angular) e `http://host.docker.internal:8080/gestao-integrada/api/` (Backend Spring Boot). O `host.docker.internal` permite que o container Docker acesse serviços rodando diretamente na sua máquina host.

### 4. Iniciar o Backend (Spring Boot)

O backend é a API RESTful do sistema.

Navegue até o diretório do backend e execute a aplicação Spring Boot:

```bash
cd src/backend
./mvnw spring-boot:run
```

*   O backend será iniciado na porta `8080`.

### 5. Iniciar o Frontend (Angular)

O frontend é a Single Page Application.

Navegue até o diretório do frontend e inicie o servidor de desenvolvimento Angular:

```bash
cd src/frontend
npm install # Se for a primeira vez, para instalar as dependências
npm run start
```

*   O frontend será iniciado na porta `4200`.

## Acesso à Aplicação

Após todos os serviços estarem rodando (Nginx, Backend e Frontend), você pode acessar a aplicação no seu navegador através da URL:

[http://localhost](http://localhost)

O Nginx fará a orquestração e você verá o frontend, que se comunicará com o backend através do proxy `/api/`.

## Acesso Inicial

Para o primeiro acesso ao sistema, utilize as seguintes credenciais:

- **Usuário:** `admin`
- **Senha:** `@RLthotr$&u=Huge1e-r`

**DISCLAIMER:** É **imprescindível** que a senha seja alterada no primeiro acesso, através do módulo de cadastro de usuários, para garantir a segurança do sistema.

## Copilot / Gemini Instructions

Este repositório inclui instruções para agentes (Copilot / Gemini). Consulte:

- `COPILOT_INSTRUCTIONS.md` na raiz para políticas e orientações globais.
- Instruções específicas por domínio em `.github/instructions/` (backend e frontend) que contêm os conteúdos dos arquivos GEMINI do projeto.

Há também um workflow de exemplo em `.github/workflows/copilot-context.yml` que demonstra como expor as instruções aos agentes via a variável de ambiente `COPILOT_CUSTOM_INSTRUCTIONS_DIRS`.
