# Plano de Implementação — Módulo LancamentoFinanceiro

## Contexto

O `LancamentoFinanceiro` é a ponte entre o módulo clínico (`Atendimento`) e o módulo financeiro (`Título`, `Caixa`).
Ao salvar um atendimento, o sistema cria automaticamente um lançamento financeiro com snapshot dos dados clínicos.
O usuário é redirecionado para a tela de revisão do lançamento antes de faturar.

---

## Decisões Arquiteturais

- **Pacote backend:** `atendimento/lancamento/` (pertence ao domínio de atendimento)
- **Rota frontend:** `/atendimento/lancamento`
- **Snapshot pattern:** Os dados do paciente, convênio e procedimentos são copiados no momento do lançamento — alterações posteriores no atendimento não retroagem
- **Procedimentos independentes:** `LancamentoFinanceiroProcedimento` é uma cópia editável dos itens do atendimento — podem ser adicionados, removidos ou alterados no financeiro sem comprometer o registro clínico
- **Status:** `PENDENTE → FATURADO` (gera Título a Receber) | `PENDENTE → CANCELADO`
- **FATURADO** cobre tanto pagamento no ato quanto faturamento para cobrança posterior
- **Geração automática:** AtendimentoServiceImpl cria o LancamentoFinanceiro após salvar o Atendimento
- **Navegação:** Frontend navega para `/atendimento/lancamento/detalhe/<id>` via router state

---

## Checklist de Implementação

### ETAPA 1 — Backend: Entidade e Infraestrutura

- [x] **1.1** Criar `LancamentoFinanceiroStatusEnum.java`
  - Pacote: `atendimento/lancamento/entity/`
  - Valores: `PENDENTE`, `FATURADO`, `CANCELADO`

- [x] **1.2** Criar `LancamentoFinanceiroValidator.java`
  - Pacote: `atendimento/lancamento/`
  - Valida: `atendimentoId` obrigatório, `valorTotal` obrigatório e >= 0, `status` obrigatório

- [x] **1.3** Criar `LancamentoFinanceiroProcedimento.java` (entidade filha)
  - Pacote: `atendimento/lancamento/entity/`
  - Estende `BaseEntity`
  - Campos:
    ```java
    LancamentoFinanceiro lancamento  // @ManyToOne FK
    UUID procedimentoId              // snapshot
    String procedimentoCodigo        // snapshot
    String procedimentoDescricao     // snapshot
    UUID convenioId                  // snapshot (nullable — pode diferir do lançamento)
    String convenioNome              // snapshot (nullable)
    UUID tabelaItemId                // snapshot (nullable)
    @Embedded Money valor            // editável no financeiro
    ```
  - Sem Builder (objeto simples criado pelo LancamentoFinanceiro)
  - `atualizar(Money novoValor)` para ajuste de glosa

- [x] **1.4** Criar `LancamentoFinanceiro.java` (entidade)
  - Pacote: `atendimento/lancamento/entity/`
  - Estende `BaseEntity`
  - Campos:
    ```java
    UUID atendimentoId           // snapshot — não FK
    Long atendimentoNumero       // snapshot
    LocalDate dataAtendimento    // snapshot
    UUID pacienteId              // snapshot
    String pacienteNome          // snapshot
    UUID convenioId              // snapshot (nullable)
    String convenioNome          // snapshot (nullable)
    @Embedded Money valorTotal   // calculado = soma dos procedimentos (atualizado ao salvar)
    LancamentoFinanceiroStatusEnum status
    String observacoes
    @OneToMany(cascade = ALL, orphanRemoval = true)
    List<LancamentoFinanceiroProcedimento> procedimentos
    ```
  - Builder + `atualizar()` usando `LancamentoFinanceiroValidator`
  - `syncProcedimentos(List<LancamentoFinanceiroProcedimento>)` — mesmo padrão do Atendimento
  - `recalcularValorTotal()` — soma `procedimento.getValor()` de todos os itens
  - Métodos de domínio: `faturar()`, `cancelar()` (validam transições de status)

