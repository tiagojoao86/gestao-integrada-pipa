# GitHub Copilot Instructions - Gestão Integrada Pipa

Sistema multi-tenant de gestão integrada com backend Spring Boot (Java 22) e frontend Angular 19.

## Architecture Overview

**Multi-tenant Strategy:** Schema-per-tenant isolation using PostgreSQL. Every HTTP request MUST include `X-Tenant-ID` header (validated by `TenantFilter` before Spring Security). JWT tokens contain `tenant_id` claim preventing cross-tenant access.

**Tech Stack:**

- Backend: Spring Boot 3.5.8, Java 22, JPA/Hibernate, Flyway, PostgreSQL, JWT (RS256)
- Frontend: Angular 19, PrimeNG 19, TypeScript 5.5, i18n (pt-BR/en-US)
- Infrastructure: Docker, Nginx reverse proxy

**Project Structure:**

```
src/backend/src/main/java/br/com/grupopipa/gestaointegrada/
  ├── cadastro/      # Registration domain (pessoa, usuario, perfil, unidadenegocio)
  ├── financeiro/    # Financial domain (titulo, planocontas, contabancaria)
  ├── tenant/        # Multi-tenancy infrastructure
  └── core/          # Shared: entities, DTOs, services, valueobjects, exceptions

src/frontend/src/app/
  ├── components/base/        # Reusable components (guards, interceptors, base services)
  ├── components/cadastro/    # Registration features
  ├── components/financeiro/  # Financial features
  └── model/                  # Shared models and DTOs
```

## Backend Development

### UUID-based IDs (CRITICAL)

**ALWAYS use UUID (UUIDv7) for entity IDs. NEVER use Long/BigSerial.**

```java
// ✅ CORRECT
@Entity
public class MyEntity extends BaseEntity {  // BaseEntity provides UUID id
    // fields...
}

// Migration
CREATE TABLE my_entity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid()
);
```

### DDD & Value Objects Pattern

**Encapsulate domain concepts in immutable Value Objects. Check `core.valueobject` before creating new ones.**

Available Value Objects: `Nome`, `CPF`, `CNPJ`, `Email`, `PhoneNumber`, `Money`

```java
// Entity using Value Objects
@Entity
public class Pessoa extends BaseEntity {
    @Embedded
    private Nome nome;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cpf"))
    private CPF cpf;
}
```

### Builder Pattern (Mandatory for Entities)

**All domain entities MUST use Builder pattern with centralized validation.**

```java
@Entity
public class MyEntity extends BaseEntity {
    private String campo1;

    // Private constructor (Builder only)
    private MyEntity(String campo1) { this.campo1 = campo1; }

    protected MyEntity() {} // JPA

    private static class ValidatedData {
        final String campo1;
        ValidatedData(String campo1) { this.campo1 = campo1; }
    }

    private static ValidatedData validate(String campo1) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        // Use ValidationUtils for Value Objects
        Campo1VO vo = ValidationUtils.validateAndGet(
            () -> new Campo1VO(campo1), violations
        );
        if (!violations.isEmpty()) {
            throw new BeanValidationException("myEntity", violations);
        }
        return new ValidatedData(campo1);
    }

    public void atualizar(String campo1) {
        ValidatedData data = validate(campo1);
        this.campo1 = data.campo1;
    }

    public static class Builder {
        private String campo1;
        public Builder campo1(String campo1) { this.campo1 = campo1; return this; }
        public MyEntity build() {
            ValidatedData data = validate(this.campo1);
            return new MyEntity(data.campo1);
        }
    }
}
```

### Validation & Error Handling

**Use BeanValidationException for ALL business validations. Exceptions become i18n keys for frontend.**

```java
// Validation with structured keys
private void validar() {
    Set<BeanValidationMessage> violations = new HashSet<>();
    if (tipo == null) {
        violations.add(new BeanValidationMessage("tipo", "Tipo é obrigatório"));
    }
    if (!violations.isEmpty()) {
        throw new BeanValidationException("titulo", violations);
    }
}
// Backend response: { "userMessageKey": ["titulo.tipo"], "status": 400 }
// Frontend uses key to fetch translated message
```

### Database Constraints (CRITICAL)

**ALL constraints MUST be explicitly named for i18n error mapping.**

Naming convention:

- UNIQUE: `uk_<table>_<field>` (e.g., `uk_usuario_login`)
- FOREIGN KEY: `fk_<table>_<referenced_table>` (e.g., `fk_titulo_pessoa`)
- CHECK: `ck_<table>_<field>` (e.g., `ck_titulo_valor_positivo`)

```sql
-- Migration
ALTER TABLE usuario ADD CONSTRAINT uk_usuario_login UNIQUE (login);
```

Register in `DatabaseConstraintsEnum`:

```java
public enum DatabaseConstraintsEnum {
    UK_USUARIO_LOGIN("usuario.login.unique"),
    // ...
}
```

`RestExceptionHandler` automatically maps constraint violations to i18n keys.

### Service Layer Pattern

**Extend CrudServiceImpl for standard CRUD operations.**

