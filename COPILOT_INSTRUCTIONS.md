# Copilot/Gemini - Instruções do Projeto

Propósito: centralizar regras e contexto compartilhado para agentes (Copilot / Gemini) e referenciar guias de domínio já existentes.

Arquivos de domínio (existentes):
- src/backend/GEMINI.md  -> Contexto e comandos específicos do backend (Java / Spring).
- src/frontend/GEMINI.md -> Contexto e comandos específicos do frontend (Angular / TypeScript).
- .github/instructions/PROJECT.md -> Visão geral do projeto (contexto centralizado)

Esses arquivos são complementares e não conflitam: mantenha-os como base por domínio.

Recomendações mínimas:
- Manter este arquivo como fonte global de políticas comuns (estilo de commit, testes obrigatórios, CI, regras de lint).
- Configurar a ferramenta para carregar ambos os diretórios de instruções como contexto:
  - Ex.: export COPILOT_CUSTOM_INSTRUCTIONS_DIRS="src/backend,src/frontend,.github/instructions"
- Opcional: adicionar referência a este arquivo no README e no pipeline de CI para validação automática de sugestões.

Como contribuir:
- Atualize os arquivos GEMINI.md locais com detalhes específicos do domínio.
- Atualize este arquivo com regras transversais (commit message, políticas de PR, testes de aceitação).

Resultado esperado: agentes terão contexto específico por domínio e um conjunto consistente de regras globais para interações mais assertivas.