- [x] **1.5** Criar migration SQL
  - Arquivo: `db/tenant-migrations/V20260501000003__create_lancamento_financeiro.sql`
  - Tabela `lancamento_financeiro`: id, atendimento_id, atendimento_numero, data_atendimento, paciente_id, paciente_nome, convenio_id, convenio_nome, valor_total, status, observacoes + auditoria
  - Tabela `lancamento_financeiro_procedimento`: id, lancamento_financeiro_id (FK), procedimento_id, procedimento_codigo, procedimento_descricao, convenio_id, convenio_nome, tabela_item_id, valor + auditoria
  - Índices: `idx_lancamento_financeiro_deleted`, `idx_lancamento_financeiro_procedimento_lancamento`
  - Incluir: `INSERT INTO modulo`, `INSERT INTO perfil_modulo`
  - Constraint: `chk_lancamento_financeiro_status` com `IN ('PENDENTE','FATURADO','CANCELADO')`

- [x] **1.6** Atualizar `DatabaseConstraintsEnum.java`
  - Adicionar: `CHK_LANCAMENTO_FINANCEIRO_STATUS`, `FK_LANCAMENTO_FINANCEIRO_PROCEDIMENTO_LANCAMENTO`

- [x] **1.7** Atualizar `DataSourceConfig.ENTITY_PACKAGES`
  - Adicionar pacote `atendimento.lancamento.entity`

### ETAPA 2 — Backend: Camada de Serviço

- [x] **2.1** Criar `LancamentoFinanceiroProcedimentoDTO.java`
  - Pacote: `atendimento/lancamento/dto/`
  - Campos: `id`, `procedimentoId`, `procedimentoCodigo`, `procedimentoDescricao`, `convenioId`, `convenioNome`, `tabelaItemId`, `valor` (BigDecimal)
  - Lombok `@Builder @Data`

- [x] **2.2** Criar `LancamentoFinanceiroDTO.java`
  - Pacote: `atendimento/lancamento/dto/`
  - Todos os campos da entidade + `List<LancamentoFinanceiroProcedimentoDTO> procedimentos` + campos de auditoria
  - Lombok `@Builder @Data`

- [x] **2.3** Criar `LancamentoFinanceiroGridDTO.java`
  - Campos: `id`, `atendimentoNumero`, `dataAtendimento`, `pacienteNome`, `convenioNome`, `valorTotal`, `status`, `procedimentosCount`, `createdAt`, `deleted`

- [x] **2.4** Criar `LancamentoFinanceiroRepository.java`
  - Estende `JpaRepository<LancamentoFinanceiro, UUID>`, `JpaSpecificationExecutor`
  - Query: `findByAtendimentoId(UUID atendimentoId): Optional<LancamentoFinanceiro>`

- [x] **2.5** Criar `LancamentoFinanceiroService.java` (interface)
  - Estende `CrudService<LancamentoFinanceiroDTO, LancamentoFinanceiroGridDTO>`
  - Métodos adicionais: `faturar(UUID id)`, `cancelar(UUID id)`

- [x] **2.6** Criar `LancamentoFinanceiroServiceImpl.java`
  - Estende `CrudServiceImpl<...>`
  - `mergeEntityAndDTO`: cria/atualiza lançamento + sincroniza procedimentos via `syncProcedimentos()`
  - `resolverProcedimentos()`: converte `LancamentoFinanceiroProcedimentoDTO` → `LancamentoFinanceiroProcedimento`
  - Após sync, chama `entity.recalcularValorTotal()` antes de salvar
  - `faturar(UUID id)`: chama `entity.faturar()`, salva — no futuro gera Título
  - `cancelar(UUID id)`: chama `entity.cancelar()`, salva

- [x] **2.7** Criar `LancamentoFinanceiroController.java`
  - `@RestController @RequestMapping("/lancamento-financeiro")`
  - Estende `BaseController`
  - Endpoints adicionais:
    - `POST /lancamento-financeiro/{id}/faturar`
    - `POST /lancamento-financeiro/{id}/cancelar`
  - `@PreAuthorize` em todos os endpoints

