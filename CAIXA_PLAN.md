# Plano — Módulo de Caixa

## Contexto

O módulo de Caixa centraliza o recebimento presencial dos lançamentos financeiros gerados
pelo módulo de atendimento. O operador abre uma sessão de caixa, seleciona os lançamentos a
receber, registra a forma de pagamento e fecha o caixa com um relatório de conferência.

---

## Análise e Decisões Arquiteturais

### Entidades novas

| Entidade | Responsabilidade |
|---|---|
| `Caixa` | Cadastro físico/lógico (regras de operação) |
| `UsuarioCaixa` | Quais usuários têm acesso a qual caixa |
| `AberturaCaixa` | Sessão (uma ativa por caixa por vez) |
| `MovimentacaoCaixa` | Cada transação da sessão: recebimento, sangria, suprimento |

### Entidades/enums alterados

| Arquivo | Alteração |
|---|---|
| `LancamentoFinanceiroStatusFinanceiroEnum` | Adicionar `PAGO_PARCIAL` |
| `LancamentoFinanceiro.java` | Adicionar campo snapshot `tipoCobranca` (PAGO_NO_ATO / FATURADO) |
| `MovimentacaoFinanceiraServiceImpl` | Detectar `PARCIAL` → atualizar lançamento para `PAGO_PARCIAL` |

> `StatusTitulo.PARCIAL` **já existe** e `permiteMovimentacao()` já cobre ABERTO/PARCIAL/VENCIDO. Não precisa ser criado.

### Semântica do statusFinanceiro = FATURADO (importante)

O status `FATURADO` cobre dois casos distintos que o caixa precisa diferenciar:

| Caso | O que significa `FATURADO` | Quem paga |
|---|---|---|
| `tipoCobranca = PAGO_NO_ATO` | Título gerado, aguardando recebimento no caixa | Paciente/responsável presencialmente |
| `tipoCobranca = FATURADO` | Enviado para cobrança do convênio/plano de saúde | Convênio via movimentação financeira direta |

Para isso, o campo `tipoCobranca` deve ser denormalizado como snapshot em `lancamento_financeiro`
(similar a `setorId`, `setorNome`). O caixa filtra somente `tipoCobranca = PAGO_NO_ATO`.

### Regras do Caixa

| Campo | Semântica |
|---|---|
| `valorPadraoAbertura` | Sugerido ao abrir, editável pelo operador |
| `percentualPagamentoParcial` | `null` = não permite parcial. Ex.: `50` = mínimo 50 % do saldo devedor |
| `valorMinimoParcela` | Valor mínimo de cada parcela de cartão crédito. `null` = sem restrição |

### Regras de negócio

- **Uma sessão aberta por caixa** — validar no endpoint de abertura.
- **Acesso restrito** — exibir somente caixas vinculados ao usuário logado.
- **Pagamento parcial** — só permitido se `percentualPagamentoParcial != null` e
  `valorRecebido >= saldoDevedor * percentualPagamentoParcial / 100`.
- **Crédito parcelado** — `valorRecebido / numeroParcelas >= valorMinimoParcela` (se configurado).
- **Código NSU** — identificador da transação emitido pela maquininha (obrigatório em débito/crédito).
- **PIX** — comprovante como upload de arquivo (PDF/imagem).
- **Sangria / Suprimento** — mesma tabela `movimentacao_caixa`, campo `tipo`.
- **Imutabilidade de sessão fechada** — ao fechar a sessão (`AberturaCaixa.status = FECHADO`),
  todas as `movimentacao_caixa` da sessão tornam-se somente leitura. Nenhum cancelamento
  ou edição é permitido após o fechamento. Apenas uma nova sessão pode gerar novas movimentações.
- **Associação lançamento → caixa** — `movimentacao_caixa.lancamento_financeiro_id` é a chave
  de rastreabilidade. Deve existir endpoint `GET /movimentacao-caixa/por-lancamento/{id}` para
  o detalhe do lançamento exibir em qual recebimento de caixa o lançamento foi liquidado.

### Fluxo principal

