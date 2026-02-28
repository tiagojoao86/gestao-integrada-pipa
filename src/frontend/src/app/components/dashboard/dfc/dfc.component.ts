import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { DecimalPipe, NgClass } from '@angular/common';
import { forkJoin } from 'rxjs';
import { BaseComponent } from '../../base/base.component';
import { DashboardService, DFCDetalheItemDTO, DFCItemDTO, RegimeDFC } from '../dashboard.service';
import { SelectButton } from 'primeng/selectbutton';
import { DatePicker } from 'primeng/datepicker';
import { Button } from 'primeng/button';
import { Panel } from 'primeng/panel';
import { NgxEchartsDirective, provideEchartsCore } from 'ngx-echarts';
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import type { EChartsOption } from 'echarts';

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

// ---- Paletas por ano (índice = posição do ano no período) ----
const RECEITAS_COLORS = ['#15803d', '#22c55e', '#86efac', '#bbf7d0'];
const DESPESAS_COLORS = ['#b91c1c', '#ef4444', '#fca5a5', '#fee2e2'];

// ---- Interfaces internas ----

interface DFCTableRow {
  label: string;
  rowType: 'receitas' | 'gastos' | 'saldo';
  values: number[];
  total: number;
}

interface DetalheCategoria {
  id: string;
  nome: string;
  codigo: string;
  values: number[];
  total: number;
}

interface DetalheAgrupador {
  id: string;
  nome: string;
  codigo: string;
  tipo: 'RECEITA' | 'DESPESA';
  values: number[];
  total: number;
  categorias: DetalheCategoria[];
}

interface RegimeOption {
  label: string;
  value: RegimeDFC;
}

const MESES_PT = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];

@Component({
  selector: 'gi-dfc',
  standalone: true,
  imports: [
    BaseComponent,
    ReactiveFormsModule,
    SelectButton,
    DatePicker,
    Button,
    Panel,
    DecimalPipe,
    NgClass,
    NgxEchartsDirective,
  ],
  providers: [DashboardService, provideEchartsCore({ echarts })],
  templateUrl: './dfc.component.html',
  styleUrl: './dfc.component.css',
})
export class DfcComponent implements OnInit {
  title: string = $localize`Fluxo de Caixa`;

  form!: FormGroup;

  // Resumo
  mesesLabels: string[] = [];
  rows: DFCTableRow[] = [];

  // Detalhamento
  receitasAgrupadores: DetalheAgrupador[] = [];
  despesasAgrupadores: DetalheAgrupador[] = [];
  receitasTotaisPorMes: number[] = [];
  despesasTotaisPorMes: number[] = [];
  receitasTotalAno = 0;
  despesasTotalAno = 0;

  // Gráfico
  chartOptions: EChartsOption = {};

  regimeOptions: RegimeOption[] = [
    { label: $localize`Competência`, value: 'COMPETENCIA' },
    { label: $localize`Caixa`, value: 'CAIXA' },
  ];

  private fb = inject(FormBuilder);
  private dashboardService = inject(DashboardService);

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  private initForm(): void {
    const now = new Date();
    this.form = this.fb.group({
      dataInicio: [new Date(now.getFullYear(), 0, 1)],
      dataFim: [new Date(now.getFullYear(), 11, 31)],
      regime: ['COMPETENCIA'],
    });
  }

  filtrar(): void {
    this.loadData();
  }

  private loadData(): void {
    const { dataInicio, dataFim, regime } = this.form.value;
    forkJoin({
      resumo: this.dashboardService.getFluxoCaixa(dataInicio, dataFim, regime),
      detalhe: this.dashboardService.getFluxoCaixaDetalhe(dataInicio, dataFim, regime),
    }).subscribe({
      next: ({ resumo, detalhe }) => {
        const allMonths = this.generateMonths(dataInicio, dataFim);
        this.mesesLabels = allMonths.map((m) => this.formatMesLabel(m));
        this.buildResumo(resumo, allMonths);
        this.buildDetalhe(detalhe, allMonths);
        this.buildLineChart(resumo, allMonths);
      },
      error: (err) => this.dashboardService['handleError'](err),
    });
  }