- [x] **2.8** Atualizar `AtendimentoServiceImpl.java`
  - Injetar `LancamentoFinanceiroService`
  - No `mergeEntityAndDTO` (após salvar): chamar `criarOuAtualizarLancamento(atendimento)`
  - `criarOuAtualizarLancamento()`: copia os procedimentos do atendimento para `LancamentoFinanceiroProcedimentoDTO` (snapshot de id, codigo, descricao, convenio, tabelaItem, valor); cria ou atualiza o lançamento
  - Se lançamento já existe e status = FATURADO/CANCELADO: NÃO atualizar os procedimentos (preservar o que foi faturado)
  - `AtendimentoDTO` ganha campo `lancamentoFinanceiroId`

### ETAPA 3 — Frontend: Model e Service

- [ ] **3.1** Criar `LancamentoFinanceiroStatusEnum.ts`
  - Arquivo: `model/atendimento/lancamento-financeiro-status.enum.ts`
  - Valores: `PENDENTE`, `FATURADO`, `CANCELADO`
  - Método `getLabel()` para exibição

- [ ] **3.2** Criar `LancamentoFinanceiroProcedimentoDTO.ts`
  - Arquivo: `model/atendimento/lancamento-financeiro-procedimento.dto.ts`
  - Classe com `@Exclude`/`@Expose`
  - Campos: `id`, `procedimentoId`, `procedimentoCodigo`, `procedimentoDescricao`, `convenioId`, `convenioNome`, `tabelaItemId`, `valor` (number)

- [ ] **3.3** Criar `LancamentoFinanceiroDTO.ts`
  - Arquivo: `model/atendimento/lancamento-financeiro.dto.ts`
  - Classe com `@Exclude`/`@Expose` (class-transformer)
  - `@Transform` para `status` (enum) e `valorTotal` (number)
  - Campo: `procedimentos: LancamentoFinanceiroProcedimentoDTO[]` com `@Type(() => LancamentoFinanceiroProcedimentoDTO)`

- [ ] **3.4** Criar `LancamentoFinanceiroGridDTO.ts`
  - Arquivo: `model/atendimento/lancamento-financeiro-grid.dto.ts`

- [ ] **3.5** Criar `LancamentoFinanceiroService.ts`
  - Arquivo: `components/atendimento/lancamento/lancamento-financeiro.service.ts`
  - Estende `BaseService<LancamentoFinanceiroDTO, LancamentoFinanceiroGridDTO>`
  - `super(inject(HttpClient), inject(MessageService))` — 2 args
  - `getDomain()` → `'lancamento-financeiro'`
  - Métodos adicionais: `faturar(id)`, `cancelar(id)` (POST requests)

### ETAPA 4 — Frontend: Componentes

- [ ] **4.1** Criar componente `lancamento-financeiro-detalhe`
  - Arquivo: `components/atendimento/lancamento/detalhe/`
  - Input: `@Input() detailId: string` (UUID ou 'add')
  - Output: `@Output() closeDetail: EventEmitter<string>`
  - Seção de cabeçalho (somente leitura): paciente, convênio, data, Nº atendimento
  - Status exibido com badge colorido (PENDENTE=amarelo, FATURADO=verde, CANCELADO=vermelho)
  - **Tabela de procedimentos editável** (quando status = PENDENTE):
    - Colunas: Procedimento, Convênio, Valor (editável inline ou via dialog)
    - Botões: adicionar linha, remover linha
    - Valor total recalculado ao editar
  - Campo `observacoes` editável
  - Botões de ação: "Salvar" (atualiza procedimentos/obs), "Faturar", "Cancelar Lançamento"
  - Quando status = FATURADO/CANCELADO: tudo somente leitura
  - Ao faturar: exibe mensagem de sucesso + emite `closeDetail`

- [ ] **4.2** Criar componente `lancamento-financeiro-grid`
  - Arquivo: `components/atendimento/lancamento/grid/`
  - Colunas: Nº Atendimento, Data, Paciente, Convênio, Valor Total, Status, Data Criação
  - Filtro por status
  - Botão de auditoria (`AuditInfoComponent`)
  - Confirmação de exclusão via `DialogService.showYesNo()`

