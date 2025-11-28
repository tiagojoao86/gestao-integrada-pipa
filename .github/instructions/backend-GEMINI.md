# Gemini - Backend

> **Nota:** Eu sou um assistente de IA. Se eu identificar informações importantes que possam ser adicionadas a este arquivo para melhorar nossas interações futuras (como novos comandos, convenções ou detalhes de arquitetura), irei sugerir atualizações. Sinta-se à vontade para me perguntar como melhorá-lo.

## Resumo do Projeto

Este projeto contém a API RESTful para o sistema Gestão Integrada. É responsável pela lógica de negócios, acesso ao banco de dados e segurança.

## Stack Tecnológica

- Java
- Spring Boot
- Maven
- Flyway (para migrações de banco de dados)
- JPA/Hibernate

## Comandos Essenciais

**Nota:** Execute os comandos a partir do diretório `src/backend`.

- **Compilar o Projeto:**
  ```bash
  ./mvnw compile
  ```

- **Executar os Testes:**
  ```bash
  ./mvnw test
  ```

- **Executar a Aplicação (desenvolvimento):**
  ```bash
  ./mvnw spring-boot:run
  ```

- **Verificação de Estilo (Lint):**
  *(Confirme o comando no `pom.xml`, mas geralmente é algo como:)*
  ```bash
  ./mvnw checkstyle:check
  ```

- **Gerar o Build de Produção:**
  ```bash
  ./mvnw package
  ```

## Arquitetura e Convenções

- A arquitetura segue o padrão Model-View-Controller (MVC), comum em aplicações Spring.
- As entidades do banco de dados estão em `src/main/java/br/com/grupopipa/gestaointegrada/core/entity`.
- Os serviços (lógica de negócio) estão em `src/main/java/br/com/grupopipa/gestaointegrada/core/service`.
- As migrações de banco de dados com Flyway estão em `src/main/resources/db/migration`. Crie novos scripts de migração para qualquer alteração no schema.

## Princípios de Desenvolvimento

### Clean Code
- Escreva código legível e autoexplicativo.
- Use nomes descritivos para classes, métodos e variáveis.
- Mantenha métodos pequenos e com responsabilidade única.
- Evite comentários desnecessários; prefira código que se explica.
- Remova código morto e duplicações.

### Value Objects
- **Evite tipos primitivos** sempre que possível.
- Encapsule conceitos de domínio em **Value Objects**.
- Exemplos: ao invés de `String email`, use `Email email`; ao invés de `BigDecimal valor`, use `Money valor`.
- Value Objects devem ser imutáveis e conter validações de negócio.
- Benefícios: type safety, validações centralizadas, expressividade do domínio.

### Domain-Driven Design (DDD)
- **Priorize o uso de DDD** para modelar o domínio.
- As **Entities devem concentrar a maior parte das regras de domínio**.
- Use **Aggregates** para garantir consistência transacional.
- Separe claramente as camadas: Domain, Application (Services), Infrastructure.
- Use **Domain Events** quando apropriado para comunicação entre agregados.
- Modele o domínio usando **Ubiquitous Language** (linguagem ubíqua).
- Services devem orquestrar a lógica, mas as regras de negócio ficam no domínio.

## Estrutura de Diretórios

- `src/main/java`: Código-fonte da aplicação.
- `src/main/resources`: Arquivos de configuração, scripts SQL e chaves.
- `src/test/java`: Testes unitários e de integração.