```
Operador seleciona Caixa → Abre sessão (informa valor inicial)
  └── Tela do caixa ativo
        ├── Selecionar Lançamentos (status FATURADO, título ABERTO ou PARCIAL)
        │     └── Para cada lançamento: escolher forma de pagamento → confirmar
        │           └── Cria MovimentacaoFinanceira → Titulo (PAGO ou PARCIAL)
        │                 └── LancamentoFinanceiro → PAGO ou PAGO_PARCIAL
        ├── Registrar Sangria / Suprimento (operações de dinheiro físico)
        └── Fechar caixa → Relatório de conferência
```

### Idéias de mercado incluídas

- **Sangria e Suprimento**: retirada ou reforço de dinheiro físico no caixa, registrado com observação.
- **Conferência no fechamento**: operador informa o valor físico contado; sistema mostra diferença.
- **Relatório de fechamento**: totais por forma de pagamento, sangrias, suprimentos, saldo esperado.
- **Histórico de sessões**: lista de todas as aberturas/fechamentos para auditoria.
- **Multi-recebimento**: selecionar vários lançamentos para receber em sequência na mesma sessão.
- **Snapshot de operador**: `usuario_nome` gravado no momento da abertura/fechamento, para auditoria mesmo se o usuário for renomeado.

---

## Modelo de Dados (SQL resumido)

```sql
-- Tabela: caixa
CREATE TABLE caixa (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nome VARCHAR(150) NOT NULL,
  valor_padrao_abertura NUMERIC(15,2) NOT NULL DEFAULT 0,
  percentual_pagamento_parcial NUMERIC(5,2),      -- null = não permite parcial
  valor_minimo_parcela NUMERIC(15,2),             -- null = sem restrição de parcela
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  -- demais campos de auditoria (BaseEntity)
);

-- Tabela: usuario_caixa (sem FK cruzada de schema)
CREATE TABLE usuario_caixa (
  usuario_id UUID NOT NULL,    -- soft ref ao usuário (sem FK — cross-schema)
  caixa_id UUID NOT NULL REFERENCES caixa(id),
  PRIMARY KEY (usuario_id, caixa_id)
);

-- Tabela: abertura_caixa
CREATE TABLE abertura_caixa (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  caixa_id UUID NOT NULL REFERENCES caixa(id),
  usuario_id UUID NOT NULL,                       -- soft ref
  usuario_nome VARCHAR(200) NOT NULL,             -- snapshot
  data_abertura TIMESTAMP NOT NULL,
  valor_abertura NUMERIC(15,2) NOT NULL,
  status VARCHAR(20) NOT NULL,                    -- ABERTO, FECHADO
  data_fechamento TIMESTAMP,
  usuario_fechamento_id UUID,                     -- soft ref
  usuario_fechamento_nome VARCHAR(200),           -- snapshot
  valor_conferencia NUMERIC(15,2),                -- informado ao fechar
  observacoes TEXT,
  -- demais campos de auditoria
);

-- Tabela: movimentacao_caixa
CREATE TABLE movimentacao_caixa (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  abertura_caixa_id UUID NOT NULL REFERENCES abertura_caixa(id),
  tipo VARCHAR(20) NOT NULL,                      -- RECEBIMENTO, SANGRIA, SUPRIMENTO
  forma_pagamento VARCHAR(20),                    -- DINHEIRO, DEBITO, CREDITO, PIX
  titulo_id UUID,                                 -- soft ref (apenas RECEBIMENTO)
  lancamento_financeiro_id UUID,                  -- soft ref (apenas RECEBIMENTO)
  movimentacao_financeira_id UUID,                -- soft ref — rastreabilidade
  valor_titulo NUMERIC(15,2),                     -- saldo devedor no momento
  valor_recebido NUMERIC(15,2) NOT NULL,
  valor_troco NUMERIC(15,2),                      -- DINHEIRO: troco calculado
  nsu VARCHAR(50),                                -- DEBITO/CREDITO: código da maquininha
  num_parcelas INTEGER,                           -- CREDITO: número de parcelas
  comprovante_pix_path VARCHAR(500),              -- PIX: path do arquivo
  data_hora TIMESTAMP NOT NULL,
  observacoes TEXT,
  -- demais campos de auditoria
);
```

---

## Checklist de Implementação

