# Gemini - Frontend

> **Nota:** Eu sou um assistente de IA. Se eu identificar informações importantes que possam ser adicionadas a este arquivo para melhorar nossas interações futuras (como novos comandos, convenções ou detalhes de arquitetura), irei sugerir atualizações. Sinta-se à vontade para me perguntar como melhorá-lo.

## Resumo do Projeto

Este projeto contém a interface de usuário (UI) para o sistema Gestão Integrada. É uma Single Page Application (SPA) construída para interagir com a API do backend.

## Stack Tecnológica

- TypeScript
- Angular
- Angular CLI
- CSS

## Comandos Essenciais

**Nota:** Execute os comandos a partir do diretório `src/frontend`.

- **Instalar Dependências:**
  ```bash
  npm install
  ```

- **Executar a Aplicação (desenvolvimento):
  ```bash
  ng serve
  ```

- **Executar os Testes:**
  ```bash
  ng test
  ```

- **Verificação de Estilo (Lint):**
  ```bash
  ng lint
  ```

- **Gerar o Build de Produção:**
  ```bash
  ng build
  ```

## Linting e Regras (ESLint)

Para garantir a qualidade e consistência do código, o projeto utiliza ESLint. As configurações estão em `eslint.config.js`.

### Comandos Essenciais de Linting

- **Executar o Lint (apenas verificar erros):**
  ```bash
  npm run lint
  ```

- **Executar o Lint e tentar corrigir automaticamente os problemas (incluindo remoção de imports não utilizados):**
  ```bash
  npm run lint -- --fix
  ```

### Regras Específicas

As seguintes regras foram configuradas para padronizar o desenvolvimento:

-   **Remoção de Imports Não Utilizados:**
    -   Utiliza o plugin `eslint-plugin-unused-imports`.
    -   `"unused-imports/no-unused-imports": "error"`: Identifica imports desnecessários.
    -   `"unused-imports/no-unused-vars"`: Configura o tratamento de variáveis não utilizadas (atualmente como `warn`).
    -   As regras padrão `no-unused-vars` do ESLint e `@typescript-eslint/no-unused-vars` foram desativadas para evitar conflitos e permitir que este plugin gerencie a remoção com `--fix`.

-   **Seletores de Angular (prefixos e estilo):**
    -   `@angular-eslint/directive-selector`:
        -   `type: "attribute"`
        -   `prefix: "gi"`: Diretivas devem usar o prefixo `gi` (ex: `[giMinhaDiretiva]`).
        -   `style: "camelCase"`: O estilo do seletor da diretiva deve ser `camelCase`.
    -   `@angular-eslint/component-selector`:
        -   `type: "element"`
        -   `prefix: "gi"`: Componentes devem usar o prefixo `gi` (ex: `<gi-meu-componente>`).
        -   `style: "kebab-case"`: O estilo do seletor do componente deve ser `kebab-case`.

## Arquitetura e Convenções

- A arquitetura é baseada em componentes, seguindo as melhores práticas do Angular.
- **Componentes Reutilizáveis:** Ficam em `src/app/components/base`.
- **Componentes de Tela (Features):** Organizados por funcionalidade, como em `src/app/components/cadastro`.
- **Serviços e Modelos de Dados:** Para manter a coesão, os serviços e modelos (DTOs) específicos de uma funcionalidade estão localizados dentro do diretório do seu respectivo componente. Por exemplo, `usuario.service.ts` e os DTOs de usuário estão em `src/app/components/cadastro/usuario/`.
  - **Extração de Interfaces:** Interfaces (como DTOs ou modelos de formulário) não devem ser definidas dentro de arquivos de componente (`.component.ts`). Elas devem ser extraídas para seus próprios arquivos na pasta `model` (ex: `permissao-form-group-value.dto.ts`) para promover a reutilização e a organização do código.
- **DTOs de Backend:** Quando a estrutura de um payload retornado por uma API do backend é diferente do DTO principal utilizado pelo componente, deve-se criar um DTO específico para representar esse payload. Este DTO deve seguir a convenção de nomenclatura `backend-*.dto.ts`. Um exemplo é o `backend-permissao-dto.ts`, que representa a estrutura de permissões como enviada pela API, facilitando a conversão para o `FormGroup` dentro do componente.
- **Serviços e Modelos Compartilhados:** Lógica e modelos que são compartilhados por toda a aplicação ou por múltiplos componentes base ficam em `src/app/components/base/`, dentro de subdiretórios apropriados (ex: `auth`, `model`).

## Princípios de Desenvolvimento

### Componentização
- **Sempre que possível, componentize** funcionalidades reutilizáveis.
- Identifique padrões que se repetem e extraia-os em componentes separados.
- Componentes devem ter uma responsabilidade bem definida e única.
- Prefira criar componentes menores e especializados a componentes grandes e genéricos.
- Use `@Input()` e `@Output()` para comunicação entre componentes.

