# Projeto: Gestão Integrada — Visão Geral

Resumo

Este repositório contém um sistema-base chamado "Gestão Integrada", com backend (API RESTful) e frontend (SPA). O objetivo é prover uma base extensível para derivar sistemas mais complexos.

Escopo principal

- Login e Autenticação (JWT).
- Cadastro de Usuários.
- Cadastro de Perfis (papéis/roles) e associação de permissões por módulo.
- Dados iniciais e módulos essenciais são inseridos via migrations (Flyway).

Arquitetura — Backend

- Stack: Java, Spring Boot, Maven, JPA/Hibernate, Flyway.
- Convenções:
  - Pacote `core` contém objetos base (BaseEntity, BaseController, CrudService/CrudServiceImpl, Specifications).
  - Repositories são interfaces que estendem `JpaRepository` e `JpaSpecificationExecutor` e ficam nos pacotes de negócio.
  - `BaseEntity` possui campos comuns: id, createdAt, updatedAt, createdBy, updatedBy.
  - Configurações (Spring, Security, Datasource, Flyway) ficam no pacote `config`.

Responsabilidade das camadas

- Entity: concentram a maior parte das regras de domínio (seguindo DDD quando aplicável).
- Service: faz a intermediação com o repository e aplica regras de negócio relacionadas à persistência.
- Controller: expõe os endpoints e delega a lógica para os serviços.

Proteção e Autorização

- Endpoints protegidos por JWT; o token contém authorities usados para autorização.
- Autorização é feita com base nas authorities presentes no JWT.

Formato de resposta — Backend

Respostas bem-sucedidas seguem o padrão:

```json
{
  "statusCode": 200,
  "errorMessage": null,
  "body": {
    "content": [ /* lista de objetos */ ],
    "pageNumber": 0,
    "pageSize": 5,
    "totalElements": 3,
    "totalPages": 1
  }
}
```

Erros seguem formato similar ao abaixo (exemplo de validação/constraint):

```json
{
  "status": 400,
  "timestamp": "2025-11-24T21:20:20.093611088-03:00",
  "title": "Invalid Data",
  "detail": [
    "mensagem técnica com detalhe do erro"
  ],
  "userMessageKey": ["chave.de.mensagem.para.i18n"]
}
```

Arquitetura — Frontend

- Stack: TypeScript, Angular (SPA). Utilizamos PrimeNG e alguns componentes do Material Design (MDC).
- Convenções:
  - Componentes standalone sempre que possível.
  - Componente de CRUD composto por: Componente Principal, Componente de Grid e Componente de Detalhe.
  - Componentes base (BaseComponent, TableComponent, FilterComponent, PaginatorComponent) fornecem comportamentos reutilizáveis.
  - Rotas e organização por domínio: ex.: `src/frontend/src/app/components/cadastro/usuario`.

Estilos e variáveis globais

- `src/frontend/src/styles.css` contém variáveis CSS globais como `--secondary-color`, `--text-size`, `--text-color`, `--info-color`, `--success-color`, `--warning-color`, `--error-color`.

Regras de negócio — Usuários e Permissões

- Modelo de autorização granular: Usuário > Perfil > Módulo x Permissão.
- Perfis agrupam permissões para módulos (Listar, Visualizar, Editar, Deletar).
- Fluxos esperados:
  - Cadastrar Perfil (vincular módulos e permissões).
  - Cadastrar Usuários (atribuir perfis ao usuário).
  - Login e renovação de token: o token tem duração padrão de 10 minutos e é renovável por mais 10 minutos a cada requisição válida.

Observações operacionais

- Migrations: todas alterações de schema devem ser aplicadas via Flyway (scripts em `src/backend/src/main/resources/db/migration`).
- Padrões de commit/PR e testes: consulte `COPILOT_INSTRUCTIONS.md` para políticas globais e boas práticas.

