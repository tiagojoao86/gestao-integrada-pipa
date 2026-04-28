# Módulo de Agendamento — Plano de Implementação

> **Como usar este arquivo:**
> Ao iniciar uma sessão, leia este arquivo para saber de onde parar.
> Ao concluir uma tarefa, marque com `[x]` e registre observações se necessário.
> Ao abrir uma issue ou bug durante a implementação, adicione em "Pendências e Notas".

---

## Status Geral

| Entrega | Descrição | Status |
|---------|-----------|--------|
| Entrega 1 | Cadastro de Agenda (CRUD) | ✅ Concluído |
| Entrega 2 | Regras de Agenda | ✅ Concluído |
| Entrega 3 | Motor de Slots + Agendamento | ✅ Concluído |
| Entrega 4 | Visualizações | ✅ Concluído |

---

## Modelo de Dados

```
Agenda
├── profissional    (FK → Profissional)
├── setor           (FK → Setor)
└── nome            (VO Nome)

AgendaRegra  (1 agenda → N regras)
├── agenda                   (FK)
├── dataInicio / dataFim     (LocalDate — range de datas)
├── horaInicio / horaFim     (LocalTime — range de horário)
├── duracaoSessaoMinutos     (Integer)
├── diasSemana               (ElementCollection: SEG|TER|QUA|QUI|SEX|SAB|DOM) — opcional
├── convenios                (ManyToMany → Convenio) — opcional
└── procedimentos            (ManyToMany → Procedimento) — opcional

Agendamento  (1 agenda → N agendamentos)
├── agenda       (FK)
├── paciente     (FK → Pessoa)
├── convenio     (FK → Convenio) — opcional
├── procedimento (FK → Procedimento) — opcional
├── observacao   (String)
└── status       (Enum: AGENDADO | CANCELADO | REALIZADO)

AgendamentoHorario  (1 agendamento → N horários)
├── agendamento      (FK)
├── dataHoraInicio   (LocalDateTime)
└── dataHoraFim      (LocalDateTime)
```

**Packages backend:**
```
atendimento/
  agendamento/
    agenda/          → Agenda
    agendaregra/     → AgendaRegra
    agendamento/     → Agendamento + AgendamentoHorario
```

**Packages frontend:**
```
components/atendimento/
  agendamento/
    agenda/          → grid + detalhe (com sub-painel de regras)
    agendaregra/     → sub-componente dentro do detalhe da agenda
    agendamento/     → calendário (visão profissional 1D/7D/30D + visão paciente)
                       agendar/  → formulário de criação/edição de agendamento
```

---

## Entrega 1 — Cadastro de Agenda

**Objetivo:** CRUD básico de agendas (profissional + setor + nome).

### Backend
- [x] Entidade `Agenda` com Builder + ValidatedData
  - Campos: `nome` (VO Nome), `profissional` (ManyToOne), `setor` (ManyToOne), `ativo` (boolean)
- [x] Migration `V20260424000001__create_agenda.sql`
  - Tabela `agenda` com FK para `profissional` e `setor`
  - Índice em `deleted`
  - INSERT de módulo `AGENDAMENTO_AGENDA` no grupo `AGENDAMENTO`
  - Grant de permissões para perfil Administrador Geral
- [x] `AgendaDTO` + `AgendaGridDTO` (Lombok `@Builder @Data`)
- [x] `AgendaRepository` (JpaRepository + JpaSpecificationExecutor)
- [x] `AgendaService` interface + `AgendaServiceImpl` extends `CrudServiceImpl`
- [x] `AgendaController` extends `BaseController` com todos os `@PreAuthorize`
- [x] Adicionar pacote em `DataSourceConfig.ENTITY_PACKAGES`

### Frontend
- [x] `AgendaDTO` e `AgendaGridDTO` (classes com `@Exclude/@Expose`)
- [x] `AgendaService` extends `BaseService`
- [x] `agenda.component.ts` (principal/roteador)
- [x] `agenda-grid.component` (listagem com AuditInfo + confirmação de exclusão)
- [x] `agenda-detalhe.component` (formulário: nome, autocomplete profissional, autocomplete setor)
- [x] `SystemModuleKey.AGENDAMENTO_AGENDA` no enum
- [x] Rota `/atendimento/agendamento/agenda` no router
- [x] Entrada no menu dentro do grupo "Atendimento"
- [x] i18n: strings em `messages.xlf` e `messages.en.xlf`

**Critério de aceite:** Criar, editar, listar e inativar agendas. ✅

---

## Entrega 2 — Regras de Agenda

**Objetivo:** Configurar regras de horário dentro de uma agenda, com alerta de conflito.

### Backend
- [x] Enum `DiaSemana` (SEG, TER, QUA, QUI, SEX, SAB, DOM)
- [x] Entidade `AgendaRegra` com Builder + ValidatedData
  - Campos: `agenda` (ManyToOne), `dataInicio`, `dataFim`, `horaInicio`, `horaFim`,
    `duracaoSessaoMinutos`, `diasSemana` (ElementCollection), `convenios` (ManyToMany),
    `procedimentos` (ManyToMany)
- [x] Migration `V20260424000002__create_agenda_regra.sql`
  - Tabelas: `agenda_regra`, `agenda_regra_dia_semana`, `agenda_regra_convenio`, `agenda_regra_procedimento`
  - INSERT de módulo `AGENDAMENTO_AGENDA_REGRA`