  // ---- Resumo ----

  private buildResumo(data: DFCItemDTO[], allMonths: string[]): void {
    const dataMap = new Map<string, DFCItemDTO>(data.map((d) => [d.mes, d]));
    const entradas = allMonths.map((m) => Number(dataMap.get(m)?.entradas ?? 0));
    const saidas = allMonths.map((m) => Number(dataMap.get(m)?.saidas ?? 0));
    const saldo = entradas.map((e, i) => e - saidas[i]);
    const sum = (arr: number[]) => arr.reduce((a, b) => a + b, 0);
    this.rows = [
      { label: 'RECEITAS', rowType: 'receitas', values: entradas, total: sum(entradas) },
      { label: 'GASTOS', rowType: 'gastos', values: saidas, total: sum(saidas) },
      { label: 'SALDO DO MÊS', rowType: 'saldo', values: saldo, total: sum(saldo) },
    ];
  }

  // ---- Gráfico de linhas ----

  private buildLineChart(data: DFCItemDTO[], allMonths: string[]): void {
    const years = [...new Set(allMonths.map((m) => m.split('-')[0]))].sort();
    const isMultiYear = years.length > 1;

    if (isMultiYear) {
      this.buildMultiYearChart(data, years);
    } else {
      this.buildSingleYearChart(data, allMonths);
    }
  }

  /** Modo ano único: X = meses do período, 2 linhas (Receitas / Despesas). */
  private buildSingleYearChart(data: DFCItemDTO[], allMonths: string[]): void {
    const dataMap = new Map<string, DFCItemDTO>(data.map((d) => [d.mes, d]));
    const entradas = allMonths.map((m) => Number(dataMap.get(m)?.entradas ?? 0));
    const saidas = allMonths.map((m) => Number(dataMap.get(m)?.saidas ?? 0));
    const labels = allMonths.map((m) => this.formatMesLabel(m));

    this.chartOptions = this.buildChartOptions(labels, [
      { name: 'Receitas', data: entradas, color: RECEITAS_COLORS[0], dashed: false },
      { name: 'Despesas', data: saidas, color: DESPESAS_COLORS[0], dashed: false },
    ]);
  }

  /** Modo multi-ano: X = jan–dez, uma linha por (ano × tipo). */
  private buildMultiYearChart(data: DFCItemDTO[], years: string[]): void {
    const labels = MESES_PT;

    const series: { name: string; data: (number | null)[]; color: string; dashed: boolean }[] = [];

    years.forEach((year, idx) => {
      const byMonth = new Map<string, DFCItemDTO>(
        data.filter((d) => d.mes.startsWith(year)).map((d) => [d.mes.split('-')[1], d])
      );

      const entradas = MESES_PT.map((_, i) => {
        const key = String(i + 1).padStart(2, '0');
        return byMonth.has(key) ? Number(byMonth.get(key)!.entradas) : null;
      });
      const saidas = MESES_PT.map((_, i) => {
        const key = String(i + 1).padStart(2, '0');
        return byMonth.has(key) ? Number(byMonth.get(key)!.saidas) : null;
      });

      series.push({
        name: `Receitas ${year}`,
        data: entradas,
        color: RECEITAS_COLORS[idx % RECEITAS_COLORS.length],
        dashed: false,
      });
      series.push({
        name: `Despesas ${year}`,
        data: saidas,
        color: DESPESAS_COLORS[idx % DESPESAS_COLORS.length],
        dashed: true,
      });
    });

    this.chartOptions = this.buildChartOptions(labels, series);
  }

