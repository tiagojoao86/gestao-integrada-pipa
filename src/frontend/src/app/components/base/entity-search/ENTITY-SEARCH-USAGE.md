# Entity Search Component - Guia de Uso

## Descrição

O componente Entity Search fornece uma forma padronizada e reutilizável de pesquisar e selecionar entidades na aplicação. Ele é genérico e pode ser usado com qualquer entidade que tenha um serviço que estende `BaseService`.

O componente é **instanciado dinamicamente** pelo service (similar ao MatDialog), evitando o uso de `any` e permitindo **tipagem completa com generics**.

## Características

- ✅ **Totalmente tipado** - Usa generics TypeScript para type safety completo
- ✅ **Instanciação dinâmica** - Componente criado via service, não precisa estar no template
- ✅ **Zero any** - Tipagem forte em todos os pontos
- ✅ Modal com overlay transparente bloqueando background
- ✅ Padrão Observable do RxJS para gerenciar resultados
- ✅ Campos de busca configuráveis (dropdown)
- ✅ Campos de resultado configuráveis (colunas da tabela)
- ✅ Paginação integrada
- ✅ Filtros usando `FilterDTO` (operador 'like')
- ✅ Responsivo
- ✅ Acessível via service de qualquer componente

## Como Usar

### 1. Importar o EntitySearchService

```typescript
import { Component, inject } from '@angular/core';
import { EntitySearchService } from '../base/entity-search/entity-search.service';
import { EntitySearchConfig, SearchField, ResultField } from '../base/entity-search/entity-search.model';
import { PessoaService } from './pessoa.service';
import { PessoaDTO } from './pessoa.dto';

@Component({
    selector: 'app-meu-componente',
    // ...
})
export class MeuComponente {
    private entitySearchService = inject(EntitySearchService);
    private pessoaService = inject(PessoaService);
}
```

> **Nota**: O componente Entity Search é instanciado dinamicamente pelo service. Você **não precisa** adicioná-lo ao template ou aos imports do seu componente.

### 2. Exemplo: Pesquisar e Selecionar uma Pessoa

```typescript
pesquisarPessoa(): void {
    // Define os campos que podem ser pesquisados
    const searchFields: SearchField[] = [
        { key: 'nome', label: 'Nome' },
        { key: 'cpf', label: 'CPF' },
        { key: 'email', label: 'E-mail' }
    ];

    // Define os campos que serão exibidos nos resultados
    const resultFields: ResultField[] = [
        { key: 'nome', label: 'Nome' },
        { key: 'cpf', label: 'CPF' },
        { key: 'email', label: 'E-mail' },
        { key: 'telefone', label: 'Telefone' }
    ];

    // Configura a busca
    const config: EntitySearchConfig<PessoaDTO> = {
        service: this.pessoaService,
        searchFields: searchFields,
        resultFields: resultFields,
        title: 'Pesquisar Pessoa',
        searchPlaceholder: 'Digite o valor para pesquisar...',
        pageSize: 10
    };

    // Abre a modal e aguarda a seleção
    this.entitySearchService.search(config).subscribe(result => {
        if (!result.cancelled && result.entity) {
            console.log('Pessoa selecionada:', result.entity);
            // Faça algo com a pessoa selecionada
            this.pessoaSelecionada = result.entity;
        } else {
            console.log('Busca cancelada pelo usuário');
        }
    });
}
```

### 3. Exemplo: Pesquisar Centro de Custo

```typescript
pesquisarCentroCusto(): void {
    const searchFields: SearchField[] = [
        { key: 'codigo', label: 'Código' },
        { key: 'descricao', label: 'Descrição' }
    ];

    const resultFields: ResultField[] = [
        { key: 'codigo', label: 'Código' },
        { key: 'descricao', label: 'Descrição' },
        { key: 'ativo', label: 'Ativo' }
    ];

    const config: EntitySearchConfig<CentroCustoDTO> = {
        service: this.centroCustoService,
        searchFields: searchFields,
        resultFields: resultFields,
        title: 'Pesquisar Centro de Custo',
        pageSize: 15
    };

    this.entitySearchService.search(config).subscribe(result => {
        if (!result.cancelled && result.entity) {
            this.centroCustoSelecionado = result.entity;
            // Atualiza o formulário com o centro de custo selecionado
            this.form.patchValue({
                centroCusto: result.entity
            });
        }
    });
}
```

### 4. Exemplo: Campos Aninhados

O componente suporta acesso a propriedades aninhadas usando notação de ponto:

```typescript
pesquisarUsuario(): void {
    const searchFields: SearchField[] = [
        { key: 'nome', label: 'Nome' },
        { key: 'login', label: 'Login' },
        { key: 'pessoa.email', label: 'E-mail da Pessoa' }  // Campo aninhado
    ];

    const resultFields: ResultField[] = [
        { key: 'nome', label: 'Nome' },
        { key: 'login', label: 'Login' },
        { key: 'pessoa.nome', label: 'Pessoa' },            // Campo aninhado
        { key: 'pessoa.cpf', label: 'CPF' },                // Campo aninhado
        { key: 'ativo', label: 'Ativo' }
    ];

    const config: EntitySearchConfig<UsuarioDTO> = {
        service: this.usuarioService,
        searchFields: searchFields,
        resultFields: resultFields,
        title: 'Pesquisar Usuário'
    };

    this.entitySearchService.search(config).subscribe(result => {
        if (!result.cancelled && result.entity) {
            this.usuarioSelecionado = result.entity;
        }
    });
}
```

