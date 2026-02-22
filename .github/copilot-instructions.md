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

**Use BeanValidationException with fluent Validator API. Messages resolved via Spring MessageSource (messages.properties / messages_en.properties).**

```java
// Use Validator.of() fluent API for all field validation
private static ValidatedData validate(String descricao, Pessoa pessoa, BigDecimal valor, ...) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    // Primitives/objects: fluent Validator
    Validator.of(descricao, "descrição", violations).notBlank().maxLength(500);
    Validator.of(pessoa, "pessoa", violations).notNull();

    // Value Objects: use ValidationUtils.validateAndGet() to accumulate errors
    // (avoids short-circuiting — shows ALL errors at once)
    Nome nomeVO = ValidationUtils.validateAndGet(() -> Nome.of(descricao), violations);

    // Business rule violations: direct BeanValidationMessage with MessageSource key
    if (dataVencimento != null && dataVencimento.isBefore(dataEmissao)) {
        violations.add(new BeanValidationMessage(
            "validation.titulo.dataVencimentoInvalida",
            "Data de vencimento não pode ser anterior à data de emissão."));
    }

    if (!violations.isEmpty()) {
        throw new BeanValidationException("titulo", violations);
    }
    return new ValidatedData(...);
}
// Validator.of() generates keys: "validation.field.required", "validation.field.notBlank", etc.
// Resolved by RestExceptionHandler via MessageSource (Portuguese fallback if key not found)
// Backend response: { "userMessageKey": ["validation.field.required"], "status": 400 }
```

**DO NOT use manual `new BeanValidationMessage("fieldName", "hardcoded message")` for standard validations.** Reserve direct `BeanValidationMessage` only for business rule violations with specific keys registered in `messages.properties`.

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

### Audit Trail System

**All entities automatically track creation, modification, and soft deletion metadata through BaseEntity.**

#### Backend Audit Fields (Automatic via CustomAuditingEntityListener)

BaseEntity provides:
- `createdBy`, `createdAt` - Set on entity creation
- `updatedBy`, `updatedAt` - Updated on modifications (NOT on soft delete)
- `deletedBy`, `deletedAt`, `deleted` - Set on soft delete

**IMPORTANT:** Soft delete does NOT update `updatedBy`/`updatedAt` - these preserve the last real modification.

```java
// CustomAuditingEntityListener handles audit fields
@PrePersist
public void touchForCreate(Object target) {
    // Sets createdBy, createdAt, updatedBy, updatedAt
}

@PreUpdate
public void touchForUpdate(Object target) {
    if (entity.getDeleted() == true) {
        // SKIP updating updatedBy/updatedAt on soft delete
        return;
    }
    // Update updatedBy/updatedAt only on real modifications
}
```

#### Controller Audit Endpoint (MANDATORY for all Controllers)

**ALL Controllers MUST override `getAuditInfo()` with permission check:**

```java
@RestController
@RequestMapping("/my-entity")
public class MyEntityController extends BaseController<MyDTO, MyGridDTO, MyService> {

    @Override
    @PreAuthorize("hasAuthority('MODULE_ENTITY_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }
}
```

#### Frontend Audit Component (MANDATORY for all Grid Components)

**ALL Grid components MUST implement audit visualization:**

1. **Imports:**
```typescript
import { AuditInfoComponent, AuditInfoData } from '../../../base/audit-info/audit-info.component';
import { Response } from '../../../base/model/response';
```

2. **Add to @Component imports:**
```typescript
@Component({
  imports: [AuditInfoComponent, /* other imports */]
})
```

3. **Properties:**
```typescript
showAuditInfo = false;
auditInfoData: AuditInfoData | null = null;
```

4. **Table action button (conditional on permission):**
```typescript
const canAudit = this.authService.hasPermission('MODULE_ENTITY_AUDITAR');
if (canAudit) {
  this.tableActions.push({
    icon: 'eye_tracking',
    iconType: 'material-symbols-outlined',
    title: 'Visualizar auditoria',
    action: (element: MyGridDTO) => this.loadAuditInfo(element.id),
  });
}
```

5. **Methods:**
```typescript
loadAuditInfo(id: string) {
  this.service.getAuditInfo(id).subscribe((response: Response<AuditInfoData>) => {
    if (response.body) {
      this.auditInfoData = response.body;
      this.showAuditInfo = true;
    }
  });
}

closeAuditInfo() {
  this.showAuditInfo = false;
  this.auditInfoData = null;
}
```

6. **Template (in footer-content, BEFORE pagination):**
```html
<div footer-content>
  @if (showAuditInfo && auditInfoData) {
    <gi-audit-info [auditData]="auditInfoData" (closeEvent)="closeAuditInfo()"></gi-audit-info>
  }
  <gi-pagination-component ...></gi-pagination-component>
</div>
```

**NOTE:** `getAuditInfo()` method is already available in `BaseService` - NO need to implement in entity services.

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
- Password: `@RLthotr$&u=Huge1e-r`
