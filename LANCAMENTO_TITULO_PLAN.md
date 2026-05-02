# Plano — Geração de Título a Receber ao Fechar Lançamento Financeiro

## Contexto

Ao fechar um lançamento financeiro, o sistema gera automaticamente um **Título a Receber**,
completando a ponte entre o módulo clínico e o módulo financeiro.

- **Particular / Pago no Ato**: pessoa do título = paciente
- **Convênio Faturado**: pessoa do título = pessoa jurídica do convênio

Quando o título for quitado (via movimentação financeira), o lançamento é marcado como **Pago**.

---

## Decisões Arquiteturais

### Botões renomeados
| Antes | Depois | Quando exibir |
|-------|--------|--------------|
| Pagar | Fechar para Pagamento | `isPagoNoAto()` |
| Fechar para Faturamento | Fechar para Faturamento | `!isPagoNoAto()` |

Ambos geram um Título a Receber. A diferença é quem é a pessoa do título.

### statusFinanceiro (semântica ajustada)
| Valor | Significado |
|-------|-------------|
| `PENDENTE` | Lançamento aberto, sem título gerado |
| `FATURADO` | Título gerado, aguardando pagamento |
| `PAGO` | Título vinculado foi quitado |

### Campos novos em LancamentoFinanceiro (snapshots)
Necessários para gerar o título sem depender de joins:
- `setorId` / `setorNome` — setor do atendimento (rateio 100%)
- `unidadeNegocioId` / `unidadeNegocioNome` — via `Setor → CentroCusto → UnidadeNegocio`
- `tituloId` (UUID nullable) — referência soft ao título gerado (não FK — evita acoplamento entre schemas)

### TituloCategoria seed
Inserir `codigo='ATEND'`, `nome='Atendimento'`, `tipo='RECEITA'` via migration idempotente.
O service busca pelo código `"ATEND"` em `TituloCategoriaRepository`.

### Sync lançamento ↔ título (quando título é pago)
`MovimentacaoFinanceiraServiceImpl.save()` — após salvar, se o título resultante ficou PAGO,
injeta `LancamentoFinanceiroRepository` e busca por `tituloId`; se encontrar, atualiza
`statusFinanceiro = PAGO`.
Mesma base de dados (tenant schema), mesma transação — acoplamento controlado e justificado.

### Título gerado (campos)
| Campo | Valor |
|-------|-------|
| `tipo` | `A_RECEBER` |
| `descricao` | `"Atendimento #" + atendimentoNumero` |
| `numeroDocumento` | `String.valueOf(atendimentoNumero)` |
| `pessoa` | Paciente (PAGO_NO_ATO) ou Convenio.pessoa (FATURADO) |
| `tituloCategoria` | `TituloCategoria` com `codigo = "ATEND"` |
| `unidadeNegocio` | `setor.centroCusto.unidadeNegocio` |
| `valorOriginal` | `lancamento.valorTotal` |
| `dataEmissao` | hoje |
| `dataVencimento` | hoje (padrão; pode ser parametrizado futuramente) |
| `observacoes` | ver formato abaixo |
| `setores` | `[setorDoAtendimento, 100%]` |
| `rateioAutomatico` | `false` |

**Formato das observações:**
```
Atendimento #7 — 02/05/2026
Paciente: Miguel Crescêncio Pereira
Convênio: Particular

Procedimentos:
- Atendimento TO (Particular): R$ 150,00
- TERAPIA ABA 20H (UNIMED NACIONAL): R$ 200,00

Valor total: R$ 350,00
```

---

## Checklist de Implementação

### ETAPA 1 — Backend: Infraestrutura

- [x] **1.1** Migration `V20260502000002__lancamento_titulo_fields.sql`
  - Adicionar em `lancamento_financeiro`:
    - `setor_id UUID` (nullable)
    - `setor_nome VARCHAR(150)` (nullable)
    - `unidade_negocio_id UUID` (nullable)
    - `unidade_negocio_nome VARCHAR(150)` (nullable)
    - `titulo_id UUID` (nullable — referência soft, sem FK)
  - Seed TituloCategoria:
    ```sql
    INSERT INTO titulo_categoria (id, codigo, nome, tipo, created_at, created_by, updated_at, updated_by)
    SELECT gen_random_uuid(), 'ATEND', 'Atendimento', 'RECEITA',
           CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration'
    WHERE NOT EXISTS (SELECT 1 FROM titulo_categoria WHERE codigo = 'ATEND');
    ```