  private buildChartOptions(
    xLabels: string[],
    series: { name: string; data: (number | null)[]; color: string; dashed: boolean }[]
  ): EChartsOption {
    return {
      tooltip: {
        trigger: 'axis',
        formatter: (params: unknown) => {
          const items = params as { marker: string; seriesName: string; value: number | null;
            axisValue: string }[];
          const header = `<b>${items[0]?.axisValue ?? ''}</b><br/>`;
          const lines = items
            .map((p) => {
              const val = p.value != null ? p.value.toLocaleString('pt-BR') : '—';
              return `${p.marker} ${p.seriesName}: <b>${val}</b>`;
            })
            .join('<br/>');
          return header + lines;
        },
      },
      legend: { bottom: 0, type: 'scroll' },
      grid: { top: 24, right: 16, bottom: 56, left: 64 },
      xAxis: { type: 'category', data: xLabels, boundaryGap: false },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (v: number) => {
            if (v >= 1_000_000) return `${(v / 1_000_000).toFixed(1)}M`;
            if (v >= 1_000) return `${(v / 1_000).toFixed(0)}k`;
            return `${v}`;
          },
        },
      },
      series: series.map((s) => ({
        name: s.name,
        type: 'line',
        smooth: true,
        connectNulls: true,
        data: s.data,
        itemStyle: { color: s.color },
        lineStyle: { color: s.color, type: s.dashed ? 'dashed' : 'solid', width: 2 },
        symbol: 'circle',
        symbolSize: 6,
      })),
    };
  }

  // ---- Detalhamento ----

  private buildDetalhe(data: DFCDetalheItemDTO[], allMonths: string[]): void {
    const agrupadorMap = new Map<string, DetalheAgrupador>();

    for (const item of data) {
      let ag = agrupadorMap.get(item.agrupadorId);
      if (!ag) {
        ag = {
          id: item.agrupadorId,
          nome: item.agrupadorNome,
          codigo: item.agrupadorCodigo,
          tipo: item.tipo,
          values: new Array(allMonths.length).fill(0),
          total: 0,
          categorias: [],
        };
        agrupadorMap.set(item.agrupadorId, ag);
      }

      const idx = allMonths.indexOf(item.mes);
      if (idx >= 0) {
        ag.values[idx] += Number(item.total);
        ag.total += Number(item.total);
      }

      if (item.temAgrupador) {
        let cat = ag.categorias.find((c) => c.id === item.categoriaId);
        if (!cat) {
          cat = {
            id: item.categoriaId,
            nome: item.categoriaNome,
            codigo: item.categoriaCodigo,
            values: new Array(allMonths.length).fill(0),
            total: 0,
          };
          ag.categorias.push(cat);
        }
        if (idx >= 0) {
          cat.values[idx] += Number(item.total);
          cat.total += Number(item.total);
        }
      }
    }

    const sorted = [...agrupadorMap.values()].sort((a, b) => a.codigo.localeCompare(b.codigo));
    sorted.forEach((ag) => ag.categorias.sort((a, b) => a.codigo.localeCompare(b.codigo)));

    this.receitasAgrupadores = sorted.filter((a) => a.tipo === 'RECEITA');
    this.despesasAgrupadores = sorted.filter((a) => a.tipo === 'DESPESA');

    this.receitasTotaisPorMes = this.somarPorMes(this.receitasAgrupadores, allMonths.length);
    this.despesasTotaisPorMes = this.somarPorMes(this.despesasAgrupadores, allMonths.length);
    this.receitasTotalAno = this.receitasAgrupadores.reduce((s, a) => s + a.total, 0);
    this.despesasTotalAno = this.despesasAgrupadores.reduce((s, a) => s + a.total, 0);
  }

  private somarPorMes(agrupadores: DetalheAgrupador[], numMeses: number): number[] {
    const totais = new Array(numMeses).fill(0);
    agrupadores.forEach((ag) => ag.values.forEach((v, i) => (totais[i] += v)));
    return totais;
  }

  // ---- Utilitários ----

  private generateMonths(start: Date, end: Date): string[] {
    const months: string[] = [];
    const current = new Date(start.getFullYear(), start.getMonth(), 1);
    const endMonth = new Date(end.getFullYear(), end.getMonth(), 1);
    while (current <= endMonth) {
      months.push(`${current.getFullYear()}-${String(current.getMonth() + 1).padStart(2, '0')}`);
      current.setMonth(current.getMonth() + 1);
    }
    return months;
  }

  private formatMesLabel(mes: string): string {
    const [year, month] = mes.split('-');
    return `${MESES_PT[parseInt(month, 10) - 1]}-${year.slice(2)}`;
  }
}