### Baixo Acoplamento
- **Priorize a criação de componentes com baixo acoplamento**.
- Componentes não devem depender diretamente de outros componentes específicos.
- Use serviços para compartilhar estado e lógica entre componentes.
- Evite acessar diretamente o DOM de componentes filhos; use `@ViewChild` apenas quando necessário.
- Prefira comunicação por eventos (`@Output`) ao invés de chamadas diretas entre componentes.
- Mantenha a lógica de negócio nos serviços, não nos componentes.
- Componentes devem ser testáveis isoladamente com mocks/stubs de suas dependências.

### Comentários no Código
- Adicione comentários no código de forma esparsa. Concentre-se no *porquê* algo é feito, especialmente para lógicas complexas, em vez de *o quê* é feito. Apenas adicione comentários de alto valor se necessário para clareza ou se solicitado pelo usuário.

## Internacionalização (i18n)

O projeto suporta múltiplos idiomas (português e inglês) usando as ferramentas de internacionalização do Angular.

### Processo de Tradução

Ao adicionar ou modificar qualquer texto que será visível para o usuário, siga estes passos:

1.  **Marcar a String:** No código TypeScript ou no template HTML, marque a nova string para tradução usando a tag `$localize`.
    ```typescript
    // Em um arquivo .ts
    const titulo = $localize`Meu Título`;
    
    // Em um arquivo .html
    <h1 i18n>Meu Título no Template</h1>
    ```

2.  **Extrair as Strings:** Execute o comando `ng extract-i18n` para escanear o projeto e adicionar as novas strings ao arquivo de origem principal.
    ```bash
    # Executar a partir do diretório src/frontend
    ng extract-i18n --output-path src/locale
    ```

3.  **Atualizar Arquivos de Tradução:**
    -   **Português (`messages.xlf`):** A extração adiciona um novo bloco `<trans-unit>` com a tag `<source>`. Para validar a tradução, adicione uma tag `<target>` com o mesmo conteúdo da tag `<source>`.
    -   **Inglês (`messages.en.xlf`):** Copie o novo bloco `<trans-unit>` do `messages.xlf` e cole-o no `messages.en.xlf`. Em seguida, preencha a tag `<target>` com a tradução correta em inglês.

## Autorização e Proteção de Rotas

- **Autenticação:** Todas as rotas que exigem que o usuário esteja logado devem ser protegidas pelo `authGuard`. Ele valida se existe um token de autenticação ativo.
- **Autorização por Módulo:** Para controlar o acesso a funcionalidades específicas (como telas de cadastro), utilizamos o `moduleAuthorityGuard`.
  - Este guarda (`guard`) é configurado na definição da rota e recebe a `key` do módulo como um dado (`data`).
  - Exemplo de como proteger uma rota e exigir a permissão de "listar" para o módulo `CADASTRO_USUARIO`:
    ```typescript
    {
      path: 'usuario',
      loadComponent: () => import('./usuario/usuario.component').then(m => m.UsuarioComponent),
      canActivate: [authGuard, moduleAuthorityGuard],
      data: {
        module: 'CADASTRO_USUARIO'
      }
    }
    ```
  - O `moduleAuthorityGuard` utiliza o `AuthService` para verificar se o usuário logado possui a permissão necessária (`LISTAR`) para o módulo especificado.

- **Autorização por Grupo:** Para controlar o acesso a seções maiores da aplicação (como um grupo de cadastros), utilizamos o `groupAuthorityGuard`.
  - Este guarda (`guard`) é configurado na rota principal da seção e recebe o nome do grupo como um dado (`data`).
  - Exemplo de como proteger uma rota e exigir que o usuário pertença ao grupo `CADASTROS`:
    ```typescript
    {
      path: 'cadastros',
      loadComponent: () => import('./cadastro/cadastro.component').then(m => m.CadastroComponent),
      canActivate: [authGuard, groupAuthorityGuard],
      data: {
        group: 'CADASTROS'
      }
    }
    ```
  - O `groupAuthorityGuard` utiliza o `AuthService` para verificar se o usuário logado possui a permissão para o grupo especificado.

## Estrutura de Diretórios

- `src/app`: Código-fonte da aplicação (componentes, serviços, rotas).
- `src/assets`: Imagens, ícones e outros arquivos estáticos.
- `src/styles.css`: Estilos globais da aplicação.
- `angular.json`: Arquivo de configuração do Angular CLI.

## Como Você Pode Me Ajudar

- **Corrigindo meu curso:** Se você notar que estou tentando navegar para uma rota que não existe, ou usando um nome de arquivo incorreto, por favor, me corrija. Por exemplo, se eu usar `/principal/home` quando a rota correta é `/`, me avise. Isso me ajuda a aprender a estrutura do seu projeto mais rapidamente.
