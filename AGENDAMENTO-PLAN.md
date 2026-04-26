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
| Entrega 3 | Motor de Slots + Agendamento | ⬜ Não iniciado |
| Entrega 4 | Visualizações | ⬜ Não iniciado |

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
    agenda/          → grid + detalhe
    agendaregra/     → sub-componente dentro do detalhe da agenda
    agendamento/     → tela de agendamento (modos: profissional / paciente)
    visualizacao/    → visão por profissional e por paciente
```

---

## Entrega 1 — Cadastro de Agenda

**Objetivo:** CRUD básico de agendas (profissional + setor + nome).

### Backend
- [ ] Entidade `Agenda` com Builder + ValidatedData
  - Campos: `nome` (VO Nome), `profissional` (ManyToOne), `setor` (ManyToOne), `ativo` (boolean)
- [ ] Migration `V20260423000001__create_agenda.sql`
  - Tabela `agenda` com FK para `profissional` e `setor`
  - Índice em `deleted`
  - INSERT de módulo `AGENDAMENTO_AGENDA` no grupo `AGENDAMENTO`
  - Grant de `pode_listar`, `pode_inserir`, `pode_editar`, `pode_excluir`, `pode_auditar` para perfil Administrador Geral (`019a7fc4-ab0c-7002-8944-8e0ef009139b`)
- [ ] `AgendaDTO` + `AgendaGridDTO` (Lombok `@Builder @Data`)
- [ ] `AgendaRepository` (JpaRepository + JpaSpecificationExecutor)
- [ ] `AgendaService` interface + `AgendaServiceImpl` extends `CrudServiceImpl`
- [ ] `AgendaController` extends `BaseController` com todos os `@PreAuthorize`
- [ ] Adicionar pacote em `DataSourceConfig.ENTITY_PACKAGES`

### Frontend
- [ ] `AgendaDTO` e `AgendaGridDTO` (classes com `@Exclude/@Expose`)
- [ ] `AgendaService` extends `BaseService`
- [ ] `agenda.component.ts` (principal/roteador)
- [ ] `agenda-grid.component` (listagem com AuditInfo + confirmação de exclusão)
- [ ] `agenda-detalhe.component` (formulário: nome, autocomplete profissional, autocomplete setor)
- [ ] `SystemModuleKey.AGENDAMENTO_AGENDA` no enum
- [ ] Rota `/atendimento/agendamento/agenda` no router
- [ ] Entrada no menu dentro do grupo "Atendimento"
- [ ] i18n: strings em `messages.xlf` e `messages.en.xlf`

**Critério de aceite:** Criar, editar, listar e inativar agendas.

---

## Entrega 2 — Regras de Agenda

**Objetivo:** Configurar regras de horário dentro de uma agenda, com alerta de conflito.

### Backend
- [ ] Enum `DiaSemana` (SEG, TER, QUA, QUI, SEX, SAB, DOM)
- [ ] Entidade `AgendaRegra` com Builder + ValidatedData
  - Campos: `agenda` (ManyToOne), `dataInicio`, `dataFim`, `horaInicio`, `horaFim`,
    `duracaoSessaoMinutos`, `diasSemana` (ElementCollection), `convenios` (ManyToMany),
    `procedimentos` (ManyToMany)
- [ ] Migration `V20260423000002__create_agenda_regra.sql`
  - Tabelas: `agenda_regra`, `agenda_regra_dia_semana`, `agenda_regra_convenio`, `agenda_regra_procedimento`
  - INSERT de módulo `AGENDAMENTO_AGENDA_REGRA`
- [ ] `AgendaRegraDTO` + `AgendaRegraGridDTO`
- [ ] `AgendaRegraRepository` + `AgendaRegraService` + `AgendaRegraServiceImpl`
- [ ] `AgendaRegraController` com endpoints aninhados sob `/agendamento/agenda/{agendaId}/regras`
- [ ] `ConflitosRegraService` — lógica de detecção de conflito entre regras de uma mesma agenda:
  - Duas regras conflitam se ranges de data E de horário se sobrepõem E dias da semana se intersectam
  - Retorna lista de pares `[regraIdA, regraIdB]` como warning (não bloqueia)
- [ ] Endpoint `GET /agendamento/agenda/{id}/regras/conflitos`

### Frontend
- [ ] Sub-painel `AgendaRegraComponent` dentro do detalhe da Agenda (carregado por ID)
- [ ] Formulário de regra:
  - Date range (dataInicio/dataFim)
  - Time range (horaInicio/horaFim)
  - Duração da sessão (InputNumber em minutos)
  - Checkboxes dos dias da semana
  - MultiSelect de convênios (opcional)
  - MultiSelect de procedimentos (opcional)
- [ ] Lista de regras configuradas com botão editar/excluir
- [ ] Banner/card de alerta exibindo pares de regras em conflito (não bloqueante)

**Critério de aceite:** Adicionar regras, editar, remover. Ver alertas quando há sobreposição de horários.

---

## Entrega 3 — Motor de Slots + Agendamento por Profissional

**Objetivo:** Calcular slots disponíveis e agendar pacientes com suporte a múltiplos horários.

### Backend
- [ ] Entidades `Agendamento` + `AgendamentoHorario` + migration `V20260423000003__create_agendamento.sql`
  - `AgendamentoStatus` enum (AGENDADO, CANCELADO, REALIZADO)
  - Tabela `agendamento_horario` com FK para `agendamento`
- [ ] `SlotCalculatorService` — dado uma agenda e range de data, gera todos os slots conforme regras,
  excluindo slots já ocupados (presentes em `agendamento_horario`)
- [ ] Endpoint `GET /agendamento/slots?agendaId=&dataInicio=&dataFim=`
  - Retorna lista de `SlotDTO { dataHoraInicio, dataHoraFim, livre: boolean, agendamentoId? }`
- [ ] Endpoint `POST /agendamento` — cria agendamento com lista de `dataHoraInicio/dataHoraFim`
- [ ] Endpoint `GET /agendamento/conflito-paciente?pessoaId=&dataHoraInicio=&dataHoraFim=`
  - Retorna lista de agendamentos do paciente que colidem com o horário solicitado

### Frontend
- [ ] Tela "Agendar por Profissional":
  1. Seleciona Agenda (autocomplete)
  2. Seleciona período/data → carrega grade de slots via endpoint
  3. Seleciona um ou mais slots livres (highlight visual)
  4. Seleciona Paciente (autocomplete de Pessoa)
  5. Seleciona Convênio + Procedimento (opcionais)
  6. Ao selecionar paciente + slots, consulta `conflito-paciente` → alerta não-bloqueante
  7. Confirma agendamento
- [ ] Tela "Agendar por Paciente":
  1. Seleciona Paciente
  2. Seleciona Agenda/Profissional
  3. Fluxo similar ao modo por profissional

**Critério de aceite:** Agendar paciente em um ou mais slots. Ver alerta de conflito. Não conseguir agendar em slot ocupado.

---

## Entrega 4 — Visualizações

**Objetivo:** Duas visões de consulta dos agendamentos.

### Backend
- [ ] Endpoint `GET /agendamento/visao-profissional?agendaId=&dataInicio=&dataFim=`
  - Retorna todos os slots (livres e ocupados) com dados do paciente quando ocupado
- [ ] Endpoint `GET /agendamento/visao-paciente?pessoaId=&dataInicio=&dataFim=`
  - Retorna todos os agendamentos do paciente agrupados por profissional/agenda

### Frontend
- [ ] **Visão por Profissional:** grade horária semanal/diária. Slot livre = cinza, ocupado = cor + nome do paciente. Clicar no slot ocupado abre resumo do agendamento.
- [ ] **Visão por Paciente:** busca paciente → lista cronológica: data, profissional, procedimento, convênio, status. Ação de cancelar diretamente na lista.
- [ ] Switcher para alternar entre as duas visões na mesma tela

**Critério de aceite:** Navegar pelas duas visões com dados reais. Cancelar agendamento pela visão do paciente.

---

## Pendências e Notas

*(Adicionar aqui qualquer issue, decisão de design ou bloqueio encontrado durante a implementação)*

---

## Referências

- Regras do projeto: `CLAUDE.md`
- Template de nova entidade: `ENTITY_CREATE_PROMPT.md`
- Arquitetura multi-tenant: `MULTI-TENANT-ARCHITECTURE.md`
- Requisitos originais: `modulo-agendamento-requisitos.txt` (fora do repositório)