```java
@Service
public class MyServiceImpl extends CrudServiceImpl<MyDTO, MyGridDTO, MyEntity, MyRepository>
        implements MyService {

    @Override
    protected MyEntity mergeEntityAndDTO(MyEntity entity, MyDTO dto) {
        if (Objects.isNull(entity)) {
            return new MyEntity.Builder()
                    .campo1(dto.getCampo1())
                    .build();
        }
        entity.atualizar(dto.getCampo1());
        return entity;
    }

    @Override
    protected MyDTO buildDTOFromEntity(MyEntity entity) {
        return MyDTO.builder()
                .id(entity.getId())
                .campo1(entity.getCampo1())
                .build();
    }

    // Implement remaining abstract methods...
}
```

### Multi-Tenant Critical Rules

- **Migrations:** Place tenant-specific migrations in `src/main/resources/db/tenant-migrations/` (NOT `db/migration/`)
- **TenantContext:** Use `TenantContext.getTenantId()` to get current schema name
- **NEVER:** Create EntityManagerFactory without multi-tenant config, use `@Transactional` on CREATE SCHEMA methods, or hardcode schema names

### Testing with Testcontainers

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public abstract class AbstractIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
}
```

## Frontend Development

### Component Naming & Selectors

**Prefix:** All components/directives use `gi-` prefix

```typescript
@Component({
  selector: 'gi-pessoa-lista',  // kebab-case for components
  // ...
})

@Directive({
  selector: '[giHighlight]'  // camelCase for directives
})
```

### Component Organization

- **Reusable components:** `components/base/` (crud-base, dialog-base, etc.)
- **Feature components:** `components/{domain}/` (cadastro, financeiro)
- **Services & DTOs:** Co-located with components (e.g., `usuario/usuario.service.ts`, `usuario/model/usuario.dto.ts`)
- **Backend DTOs:** Use `backend-*.dto.ts` when API payload differs from component DTO

### Guards & Interceptors

- **AuthGuard:** `components/base/auth/` - checks JWT validity, redirects to login
- **TenantInterceptor:** Automatically adds `X-Tenant-ID` header to ALL HTTP requests
- **ErrorInterceptor:** Maps backend `userMessageKey` to translated messages

### i18n Pattern

**Messages must be defined in `src/locale/messages.xlf` (pt-BR) and `messages.en.xlf`.**

```typescript
// Component
constructor(private translate: TranslateService) {}

showError(key: string) {
  const msg = this.translate.instant(key);
  this.messageService.add({ severity: 'error', detail: msg });
}

// Backend returns: { "userMessageKey": ["usuario.login.unique"] }
// Frontend displays: "Login já cadastrado" (pt-BR) or "Login already exists" (en-US)
```

### Angular Best Practices

- **Componentization:** Extract repeating patterns into reusable components
- **Low coupling:** Use `@Input()`/`@Output()` for component communication
- **No inline interfaces:** Extract DTOs/models to `model/` folder
- **Lint:** Run `npm run lint -- --fix` to auto-remove unused imports

## Development Workflows

### Backend Commands (from `src/backend/`)

```bash
./mvnw spring-boot:run          # Run dev server (port 8080)
./mvnw test                     # Run tests
./mvnw test -Dtest=MyTest       # Run specific test
./mvnw package                  # Build production JAR
```

### Frontend Commands (from `src/frontend/`)

```bash
npm install                     # Install dependencies
ng serve                        # Dev server (port 4200)
ng serve --configuration=en-US  # Dev server with English locale
ng test                         # Run Karma tests
npm run lint -- --fix           # Lint and auto-fix
ng build                        # Production build
```

### Docker Compose Environments

```bash
# Local development (requires external DB)
docker-compose -f docker-compose.local.yml up

# Production with HTTPS
docker-compose -f docker-compose.prod.yml up -d
```

### Database Setup

```bash
# Run migrations for new tenant
./setup-database.sh

# Test tenant connection
./test-tenant.sh <tenant-name>
```

## Key Integration Points

**Authentication Flow:**

1. Frontend sends credentials to `/auth/login`
2. Backend validates and returns JWT (RS256) with `tenant_id` claim
3. Frontend stores JWT in localStorage
4. All subsequent requests include `Authorization: Bearer <token>` and `X-Tenant-ID` header

**Request Flow:**

1. Nginx (port 443/80) → routes `/api/*` to backend:8080, `/` to frontend:80
2. TenantFilter validates `X-Tenant-ID` header and JWT `tenant_id` match
3. Spring Security authenticates user
4. TenantConnectionProvider applies `SET search_path = tenant_<id>`
5. Service layer processes business logic
6. Response includes structured `userMessageKey` for i18n errors

## Additional Documentation

**MUST READ before working on:**

- Multi-tenancy: `MULTI-TENANT-ARCHITECTURE.md`
- Backend patterns: `.github/instructions/backend-GEMINI.md`
- Frontend patterns: `.github/instructions/frontend-GEMINI.md`
- Deployment: `DEPLOY.md`, `DEPLOY-LOCAL.md`
- Complete conventions: `COPILOT_INSTRUCTIONS.md` (root)

**Default Credentials (CHANGE IMMEDIATELY):**

- User: `admin`
- Password: `(definida na instalação — altere no primeiro acesso)`