### ETAPA 1 — Cadastro de Caixa (backend + frontend)

- [ ] **1.1** Migration `V20260502000003__create_caixa.sql`
  - Tabelas: `caixa`, `usuario_caixa`
  - Seed módulo: `CADASTRO_CAIXA` (INSERT idempotente)
  - Constraints: `uk_caixa_nome`

- [ ] **1.2** `Caixa.java` — entidade (Builder + CaixaValidator)
  - Campos: `nome`, `valorPadraoAbertura`, `percentualPagamentoParcial`, `valorMinimoParcela`, `ativo`

- [ ] **1.3** `DatabaseConstraintsEnum` — registrar `uk_caixa_nome`

- [ ] **1.4** `DataSourceConfig.ENTITY_PACKAGES` — adicionar pacote do Caixa

- [ ] **1.5** `CaixaDTO.java`, `CaixaGridDTO.java`

- [ ] **1.6** `CaixaRepository.java`, `CaixaService.java`, `CaixaServiceImpl.java`, `CaixaController.java`
  - Endpoint extra: `GET /caixa/meus-caixas` — retorna apenas os caixas do usuário logado

- [ ] **1.7** Frontend: `caixa.service.ts`, `CaixaDTO`, `CaixaGridDTO`

- [ ] **1.8** Frontend: `caixa-grid.component` + `caixa-detalhe.component`
  - No detalhe: campos normais + seção "Usuários com acesso" (lista de usuários vinculados ao caixa)

- [ ] **1.9** `SystemModuleKey` + rotas + menu (grupo Financeiro)

**Testável:** criar caixa "Caixa 1" com valorPadrão=200, percentualParcial=50; listar; editar; excluir.

---

### ETAPA 2 — Controle de Acesso Usuário × Caixa

- [ ] **2.1** Backend: endpoint `PUT /caixa/{id}/usuarios` — recebe lista de `usuarioId` e substitui
- [ ] **2.2** Backend: endpoint `GET /caixa/{id}/usuarios` — lista usuários vinculados
- [ ] **2.3** Frontend (detalhe do Caixa): seção "Usuários com acesso"
  - `EntitySearch` para buscar usuários + lista dos já vinculados com botão de remover

**Testável:** vincular dois usuários ao Caixa 1; confirmar que apenas esses usuários veem o caixa no módulo de recebimentos.

---

### ETAPA 3 — Sessão de Caixa: Abertura e Fechamento (sem recebimentos)

- [ ] **3.1** Migration `V20260502000004__create_abertura_caixa.sql`
  - Tabela: `abertura_caixa`
  - Constraint: `uk_abertura_caixa_ativa` (parcial: WHERE status = 'ABERTO')

- [ ] **3.2** `AberturaCaixa.java`, `AberturaCaixaValidator.java`
  - Status: `ABERTO`, `FECHADO`
  - Método: `fechar(valorConferencia, observacoes)`

- [ ] **3.3** `AberturaCaixaDTO.java`, `AberturaCaixaGridDTO.java`

- [ ] **3.4** `AberturaCaixaService.java`, `AberturaCaixaServiceImpl.java`, `AberturaCaixaController.java`
  - `POST /abertura-caixa/abrir` — recebe `caixaId` + `valorAbertura`; valida se já há sessão ativa
  - `POST /abertura-caixa/{id}/fechar` — recebe `valorConferencia` + `observacoes`
  - `GET /abertura-caixa/ativa?caixaId={}` — retorna sessão ativa do caixa (ou 404)
  - `GET /abertura-caixa` — histórico paginado

- [ ] **3.5** Frontend: tela "Caixa" (módulo operacional, separado do cadastro)
  - Seleção do caixa (apenas os do usuário logado)
  - Botão "Abrir Caixa" → modal com valor de abertura (padrão do cadastro)
  - Sessão ativa exibida em tela com botão "Fechar Caixa"

**Testável:** abrir Caixa 1 com valor=R$200; confirmar status ABERTO; tentar abrir novamente (deve rejeitar); fechar; ver histórico.

---

### ETAPA 4 — Recebimentos: Backend

- [ ] **4.1** Adicionar `PAGO_PARCIAL` em `LancamentoFinanceiroStatusFinanceiroEnum`

