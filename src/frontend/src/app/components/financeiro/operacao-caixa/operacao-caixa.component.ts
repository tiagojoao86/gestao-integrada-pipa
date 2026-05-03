import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { TextareaModule } from 'primeng/textarea';
import { TagModule } from 'primeng/tag';
import { IftaLabelModule } from 'primeng/iftalabel';

import { BaseComponent } from '../../base/base.component';
import { MessageService } from '../../base/messages/messages.service';
import { AberturaCaixaService } from './abertura-caixa.service';
import { CaixaComStatusDTO } from './model/caixa-com-status-dto';
import { StatusAberturaCaixa } from './model/status-abertura-caixa.enum';

@Component({
  selector: 'gi-operacao-caixa',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    BaseComponent,
    ButtonModule,
    CardModule,
    DialogModule,
    InputNumberModule,
    TextareaModule,
    TagModule,
    IftaLabelModule,
  ],
  templateUrl: './operacao-caixa.component.html',
  styleUrl: './operacao-caixa.component.css',
  providers: [AberturaCaixaService],
})
export class OperacaoCaixaComponent implements OnInit {
  titulo = $localize`Operação de Caixa`;
  caixas: CaixaComStatusDTO[] = [];
  loading = false;

  showAbrirDialog = false;
  showFecharDialog = false;
  caixaSelecionado: CaixaComStatusDTO | null = null;
  valorAbertura: number | null = null;
  valorConferencia: number | null = null;
  observacoes = '';

  readonly StatusAberturaCaixa = StatusAberturaCaixa;

  private service = inject(AberturaCaixaService);
  private messages = inject(MessageService);

  ngOnInit(): void {
    this.carregarCaixas();
  }

  carregarCaixas(): void {
    this.loading = true;
    this.service.listarMeusCaixas().subscribe({
      next: (data) => {
        this.caixas = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  abrirDialogAbrir(caixa: CaixaComStatusDTO): void {
    this.caixaSelecionado = caixa;
    this.valorAbertura = caixa.valorPadraoAbertura ?? 0;
    this.showAbrirDialog = true;
  }

  abrirDialogFechar(caixa: CaixaComStatusDTO): void {
    this.caixaSelecionado = caixa;
    this.valorConferencia = null;
    this.observacoes = '';
    this.showFecharDialog = true;
  }

  confirmarAbertura(): void {
    if (!this.caixaSelecionado) return;

    this.service.abrir(this.caixaSelecionado.caixaId, this.valorAbertura ?? 0).subscribe({
      next: () => {
        this.messages.sucesso($localize`Caixa aberto com sucesso.`);
        this.showAbrirDialog = false;
        this.carregarCaixas();
      },
    });
  }

  confirmarFechamento(): void {
    if (!this.caixaSelecionado || !this.caixaSelecionado.aberturaCaixaId) return;
    if (this.valorConferencia === null) {
      this.messages.erro($localize`Informe o valor de conferência.`);
      return;
    }

    this.service
      .fechar(
        this.caixaSelecionado.aberturaCaixaId,
        this.valorConferencia,
        this.observacoes
      )
      .subscribe({
        next: () => {
          this.messages.sucesso($localize`Caixa fechado com sucesso.`);
          this.showFecharDialog = false;
          this.carregarCaixas();
        },
      });
  }

  getSeverity(
    status: StatusAberturaCaixa | null
  ): 'success' | 'danger' | 'secondary' {
    if (status === StatusAberturaCaixa.ABERTO) return 'success';
    if (status === StatusAberturaCaixa.FECHADO) return 'danger';
    return 'secondary';
  }

  getStatusLabel(status: StatusAberturaCaixa | null): string {
    if (status === StatusAberturaCaixa.ABERTO) return $localize`Aberto`;
    if (status === StatusAberturaCaixa.FECHADO) return $localize`Fechado`;
    return $localize`Sem sessão`;
  }
}
