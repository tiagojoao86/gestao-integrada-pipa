# Modelo de Prompt — Criar Nova Entidade (Gestão Integrada Pipa)

Objetivo

- Um prompt conciso e reutilizável para gerar uma nova entidade de domínio completa seguindo as convenções do projeto (backend + migrações tenant + frontend + i18n + testes).

Como usar

- Preencha os campos entre `{{}}` antes de enviar o prompt para um gerador de código. O gerador deverá produzir: entidade Java, repositório, serviço, controller, DTOs, migrations Flyway (tenant, idempotentes), componentes frontend (grid/detalhe), rota, serviço de mensagens backend, entradas i18n (pt-BR e en-US) e esboços de testes.

Restrições do projeto (obrigatórias)

- IDs: Use UUIDv7 para todos os IDs de entidade. No SQL, coluna `UUID` com `DEFAULT gen_random_uuid()`.
- Builder: Entidades JPA devem usar o padrão Builder, com método de validação central que retorne um objeto `ValidatedData`. Incluir construtor protegido sem argumentos para JPA.
- Value Objects: Preferir VOs já existentes em `core.valueobject` (por exemplo: `Nome`, `CPF`, `CNPJ`, `Email`, `PhoneNumber`, `Money`). Novos VOs devem ser imutáveis e conter validação.
- Validação: Validações de negócio devem lançar `BeanValidationException` com mensagens do tipo `BeanValidationMessage`; essas chaves são usadas para i18n no frontend.
- Nomes de constraints: Nomear explicitamente todas as constraints seguindo a convenção do projeto:
  - UNIQUE: `uk_<tabela>_<campo>`
  - FOREIGN KEY: `fk_<tabela>_<tabela_referenciada>`
  - CHECK: `ck_<tabela>_<campo>`
- Migrations tenant: Colocar DDL em `src/main/resources/db/tenant-migrations/`. As migrations devem ser idempotentes (usar `IF NOT EXISTS` ou blocos protegidos) porque serão executadas em múltiplos schemas durante os testes.
- Camada de serviço: Quando aplicável, os serviços devem estender `CrudServiceImpl<...>` e implementar `mergeEntityAndDTO` e `buildDTOFromEntity`.
- Testes: Evitar dados determinísticos que causem colisões entre testes que compartilham o schema tenant. Para campos `codigo` curtos, usar valores únicos em tempo de execução, por exemplo: `(UUID.randomUUID().toString() + System.nanoTime()).replace("-", "").substring(0, 18)`.
- Frontend: Componentes usam prefixo `gi-`. Rotas devem ser adicionadas sob `src/frontend/src/app/components/<domain>/<entity>`. Separar DTOs em arquivos de detalhe e grid. Serviços de mensagens backend devem estender `AbstractBackendMessageService` e exportar o nome esperado pelo projeto.
- i18n: Adicionar traduções em `src/frontend/src/app/locale/messages.xlf` (pt-BR) e `messages.en.xlf` (en-US). Quando possível, reutilizar ids numéricos já existentes; para mensagens de validação, usar chaves semânticas como `{{entity}}.{{field}}.notBlank`.

Template do prompt (preencher `{{}}`)