- [ ] **4.2** Atualizar `MovimentacaoFinanceiraServiceImpl.save()`
  - Além de PAGO → lançamento PAGO, adicionar: PARCIAL → lançamento PAGO_PARCIAL

- [ ] **4.3** Migration `V20260502000005__create_movimentacao_caixa.sql`
  - Tabela: `movimentacao_caixa`

- [ ] **4.4** `MovimentacaoCaixa.java`, `MovimentacaoCaixaValidator.java`
  - Enum `TipoMovimentacaoCaixa`: `RECEBIMENTO`, `SANGRIA`, `SUPRIMENTO`
  - Enum `FormaPagamentoCaixa`: `DINHEIRO`, `DEBITO`, `CREDITO`, `PIX`

- [ ] **4.5** `MovimentacaoCaixaDTO.java`, `MovimentacaoCaixaGridDTO.java`

- [ ] **4.6** `MovimentacaoCaixaService.java`, `MovimentacaoCaixaServiceImpl.java`
  - `registrarRecebimento(MovimentacaoCaixaDTO dto)`:
    1. Validar que a `aberturaCaixa` está `ABERTO`
    2. Carregar `Titulo` via `tituloId` → verificar `permiteMovimentacao()`
    3. Validar regras do caixa:
       - Parcial: `percentualPagamentoParcial` nulo → rejeitar se `valorRecebido < saldoDevedor`
       - Parcial: se configurado → `valorRecebido >= saldoDevedor * percentual / 100`
       - Crédito: `valorRecebido / numParcelas >= valorMinimoParcela`
    4. Calcular `troco` (DINHEIRO: `max(0, valorRecebido - saldoDevedor)`)
    5. Chamar `MovimentacaoFinanceiraService.save()` → cria movimentação no título
    6. Salvar `MovimentacaoCaixa` com `movimentacaoFinanceiraId`
  - `registrarSangria(aberturaCaixaId, valor, observacoes)`
  - `registrarSuprimento(aberturaCaixaId, valor, observacoes)`

- [ ] **4.7** `MovimentacaoCaixaController.java`
  - `POST /movimentacao-caixa/receber`
  - `POST /movimentacao-caixa/sangria`
  - `POST /movimentacao-caixa/suprimento`
  - `GET /movimentacao-caixa?aberturaCaixaId={}` — lista da sessão

- [ ] **4.8** Snapshot `tipoCobranca` em `LancamentoFinanceiro`
  - Migration: adicionar coluna `tipo_cobranca VARCHAR(20)` em `lancamento_financeiro`
  - Preencher no `AtendimentoServiceImpl` junto com os outros snapshots do convênio
  - Atualizar `LancamentoFinanceiro.java`, `LancamentoFinanceiroDTO.java`, `LancamentoFinanceiroValidator.java`

- [ ] **4.9** Endpoint de lançamentos disponíveis para receber:
  - `GET /lancamento-financeiro/disponiveis-para-caixa` — retorna lançamentos com
    `tipoCobranca = PAGO_NO_ATO`
    AND `statusFinanceiro IN (FATURADO, PAGO_PARCIAL)`
    AND título em `permiteMovimentacao()` (ABERTO ou PARCIAL)

- [ ] **4.10** Endpoint de consulta por lançamento:
  - `GET /movimentacao-caixa/por-lancamento/{lancamentoId}` — retorna a `MovimentacaoCaixa`
    de tipo RECEBIMENTO associada ao lançamento (se houver), incluindo o status da sessão
    (`aberturaCaixa.status`: ABERTO ou FECHADO)

**Testável (via Swagger/curl):** receber atendimento #2 em PIX R$200; confirmar título PAGO e lançamento PAGO; receber atendimento com R$50 quando regra não permite parcial (deve rejeitar).

---

### ETAPA 5 — Recebimentos: Frontend

- [ ] **5.1** `movimentacao-caixa.service.ts` com métodos `receber()`, `sangria()`, `suprimento()`

- [ ] **5.2** Tela principal do caixa ativo:
  - Lista de lançamentos disponíveis (paginada, com busca por paciente/convênio)
  - Para cada lançamento: paciente, convênio, valor total, saldo devedor, status