### 5. Exemplo: Uso em Template HTML

```html
<!-- Botão para abrir a pesquisa -->
<button type="button" class="btn btn-primary" (click)="pesquisarPessoa()">
    Pesquisar Pessoa
</button>

<!-- Exibir pessoa selecionada -->
@if (pessoaSelecionada) {
    <div class="pessoa-info">
        <h3>Pessoa Selecionada</h3>
        <p><strong>Nome:</strong> {{ pessoaSelecionada.nome }}</p>
        <p><strong>CPF:</strong> {{ pessoaSelecionada.cpf }}</p>
        <p><strong>E-mail:</strong> {{ pessoaSelecionada.email }}</p>
    </div>
}
```

## API do EntitySearchService

### search<T>(config: EntitySearchConfig<T>): Observable<EntitySearchResult<T>>

Abre a modal de busca de entidade.

**Parâmetros:**
- `config: EntitySearchConfig<T>` - Configuração da busca

**Retorna:** `Observable<EntitySearchResult<T>>` - Observable que emite o resultado da busca

## Interfaces

### EntitySearchConfig<T>

Configuração da busca de entidade.

```typescript
interface EntitySearchConfig<T> {
    service: BaseService<T, unknown>;    // Service que será usado para buscar
    searchFields: SearchField[];         // Campos disponíveis para pesquisa
    resultFields: ResultField[];         // Campos exibidos nos resultados
    title?: string;                      // Título da modal (padrão: 'Pesquisar')
    searchPlaceholder?: string;          // Placeholder do input (padrão: 'Digite para pesquisar...')
    pageSize?: number;                   // Tamanho da página (padrão: 10)
}
```

### SearchField

Campo que pode ser usado para pesquisa.

```typescript
interface SearchField {
    key: string;      // Chave da propriedade no DTO
    label: string;    // Label exibida no dropdown
}
```

### ResultField

Campo exibido nos resultados.

```typescript
interface ResultField {
    key: string;      // Chave da propriedade no DTO (suporta notação de ponto)
    label: string;    // Label exibida no cabeçalho da tabela
}
```

### EntitySearchResult<T>

Resultado da busca.

```typescript
interface EntitySearchResult<T> {
    entity: T;           // Entidade selecionada
    cancelled: boolean;  // true se o usuário cancelou
}
```

## Vantagens da Instanciação Dinâmica

### Por que usar Dynamic Component Instantiation?

1. **Type Safety Completa**
   - Sem uso de `any` - tudo é tipado com generics
   - Autocomplete funciona perfeitamente no IDE
   - Erros de tipo são capturados em tempo de compilação

2. **Arquitetura Limpa**
   - Componente não precisa estar no template
   - Não polui o DOM quando não está em uso
   - Similar ao padrão MatDialog do Angular Material

3. **Melhor Performance**
   - Componente só é criado quando necessário
   - Destruído automaticamente após uso
   - Menos overhead de detecção de mudanças

4. **Flexibilidade**
   - Cada instância é completamente independente
   - Pode ter múltiplas buscas diferentes simultâneas (se necessário)
   - Configuração passada diretamente, não via Subject

### Como funciona internamente?

```typescript
// No EntitySearchService
search<T>(config: EntitySearchConfig<T>): Observable<EntitySearchResult<T>> {
    // 1. Cria o componente dinamicamente com o tipo genérico T
    const componentRef = createComponent(EntitySearchComponent<T>, {
        environmentInjector: this.injector
    });

    // 2. Configura a instância com tipagem forte
    componentRef.instance.config = config;  // config é do tipo EntitySearchConfig<T>
    componentRef.instance.isVisible = true;

    // 3. Se inscreve nos eventos tipados
    componentRef.instance.entitySelected.subscribe((entity) => {
        // entity é do tipo T, não any!
        resultSubject.next({ entity: entity as T, cancelled: false });
    });

    // 4. Anexa ao DOM
    this.appRef.attachView(componentRef.hostView);
    document.body.appendChild(componentRef.location.nativeElement);

    // 5. Retorna Observable tipado
    return resultSubject.asObservable();  // Observable<EntitySearchResult<T>>
}
```

## Funcionamento Interno

1. **Abertura da Modal**: Quando você chama `entitySearchService.search(config)`, o componente é criado dinamicamente
2. **Seleção de Campo**: O usuário seleciona um campo de busca no dropdown
3. **Digitação**: O usuário digita o valor a ser pesquisado
4. **Pesquisa**: Ao clicar em "Pesquisar" (ou pressionar Enter), o componente:
   - Cria um `FilterItemDTO` com operador 'like'
   - Cria um `PageRequest` com a página e tamanho
   - Chama o método `list()` do service fornecido