"""
Contexto: Gere código backend (Java/Spring Boot) e frontend (Angular) para uma nova entidade de domínio no projeto "Gestão Integrada Pipa". Siga estritamente as convenções do projeto (IDs UUID, Builder + validação, migrações tenant idempotentes, nomes de constraints, padrões de serviço, chaves i18n). Produza os arquivos e scripts descritos abaixo.

Resumo da entidade:

- Nome da entidade: {{EntityName}} (CamelCase, singular), ex.: `CategoriaTitulo`
- Nome da tabela: {{table_name}} (snake_case, plural), ex.: `categoria_titulo`
- Descrição curta: {{ShortDescription}} (uma frase)
- Campos: liste campos no formato: `nome` (snake_case), `javaType` (String/Integer/LocalDate/UUID/VO), `required` (true/false), `maxLength` (quando aplicável), validações especiais e se é `@Embedded` (VO).
  Exemplo:
  - nome (String), required, max 200
  - codigo (String), required, unique, max 20
  - unidade_negocio_id (UUID), required, FK -> unidade_negocio(id)

Saídas requeridas (cada arquivo como um bloco de conteúdo separado):

1. Entidade Java: `{{EntityName}}` no pacote `br.com.grupopipa.gestaointegrada.{{domain}}.{{entityPackage}}`.

   - `@Entity`, estende `BaseEntity` (id UUID do base), padrão Builder, construtor privado e método `validate(...)` que retorna `ValidatedData`.
   - Use `@Embedded` para Value Objects.
   - Mapeamentos JPA para relacionamentos devem ser adicionados conforme necessário; se a relação estiver sendo desmembrada (decoupled), não adicione `@ManyToOne` — indique que a FK será adicionada em migration separada.

2. Repositório: `{{EntityName}}Repository` extends `JpaRepository<{{EntityName}}, UUID>` em `...repository`.

3. DTOs: `{{EntityName}}DTO` (detalhe) e `{{EntityName}}GridDTO` (grid). Usar builders compatíveis com o projeto.

4. Serviço: `{{EntityName}}Service` (interface) e `{{EntityName}}ServiceImpl` em `...service`.

   - `ServiceImpl` deve estender `CrudServiceImpl<{{EntityName}}DTO, {{EntityName}}GridDTO, {{EntityName}}, {{EntityName}}Repository>` e implementar `mergeEntityAndDTO` e `buildDTOFromEntity`.

5. Controller: `{{EntityName}}Controller` com `@RestController` e rota `/api/{{entityPath}}` — CRUD padrão conforme convenções do projeto.

6. Migrations Flyway (tenant, idempotentes):

   - `V{{yyyymmddHHMMSS}}__create_{{table_name}}_table.sql` (criar tabela com PK UUID default gen_random_uuid(); nomear constraints explicitamente). Colocar em `src/main/resources/db/tenant-migrations/`.
   - Opcional: `V{{yyyymmddHHMMSS}}__add_fk_{{campo}}_to_{{outra_tabela}}.sql` para FKs posteriores — sempre idempotente.

7. Serviço de mensagens backend (frontend): `{{entity}}-backend-message.service.ts` estendendo `AbstractBackendMessageService` e mapeando chaves como `{{entity}}.nome.notBlank`.

8. Scaffolding frontend

   - Rotas: adicionar entrada em `financeiro.routes.ts` (ou rota de domínio apropriada) com `moduleKey: 'FINANCEIRO_{{ENTITY_UPPER}}'` ou chave correta do módulo.
   - Componentes: criar pasta `{{entity}}/` com componentes grid e detalhe, serviços (`{{entity}}.service.ts`) seguindo padrões existentes.
   - DTOs: `{{entity}}.dto.ts` e `{{entity}}-grid.dto.ts`.
   - i18n: adicionar `trans-unit` em pt-BR e en-US para todas as strings visíveis ao usuário e mensagens de validação.

9. Testes

   - Esboços de testes unitários para repositório e serviço. Exemplos de dados de teste devem usar valores únicos em tempo de execução para evitar colisões.

10. Checklist (o que mudou e onde)

Formato de saída do gerador (obrigatório):

- Retornar um objeto JSON onde cada chave é o caminho do arquivo e o valor é o conteúdo do arquivo. Exemplo:
  {
  "src/backend/src/main/java/.../{{EntityName}}.java": "<conteúdo>",
  "src/backend/src/main/resources/db/tenant-migrations/V2025...\__create_{{table_name}}\_table.sql": "<sql>",
  "src/frontend/src/app/components/.../{{entity}}.service.ts": "<conteúdo>",
  "i18n/pt": [ { "key": "...", "source": "...", "target": "..." } ],
  "i18n/en": [ ... ]
  }

Regras de validação que o gerador deve obedecer:

- IDs: UUID.
- Constraints: nomes explícitos seguindo convenção.
- Migrations: em `db/tenant-migrations` e idempotentes.
- Validação: usar Builder + `BeanValidationException` para regras de negócio.

Exemplo de entrada (preencher ao executar o gerador):
{
"EntityName": "CategoriaTitulo",
"table_name": "categoria_titulo",
"domain": "financeiro",
"entityPackage": "categoria",
"ShortDescription": "Categoria para títulos financeiros",
"fields": [
{"name":"nome","javaType":"String","required":true,"maxLength":200},
{"name":"codigo","javaType":"String","required":true,"unique":true,"maxLength":20},
{"name":"unidadeNegocioId","javaType":"UUID","required":true,"fk":"unidade_negocio(id)"}
]
}

Boas práticas e observações

- Mantenha mudanças pequenas e focadas para respeitar o estilo do projeto.
- Execute os testes de backend após adicionar migrations para confirmar que os scripts tenant são aplicados corretamente.
- Ao adicionar FK a `titulo`, prefira uma migration separada depois da criação da nova tabela; atualize a entidade `titulo` e ajuste testes conforme necessário.

---

Próximos passos que posso executar para você:

- Gerar um exemplo preenchido para `CategoriaTitulo` com base neste template.
- Salvar uma cópia alternativa em outro local do repositório.
- Executar um build do frontend para validar chaves i18n após alterações.