- [ ] **5.3** Modal de recebimento (por lançamento selecionado):
  - Seleção da forma de pagamento (abas ou dropdown)
  - **Dinheiro**: campo "valor recebido" → troco calculado em tempo real
  - **Débito**: campo NSU (obrigatório)
  - **Crédito**: campos NSU + nº parcelas; validação do valor mínimo da parcela em tempo real
  - **Pix**: upload de comprovante (PDF/imagem)
  - Campo "valor recebido" editável em todas as formas (para pagamento parcial)
  - Botão "Confirmar"

- [ ] **5.4** Após confirmação: atualizar lista de lançamentos (remover os totalmente pagos, manter os parciais)

- [ ] **5.5** Painel "Caixa" no detalhe do LancamentoFinanceiro
  - Sempre visível (independente de status), carrega via `GET /movimentacao-caixa/por-lancamento/{id}`
  - **Sem recebimento anterior** → botão "Receber no Caixa"
    - Navega para `/financeiro/caixa` com `lancamentoId` no Router state
    - Se usuário **tem sessão ativa**: abre modal de recebimento pré-preenchido
    - Se **não tem sessão ativa**: passa pelo fluxo de seleção/abertura de caixa e, ao concluir, abre o modal
  - **Com recebimento existente (sessão ABERTO)** → exibe dados do recebimento (forma de pagamento,
    valor, data/hora, operador, caixa) — possível cancelamento futuro
  - **Com recebimento existente (sessão FECHADO)** → exibe os mesmos dados em modo somente leitura,
    com indicação visual clara de que a sessão foi encerrada e o registro é imutável

**Testável:** no detalhe de um lançamento PAGO_NO_ATO FATURADO, clicar "Receber no Caixa" → modal abre; após confirmar, painel mostra os dados do recebimento; fechar a sessão e confirmar que painel fica somente leitura; testar sem caixa aberto (deve abrir fluxo de seleção primeiro).

---

### ETAPA 6 — Fechamento e Relatório

- [ ] **6.1** Backend: `GET /abertura-caixa/{id}/relatorio`
  - Retorna:
    - `valorAbertura`
    - Totais por forma de pagamento: dinheiro, débito, crédito, pix
    - Total de sangrias, total de suprimentos
    - `saldoEsperadoDinheiro` = abertura + recebimentos dinheiro - sangrias + suprimentos
    - `valorConferencia` (se já fechado)
    - `diferenca` = saldoEsperado - valorConferencia

- [ ] **6.2** Frontend: modal de fechamento
  - Resumo por forma de pagamento
  - Campo "Valor conferido em caixa" (dinheiro físico contado)
  - Sistema exibe diferença (sobra/falta)
  - Botão "Confirmar Fechamento"

- [ ] **6.3** Após fechar: exibir relatório completo (imprimível / exportável para PDF)

**Testável:** abrir caixa, fazer 3 recebimentos (dinheiro + pix + débito), 1 sangria; fechar; verificar totais no relatório; verificar diferença ao informar valor errado na conferência.

---

### ETAPA 7 — Sangria e Suprimento (frontend)

- [ ] **7.1** Botão "Sangria" na tela do caixa ativo → modal: valor + observação
- [ ] **7.2** Botão "Suprimento" na tela do caixa ativo → modal: valor + observação
- [ ] **7.3** Lista de movimentações da sessão (mini-extrato no rodapé da tela)

**Testável:** registrar sangria de R$100; ver no relatório; confirmar que saldo esperado diminui.

---

### ETAPA 8 — Verificação Final

