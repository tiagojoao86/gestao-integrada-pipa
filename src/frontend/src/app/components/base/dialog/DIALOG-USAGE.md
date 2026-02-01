# Dialog Component - Guia de Uso

## Descrição

O componente Dialog fornece uma forma padronizada de exibir diálogos modais na aplicação, com 3 tipos diferentes:
1. **OK** - Apenas confirmação
2. **Sim/Não** - Confirmação com duas opções
3. **Sim/Não/Cancelar** - Confirmação com três opções

## Características

- ✅ Overlay com máscara transparente que bloqueia a interação com o background
- ✅ Padrão Observable do RxJS para gerenciar respostas
- ✅ Customização de labels dos botões
- ✅ Animações suaves de entrada/saída
- ✅ Responsivo
- ✅ Acessível via service de qualquer componente

## Como Usar

### 1. Importar o DialogService

```typescript
import { Component } from '@angular/core';
import { DialogService } from '../base/dialog/dialog.service';
import { DialogResult } from '../base/dialog/dialog.model';

@Component({
    selector: 'app-meu-componente',
    // ...
})
export class MeuComponente {
    constructor(private dialogService: DialogService) {}
}
```

### 2. Exemplo: Diálogo com OK

```typescript
mostrarMensagem(): void {
    this.dialogService.showOk('Sucesso', 'Operação realizada com sucesso!')
        .subscribe(result => {
            console.log('Usuário clicou em OK');
            // Executar ação após o usuário clicar em OK
        });
}
```

### 3. Exemplo: Diálogo Sim/Não

```typescript
confirmarExclusao(): void {
    this.dialogService.showYesNo(
        'Confirmar Exclusão',
        'Deseja realmente excluir este registro?'
    ).subscribe(result => {
        if (result === DialogResult.YES) {
            console.log('Usuário confirmou a exclusão');
            this.excluirRegistro();
        } else {
            console.log('Usuário cancelou a exclusão');
        }
    });
}
```

### 4. Exemplo: Diálogo Sim/Não/Cancelar

```typescript
salvarAlteracoes(): void {
    this.dialogService.showYesNoCancel(
        'Salvar Alterações',
        'Há alterações não salvas. Deseja salvar antes de sair?'
    ).subscribe(result => {
        switch (result) {
            case DialogResult.YES:
                console.log('Salvar e sair');
                this.salvar().then(() => this.sair());
                break;
            case DialogResult.NO:
                console.log('Sair sem salvar');
                this.sair();
                break;
            case DialogResult.CANCEL:
                console.log('Cancelar');
                // Não faz nada
                break;
        }
    });
}
```

### 5. Exemplo: Labels Customizadas

```typescript
confirmarAcao(): void {
    this.dialogService.showYesNo(
        'Confirmar Ação',
        'Deseja continuar com esta operação?',
        'Continuar',  // Label do botão Sim
        'Voltar'      // Label do botão Não
    ).subscribe(result => {
        if (result === DialogResult.YES) {
            console.log('Usuário escolheu Continuar');
        } else {
            console.log('Usuário escolheu Voltar');
        }
    });
}
```

## API do DialogService

### showOk(title, message, okLabel?)

Exibe um diálogo com apenas o botão OK.

**Parâmetros:**
- `title: string` - Título do diálogo
- `message: string` - Mensagem do diálogo
- `okLabel?: string` - Label customizada para o botão (padrão: 'OK')

**Retorna:** `Observable<DialogResult>`

### showYesNo(title, message, yesLabel?, noLabel?)

Exibe um diálogo com botões Sim/Não.

**Parâmetros:**
- `title: string` - Título do diálogo
- `message: string` - Mensagem do diálogo
- `yesLabel?: string` - Label customizada para o botão Sim (padrão: 'Sim')
- `noLabel?: string` - Label customizada para o botão Não (padrão: 'Não')

**Retorna:** `Observable<DialogResult>` - Emite `DialogResult.YES` ou `DialogResult.NO`

### showYesNoCancel(title, message, yesLabel?, noLabel?, cancelLabel?)

Exibe um diálogo com botões Sim/Não/Cancelar.

**Parâmetros:**
- `title: string` - Título do diálogo
- `message: string` - Mensagem do diálogo
- `yesLabel?: string` - Label customizada para o botão Sim (padrão: 'Sim')
- `noLabel?: string` - Label customizada para o botão Não (padrão: 'Não')
- `cancelLabel?: string` - Label customizada para o botão Cancelar (padrão: 'Cancelar')

**Retorna:** `Observable<DialogResult>` - Emite `DialogResult.YES`, `DialogResult.NO` ou `DialogResult.CANCEL`

## DialogResult

Enum com os possíveis resultados:

```typescript
export enum DialogResult {
    OK = 'OK',
    YES = 'YES',
    NO = 'NO',
    CANCEL = 'CANCEL'
}
```

## Exemplo Completo em um Componente

```typescript
import { Component } from '@angular/core';
import { DialogService } from '../base/dialog/dialog.service';
import { DialogResult } from '../base/dialog/dialog.model';

@Component({
    selector: 'app-pessoa-grid',
    templateUrl: './pessoa-grid.component.html'
})
export class PessoaGridComponent {

    constructor(private dialogService: DialogService) {}

    excluir(id: string): void {
        // Confirma exclusão
        this.dialogService.showYesNo(
            'Confirmar Exclusão',
            'Tem certeza que deseja excluir esta pessoa?'
        ).subscribe(result => {
            if (result === DialogResult.YES) {
                // Chama o serviço de exclusão
                this.pessoaService.delete(id).subscribe({
                    next: () => {
                        // Mostra mensagem de sucesso
                        this.dialogService.showOk(
                            'Sucesso',
                            'Pessoa excluída com sucesso!'
                        ).subscribe(() => {
                            // Recarrega a lista
                            this.carregarLista();
                        });
                    },
                    error: () => {
                        // Mostra mensagem de erro
                        this.dialogService.showOk(
                            'Erro',
                            'Não foi possível excluir a pessoa.'
                        );
                    }
                });
            }
        });
    }

    voltarSemSalvar(): void {
        if (this.formulario.dirty) {
            this.dialogService.showYesNoCancel(
                'Alterações Não Salvas',
                'Você tem alterações não salvas. Deseja salvar antes de sair?',
                'Salvar',
                'Descartar',
                'Cancelar'
            ).subscribe(result => {
                switch (result) {
                    case DialogResult.YES:
                        this.salvar().then(() => this.router.navigate(['/']));
                        break;
                    case DialogResult.NO:
                        this.router.navigate(['/']);
                        break;
                    case DialogResult.CANCEL:
                        // Não faz nada
                        break;
                }
            });
        } else {
            this.router.navigate(['/']);
        }
    }
}
```

## Boas Práticas

1. **Sempre faça subscribe** - O diálogo só abre quando você faz subscribe no Observable
2. **Unsubscribe automático** - O Observable completa automaticamente após a escolha do usuário
3. **Mensagens claras** - Use mensagens objetivas que deixem claro o que vai acontecer
4. **Labels descritivas** - Customize as labels quando necessário para melhor UX
5. **Tratamento de erros** - Use o diálogo para mostrar erros ao usuário

## Observações

- O overlay **não fecha** ao clicar fora do diálogo por padrão (usuário deve escolher uma opção)
- Se quiser permitir fechar clicando fora, edite o método `onOverlayClick()` no componente
- O diálogo tem z-index de 9999 para ficar sobre outros elementos
- Apenas um diálogo pode ser exibido por vez
