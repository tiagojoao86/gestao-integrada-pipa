import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { ButtonModule } from 'primeng/button';
import { SetorService } from '../../../cadastro/setor/setor.service';
import { SetorGridDTO } from '../../../cadastro/setor/model/setor-grid-dto';
import { PageRequest } from '../../../base/model/page-request';
import { FilterDTO } from '../../../base/model/filter-dto';

export interface TituloSetorRateio {
  setorId: string;
  setorNome: string;
  percentualRateio: number;
}

@Component({
  selector: 'gi-titulo-setor-rateio',
  imports: [FormsModule, SelectModule, InputNumberModule, ButtonModule],
  templateUrl: './titulo-setor-rateio.component.html',
  styleUrl: './titulo-setor-rateio.component.css',
  providers: [SetorService],
})
export class TituloSetorRateioComponent implements OnInit {
  @Input() setoresSelecionados: TituloSetorRateio[] = [];
  @Output() setoresChange = new EventEmitter<TituloSetorRateio[]>();

  setoresDisponiveis: SetorGridDTO[] = [];
  setorSelecionado: SetorGridDTO | null = null;
  tipoRateio: 'percentual' | 'valor' = 'percentual';
  valorTotal = 0;

  tipoRateioOptions = [
    { label: 'Percentual', value: 'percentual' },
    { label: 'Valor', value: 'valor' },
  ];

  private setorService = inject(SetorService);

  ngOnInit(): void {
    this.carregarSetores();
  }

  carregarSetores(): void {
    const filter: FilterDTO = {
      filterLogicOperator: 'AND',
      items: [],
    };
    const pageRequest = new PageRequest(filter, 1000, 0, []);
    this.setorService.list(pageRequest).subscribe((response) => {
      this.setoresDisponiveis = response.body?.content ?? [];
    });
  }

  get setoresDisponiveisParaSelecao(): SetorGridDTO[] {
    const idsJaSelecionados = this.setoresSelecionados.map((s) => s.setorId);
    return this.setoresDisponiveis.filter(
      (s) => !idsJaSelecionados.includes(s.id)
    );
  }

  adicionarSetor(): void {
    if (!this.setorSelecionado) {
      return;
    }

    const novoSetor: TituloSetorRateio = {
      setorId: this.setorSelecionado.id,
      setorNome: this.setorSelecionado.nome ?? '',
      percentualRateio: 0,
    };

    this.setoresSelecionados.push(novoSetor);
    this.setorSelecionado = null;
    this.distribuirPercentuais();
    this.emitirMudanca();
  }

  removerSetor(index: number): void {
    this.setoresSelecionados.splice(index, 1);
    this.distribuirPercentuais();
    this.emitirMudanca();
  }

  private distribuirPercentuais(): void {
    const qtd = this.setoresSelecionados.length;
    if (qtd === 0) {
      return;
    }
    const base = Math.floor((10000 / qtd)) / 100; // 2 decimais
    const soma = +(base * (qtd - 1)).toFixed(2);
    const ultimo = +(100 - soma).toFixed(2);

    for (let i = 0; i < qtd; i++) {
      this.setoresSelecionados[i].percentualRateio =
        i === 0 ? ultimo : base;
    }
  }

  onPercentualChange(index: number, valor: number | null): void {
    if (valor !== null && valor !== undefined) {
      this.setoresSelecionados[index].percentualRateio = valor;
      this.emitirMudanca();
    }
  }

  onValorChange(index: number, valor: number | null): void {
    if (valor !== null && valor !== undefined && this.valorTotal > 0) {
      const percentual = (valor / this.valorTotal) * 100;
      this.setoresSelecionados[index].percentualRateio = Number(
        percentual.toFixed(2)
      );
      this.emitirMudanca();
    }
  }

  calcularValorPorSetor(percentual: number): number {
    if (this.valorTotal === 0) {
      return 0;
    }
    return (this.valorTotal * percentual) / 100;
  }

  get somaPercentuais(): number {
    return this.setoresSelecionados.reduce(
      (sum, s) => sum + (s.percentualRateio || 0),
      0
    );
  }

  get percentualValido(): boolean {
    const soma = this.somaPercentuais;
    return Math.abs(soma - 100) < 0.01; // Tolerância de 0.01 para problemas de arredondamento
  }

  get mensagemValidacao(): string {
    if (this.setoresSelecionados.length === 0) {
      return 'Adicione pelo menos um setor';
    }

    const soma = this.somaPercentuais;
    if (soma < 100) {
      return `Faltam ${(100 - soma).toFixed(2)}% para completar 100%`;
    } else if (soma > 100) {
      return `Excede ${(soma - 100).toFixed(2)}% o total de 100%`;
    }

    return 'Distribuição válida';
  }

  private emitirMudanca(): void {
    this.setoresChange.emit([...this.setoresSelecionados]);
  }

  @Input()
  set valorTitulo(valor: number) {
    this.valorTotal = valor || 0;
  }
}