- [x] `AgendaRegraDTO` + `AgendaRegraGridDTO`
- [x] `AgendaRegraRepository` + `AgendaRegraService` + `AgendaRegraServiceImpl`
- [x] `AgendaRegraController` com endpoints aninhados sob `/agendamento/agenda/{agendaId}/regras`
- [x] `ConflitosRegraService` — lógica de detecção de conflito entre regras de uma mesma agenda
- [x] Endpoint `GET /agendamento/agenda/{id}/regras/conflitos`

### Frontend
- [x] Sub-painel `AgendaRegraComponent` dentro do detalhe da Agenda (carregado por ID)
- [x] Formulário de regra (date range, time range, duração, dias da semana, multiselect convênios/procedimentos)
- [x] Lista de regras configuradas com botão editar/excluir
- [x] Banner/card de alerta exibindo pares de regras em conflito (não bloqueante)

**Critério de aceite:** Adicionar regras, editar, remover. Ver alertas quando há sobreposição de horários. ✅

---

## Entrega 3 — Motor de Slots + Agendamento por Profissional

**Objetivo:** Calcular slots disponíveis e agendar pacientes com suporte a múltiplos horários.

### Backend
- [x] Entidades `Agendamento` + `AgendamentoHorario` + migration `V20260426000001__create_agendamento.sql`
  - `AgendamentoStatus` enum (AGENDADO, CANCELADO, REALIZADO)
  - Tabela `agendamento_horario` com FK para `agendamento`
- [x] `SlotCalculatorService` — dado uma agenda e range de data, gera todos os slots conforme regras,
  excluindo slots já ocupados (presentes em `agendamento_horario`)
- [x] Endpoint `GET /agendamento/agendamento/slots?agendaId=&dataInicio=&dataFim=`
  - Retorna lista de `SlotDTO { dataHoraInicio, dataHoraFim, livre, agendamentoId?, pacienteNome?, ... }`
- [x] Endpoint `POST /agendamento/agendamento` — cria agendamento com lista de `dataHoraInicio/dataHoraFim`
- [x] Endpoint `GET /agendamento/agendamento/conflito-paciente?pacienteId=&dataInicio=&dataFim=`
- [x] Endpoints `PATCH /agendamento/agendamento/cancelar/{id}` e `realizar/{id}`

### Frontend
- [x] Tela `AgendarComponent` (formulário completo):
  - Seleção de agenda, paciente, convênio, procedimento (via EntityField)
  - Busca de slots por período → grade de slots com toggle de seleção
  - Alerta de conflito ao selecionar paciente + slots
  - Modo calendário: pré-preenche agenda e slot a partir do slot clicado
  - Modo somente leitura para datas passadas

**Critério de aceite:** Agendar paciente em um ou mais slots. Ver alerta de conflito. ✅

---

## Entrega 4 — Visualizações

**Objetivo:** Duas visões de consulta dos agendamentos.

> **Nota de implementação:** As visualizações foram integradas diretamente no
> `AgendamentoComponent` (calendário), eliminando a necessidade de um componente
> `visualizacao/` separado. O endpoint `/slots` serve como `visao-profissional`.

### Backend
- [x] Endpoint `GET /agendamento/agendamento/slots` (serve visão profissional — livres + ocupados com dados do paciente)
- [x] Endpoint `GET /agendamento/agendamento/visao-paciente?pessoaId=&dataInicio=&dataFim=`
  - Retorna agendamentos do paciente ordenados cronologicamente

### Frontend
- [x] **Visão por Profissional:**
  - Modo 1D: grade de slots do dia com status livre/ocupado, nome do paciente, procedimento/convênio
  - Modo 7D: grade semanal compacta (7 colunas)
  - Modo 30D: calendário mensal com contadores de livres/ocupados por célula
  - Clicar em slot livre → abre `AgendarComponent` para novo agendamento
  - Clicar em slot ocupado → abre `AgendarComponent` em modo visualização
- [x] **Visão por Paciente:**
  - Busca por paciente (EntityField) + filtro de período
  - Lista cronológica agrupada por data: hora, agenda, profissional, procedimento, convênio, status
  - Botão cancelar diretamente na lista (apenas para status AGENDADO)
- [x] Switcher Profissional/Paciente na barra de controles

### Extras implementados além do plano
- [x] Filtro de agendas por unidade de negócio via setor (`AgendaServiceImpl.addUnidadeNegocioFilterIfApplicable`)
- [x] Testes unitários: `SlotCalculatorServiceTest` (11 casos), `AgendamentoServiceTest` (8 casos), `AgendarComponent` (15 casos)

**Critério de aceite:** Navegar pelas duas visões com dados reais. Cancelar agendamento pela visão do paciente. ✅

---

## Módulo Concluído ✅

Commits principais:
- `328c7e2` — Entrega 1 + 2 (agenda, regras, conflitos)
- `a2b0466` — Entrega 3 + 4 (motor de slots, agendamento, visualizações)
- `e6ba20a` — Testes, lint, filtro por setor
- `37d94ec` — Fix budget CSS para build de produção

---

## Referências

- Regras do projeto: `CLAUDE.md`
- Template de nova entidade: `ENTITY_CREATE_PROMPT.md`
- Arquitetura multi-tenant: `MULTI-TENANT-ARCHITECTURE.md`
- Requisitos originais: `modulo-agendamento-requisitos.txt` (fora do repositório)