- [ ] **4.3** Criar componente `lancamento-financeiro-main`
  - Arquivo: `components/atendimento/lancamento/lancamento-financeiro.component.ts`
  - Controla `viewMode`: `'GRID'` | `'DETAIL'`
  - Recebe router state: `lancamentoId` (para navegação direta do atendimento)

- [ ] **4.4** Atualizar `atendimento-detalhe.component.ts`
  - Após salvar com sucesso: navegar para lançamento
    ```typescript
    onSuccess: (saved) => {
      if (saved.lancamentoFinanceiroId) {
        this.router.navigate(
          ['/atendimento/lancamento'],
          { state: { lancamentoId: saved.lancamentoFinanceiroId } }
        );
      } else {
        this.closeDetail.emit(saved.id ?? '');
      }
    }
    ```

### ETAPA 5 — Roteamento e Menu

- [ ] **5.1** Adicionar `LANCAMENTO_FINANCEIRO` em `SystemModuleKey` enum
  - Arquivo: `components/base/enum/system-module-key.enum.ts`

- [ ] **5.2** Criar rotas do módulo
  - Arquivo: `components/atendimento/lancamento/lancamento-financeiro.routes.ts`
  - Rota: `{ path: 'lancamento', component: LancamentoFinanceiroComponent }`

- [ ] **5.3** Adicionar rota ao módulo de atendimento
  - Arquivo: `components/atendimento/atendimento.routes.ts`

- [ ] **5.4** Adicionar item de menu
  - Arquivo: onde o menu lateral é configurado (verificar `app-menu` ou `sidebar`)
  - Grupo: Atendimento / Financeiro do Atendimento

### ETAPA 6 — Verificação Final

- [ ] **6.1** Backend compila sem erros (`./mvnw compile`)
- [ ] **6.2** Migrations rodam sem erro (subir Docker e verificar logs)
- [ ] **6.3** Frontend compila sem erros (`ng build --configuration=development`)
- [ ] **6.4** Testar fluxo completo: criar atendimento → auto-criar lançamento → revisar → faturar
- [ ] **6.5** Verificar grid de lançamentos
- [ ] **6.6** Verificar navegação de volta ao atendimento

---

## Fluxo de Navegação

```
AtendimentoDetalhe.salvar()
  └── AtendimentoServiceImpl.mergeEntityAndDTO()
        └── criarOuAtualizarLancamento(atendimento)  ← novo
  └── AtendimentoDTO { lancamentoFinanceiroId: <id> }  ← novo campo
  └── Frontend recebe saved.lancamentoFinanceiroId
  └── router.navigate(['/atendimento/lancamento'], { state: { lancamentoId: <id> } })
        └── LancamentoFinanceiroMain lê history.state.lancamentoId
        └── Abre LancamentoFinanceiroDetalhe em modo edição
              └── Usuário revisa e clica "Faturar"
              └── service.faturar(id) → status = FATURADO
              └── Emite closeDetail → volta para grid de lançamentos
```

---

## Cálculo do Valor Total

O `valorTotal` é sempre derivado da soma dos procedimentos — nunca editado diretamente.

**Na criação (AtendimentoServiceImpl):** copia os procedimentos do atendimento como snapshot inicial.

**Na edição (LancamentoFinanceiroServiceImpl):** após `syncProcedimentos()`, chama `entity.recalcularValorTotal()`:

```java
// LancamentoFinanceiro.java
public void recalcularValorTotal() {
    this.valorTotal = procedimentos.stream()
        .map(LancamentoFinanceiroProcedimento::getValor)
        .filter(Objects::nonNull)
        .reduce(Money.ZERO, Money::add);
}
```

**Regra de proteção:** se o lançamento já está FATURADO ou CANCELADO, `mergeEntityAndDTO` não atualiza procedimentos nem recalcula valor — a integridade financeira é preservada.

**Se nenhum procedimento tiver valor:** `valorTotal = Money.ZERO` — tela de revisão deve alertar o usuário antes de faturar.

---

## Status da Implementação

**Data de criação do plano:** 2026-05-01  
**Status:** Aguardando implementação (Etapa 1 não iniciada)