- [x] **1.2** Atualizar `LancamentoFinanceiro.java`
- [x] **1.3** Atualizar `LancamentoFinanceiroValidator.java`
- [x] **1.4** Atualizar `LancamentoFinanceiroDTO.java`
- [x] **1.5** Atualizar `AtendimentoServiceImpl.java`

### ETAPA 2 — Backend: Geração do Título

- [x] **2.1** Criar `LancamentoTituloService.java` (interface)
- [x] **2.2** Criar `LancamentoTituloServiceImpl.java`
- [x] **2.3** Atualizar `LancamentoFinanceiroServiceImpl.java`
- [x] **2.4** Atualizar `LancamentoFinanceiroController.java`
- [x] **2.5** Atualizar `LancamentoFinanceiro.java` — lógica de domínio

### ETAPA 3 — Backend: Sincronização Título → Lançamento

- [x] **3.1** Atualizar `MovimentacaoFinanceiraServiceImpl.java`
- [x] **3.2** Atualizar `LancamentoFinanceiroRepository.java`

### ETAPA 4 — Frontend

- [x] **4.1** Atualizar `lancamento-financeiro.service.ts`
- [x] **4.2** Atualizar `lancamento-financeiro-detalhe.component.ts`
- [x] **4.3** Atualizar `lancamento-financeiro-detalhe.component.html`
- [x] **4.4** Atualizar `lancamento-financeiro-dto.ts`

### ETAPA 5 — Verificação

- [x] **5.1** Backend compila sem erros
- [x] **5.2** Migrations rodam sem erro
- [x] **5.3** Frontend compila sem erros (0 erros TypeScript)
- [x] **5.4** Testar fluxo PAGO_NO_ATO: atendimento #7 (Particular) → fechar para pagamento → título gerado com pessoa=Miguel Crescêncio Pereira ✓
- [x] **5.5** Testar fluxo FATURADO: atendimento #2 (UNIMED NACIONAL) → fechar para faturamento → título gerado com pessoa=UNIMED NACIONAL ✓
- [x] **5.6** Registrar movimentação financeira no título → lançamento muda para PAGO ✓
- [x] **5.7** Verificar observações do título (formato correto com procedimentos e valores) ✓
- [x] **5.8** Verificar rateio (setor "Atendimento KIDS", 100%) ✓

---

## Fluxo Completo

```
Atendimento salvo
  └── LancamentoFinanceiro criado/atualizado (snapshot inclui setorId, unidadeNegocioId)

Usuário abre LancamentoFinanceiro (ABERTO + PENDENTE)
  └── Clica "Fechar para Pagamento" ou "Fechar para Faturamento"
        └── LancamentoTituloService.gerarTitulo()
              ├── Pessoa: paciente (PAGO_NO_ATO) ou convenio.pessoa (FATURADO)
              ├── Categoria: TituloCategoria{codigo="ATEND"}
              ├── Setor: setorId do lançamento → 100%
              └── Titulo A_RECEBER gerado e salvo
        └── LancamentoFinanceiro: situacao=FECHADO, statusFinanceiro=FATURADO, tituloId=<id>

Financeiro registra MovimentacaoFinanceira no Título
  └── Titulo.registrarPagamento() → atualizarStatus() → StatusTitulo.PAGO
  └── MovimentacaoFinanceiraServiceImpl detecta titulo PAGO
        └── LancamentoFinanceiro.marcarComoPago() → statusFinanceiro=PAGO
```

---

## Pontos de Atenção

- **Setor obrigatório no atendimento**: se o lançamento não tiver `setorId`, o `fechar*()` deve
  rejeitar com mensagem clara. Verificar se atendimentos antigos terão este campo preenchido.

- **TituloCategoria "ATEND" ausente**: se a migration não rodou (base antiga), o service lança
  uma `BeanValidationException` com mensagem legível — nunca NPE silencioso.

- **Convenio sem pessoa (Particular seedado)**: Particular tem `pessoa_id = NULL`. Para FATURADO
  o convenio precisa ter pessoa. Para PAGO_NO_ATO usa o paciente — sem problema.

- **Valor zero**: se `valorTotal = 0`, o título será criado com valor zero. A tela de lançamento
  poderia exibir um aviso antes de fechar, mas não bloquear (o usuário pode querer registrar
  atendimentos gratuitos).

- **Rollback**: se a geração do título falhar, a transação inteira é revertida — o lançamento
  continua ABERTO + PENDENTE.

---

## Status

**Data de criação:** 2026-05-02
**Status:** Implementação concluída e verificada. Todos os itens do checklist concluídos.