5. **Resultados**: Os resultados são exibidos em uma tabela
6. **Paginação**: Se houver mais de uma página, os controles de paginação são exibidos
7. **Seleção**: O usuário pode:
   - Clicar na linha inteira para selecionar
   - Clicar no botão "Selecionar"
8. **Retorno**: A entidade selecionada é emitida via Observable
9. **Destruição**: O componente é automaticamente destruído e removido do DOM

## Exemplo Completo em um Componente

```typescript
import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { EntitySearchService } from '../base/entity-search/entity-search.service';
import { EntitySearchConfig, SearchField, ResultField } from '../base/entity-search/entity-search.model';
import { TituloService } from './titulo.service';
import { TituloDTO } from './titulo.dto';

@Component({
    selector: 'app-movimentacao-financeira-detalhe',
    templateUrl: './movimentacao-financeira-detalhe.component.html'
})
export class MovimentacaoFinanceiraDetalheComponent {
    private fb = inject(FormBuilder);
    private entitySearchService = inject(EntitySearchService);
    private tituloService = inject(TituloService);

    form: FormGroup;
    tituloSelecionado: TituloDTO | null = null;

    constructor() {
        this.form = this.fb.group({
            titulo: [null],
            valor: [null],
            // ... outros campos
        });
    }

    selecionarTitulo(): void {
        const searchFields: SearchField[] = [
            { key: 'numero', label: 'Número' },
            { key: 'descricao', label: 'Descrição' },
            { key: 'pessoa.nome', label: 'Pessoa' }
        ];

        const resultFields: ResultField[] = [
            { key: 'numero', label: 'Número' },
            { key: 'descricao', label: 'Descrição' },
            { key: 'pessoa.nome', label: 'Pessoa' },
            { key: 'valor', label: 'Valor' },
            { key: 'vencimento', label: 'Vencimento' },
            { key: 'status', label: 'Status' }
        ];

        const config: EntitySearchConfig<TituloDTO> = {
            service: this.tituloService,
            searchFields: searchFields,
            resultFields: resultFields,
            title: 'Pesquisar Título',
            searchPlaceholder: 'Digite para pesquisar títulos...',
            pageSize: 15
        };

        this.entitySearchService.search(config).subscribe(result => {
            if (!result.cancelled && result.entity) {
                this.tituloSelecionado = result.entity;

                // Atualiza o formulário com os dados do título
                this.form.patchValue({
                    titulo: result.entity,
                    valor: result.entity.valor
                });

                console.log('Título selecionado:', result.entity);
            } else {
                console.log('Seleção de título cancelada');
            }
        });
    }

    limparTitulo(): void {
        this.tituloSelecionado = null;
        this.form.patchValue({
            titulo: null,
            valor: null
        });
    }
}
```

Template correspondente:

```html
<form [formGroup]="form">
    <!-- Campo de título com botão de pesquisa -->
    <div class="form-group">
        <label>Título</label>
        <div class="input-group">
            @if (tituloSelecionado) {
                <div class="selected-titulo">
                    <strong>{{ tituloSelecionado.numero }}</strong> - {{ tituloSelecionado.descricao }}
                    <button type="button" class="btn-clear" (click)="limparTitulo()">
                        ✕
                    </button>
                </div>
            } @else {
                <button type="button" class="btn btn-primary" (click)="selecionarTitulo()">
                    Pesquisar Título
                </button>
            }
        </div>
    </div>

    <!-- Outros campos do formulário -->
    <div class="form-group">
        <label>Valor</label>
        <input type="number" formControlName="valor" class="form-control" />
    </div>
</form>
```

## Boas Práticas

1. **Campos Relevantes**: Escolha campos de busca que os usuários realmente usarão
2. **Resultados Informativos**: Exiba informações suficientes para o usuário identificar a entidade correta
3. **Performance**: Use `pageSize` adequado ao contexto (10-15 é um bom padrão)
4. **Títulos Descritivos**: Use títulos claros que indiquem o que está sendo pesquisado
5. **Tratamento de Resultado**: Sempre verifique `cancelled` antes de usar a entidade
6. **Unsubscribe Automático**: O Observable completa automaticamente, não precisa unsubscribe manual

## Observações Técnicas

- O componente usa **operador 'like'** nos filtros (busca parcial)
- Suporta **propriedades aninhadas** usando notação de ponto (ex: 'pessoa.nome')
- O overlay **não fecha** ao clicar fora por padrão (usuário deve selecionar ou cancelar)
- Integra automaticamente com a **paginação** existente do sistema
- Tecla **Enter** no campo de busca dispara a pesquisa
- As linhas da tabela são **clicáveis** para seleção rápida
- Z-index de 9999 garante que fique sobre outros elementos

## Diferenças em Relação ao Dialog

- **Dialog**: Para confirmações e mensagens simples
- **EntitySearch**: Para pesquisar e selecionar entidades complexas

Ambos usam o padrão Observable e overlay transparente, mantendo consistência na UX.