- [ ] **8.1** Backend e frontend compilam sem erros
- [ ] **8.2** Migrations rodam sem erros
- [ ] **8.3** Fluxo completo PAGO_NO_ATO: atendimento → lançamento → caixa → receber → título PAGO → lançamento PAGO
- [ ] **8.4** Fluxo FATURADO: atendimento convênio → caixa → receber → título PAGO → lançamento PAGO
- [ ] **8.5** Pagamento parcial autorizado: valor < saldo, título PARCIAL, lançamento PAGO_PARCIAL
- [ ] **8.6** Pagamento parcial bloqueado: regra `percentualPagamentoParcial = null` → mensagem clara
- [ ] **8.7** Crédito parcelado: 3x R$70 = R$210 com valorMínimoParcela = R$30 → permite; 10x R$5 = R$50 com min R$30 → rejeita
- [ ] **8.8** NSU obrigatório no débito (backend rejeita sem NSU)
- [ ] **8.9** PIX com comprovante anexado
- [ ] **8.10** Sangria + Suprimento refletem corretamente no relatório de fechamento
- [ ] **8.11** Dois usuários diferentes: cada um vê apenas os caixas vinculados a ele
- [ ] **8.12** Tentativa de abrir caixa já aberto → mensagem de erro clara

---

## Fluxo Completo

```
Usuário logado
  └── Tela "Caixa" → ver somente caixas vinculados a ele          ← entrada direta
        └── Selecionar caixa → Abrir sessão (valor abertura)
              └── Tela do caixa ativo
                    ├── Listar lançamentos disponíveis
                    │     └── Selecionar lançamento → Modal de recebimento
                    │           ├── DINHEIRO: valorRecebido → troco
                    │           ├── DEBITO: NSU
                    │           ├── CREDITO: NSU + parcelas (validar min parcela)
                    │           └── PIX: upload comprovante
                    │           → Confirmar → MovimentacaoFinanceira → Titulo (PAGO/PARCIAL)
                    │                          → LancamentoFinanceiro (PAGO/PAGO_PARCIAL)
                    ├── Sangria / Suprimento
                    └── Fechar caixa → Conferência → Relatório

LancamentoFinanceiro (detalhe, tipoCobranca=PAGO_NO_ATO)          ← entrada via atalho
  └── Painel "Caixa" (sempre visível)
        ├── Sem recebimento → botão "Receber no Caixa"
        │     ├── Sem sessão ativa → fluxo abertura de caixa → modal de recebimento
        │     └── Com sessão ativa → modal de recebimento (lançamento pré-carregado)
        ├── Com recebimento, sessão ABERTO → dados do recebimento (editável/cancelável)
        └── Com recebimento, sessão FECHADO → dados do recebimento (somente leitura — imutável)
```

---

## Pontos de Atenção

- **`usuario_caixa` sem FK de schema**: `usuario_id` é soft reference (usuário está no schema público).
- **Upload PIX**: definir estratégia de storage (filesystem local ou S3). Por ora, filesystem é suficiente.
- **Troco**: apenas informativo — o sistema não "guarda" o troco, apenas exibe para o operador.
- **Sessão aberta por outro usuário**: decidir se qualquer usuário com permissão pode fechar a sessão de outro (recomendado: sim, com registro de quem fechou).
- **Atalho do lançamento**: usa Angular Router `state` (não query param) para não expor o ID na URL e não quebrar em reload; o módulo de caixa testa `history.state.lancamentoId` no `ngOnInit`.
- **FATURADO ≠ elegível para caixa**: lançamentos de convênios faturados têm `statusFinanceiro=FATURADO` mas `tipoCobranca=FATURADO` — serão pagos pelo plano de saúde, não pelo caixa. O filtro de elegibilidade usa `tipoCobranca=PAGO_NO_ATO`.
- **`tipoCobranca` snapshot obrigatório**: sem ele não é possível distinguir os dois casos de `FATURADO` na listagem do caixa. Deve ser preenchido em `AtendimentoServiceImpl` junto com `setorId` e `unidadeNegocioId`.
- **Imutabilidade implementada no backend**: `MovimentacaoCaixaServiceImpl.registrarRecebimento()` deve validar que `aberturaCaixa.status == ABERTO` antes de qualquer operação. O frontend reforça a restrição visualmente, mas a fonte da verdade é o backend.
- **Múltiplos lançamentos no mesmo recebimento**: a ETAPA 5 suporta receber um de cada vez; receber N de uma vez é uma melhoria futura.
- **Lançamento com múltiplos títulos**: não existe atualmente (1 lançamento = 1 título), mas é bom validar isso.

---

## Status

**Data de criação:** 2026-05-02
**Status:** Aguardando implementação (Etapa 1 não iniciada).
