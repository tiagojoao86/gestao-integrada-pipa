import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { BaseComponent } from '../../base/base.component';
import { AtendimentoMesItemDTO, DashboardService, SetorLookupItemDTO } from '../dashboard.service';
import { AuthService } from '../../base/auth/auth-service';
import { DatePicker } from 'primeng/datepicker';
import { Button } from 'primeng/button';
import { Panel } from 'primeng/panel';
import { MultiSelectModule } from 'primeng/multiselect';
import { NgxEchartsDirective, provideEchartsCore } from 'ngx-echarts';
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import type { EChartsOption } from 'echarts';

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const ATENDIMENTO_COLORS = ['#1d4ed8', '#60a5fa', '#93c5fd', '#bfdbfe'];
const MESES_PT = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];

@Component({
  selector: 'gi-atendimento-por-mes',
  standalone: true,
  imports: [
    BaseComponent,
    ReactiveFormsModule,
    DatePicker,
    Button,
    Panel,
    MultiSelectModule,
    NgxEchartsDirective,
  ],
  providers: [DashboardService, provideEchartsCore({ echarts })],
  templateUrl: './atendimento-por-mes.component.html',
  styleUrl: './atendimento-por-mes.component.css',
})
export class AtendimentoPorMesComponent implements OnInit {
  title = $localize`Atendimentos por Mês`;

  form!: FormGroup;
  chartOptions: EChartsOption = {};
  mesesLabels: string[] = [];
  totaisPorMes: number[] = [];
  acumuladoPorMes: number[] = [];
  totalGeral = 0;

  unidadeOptions: { label: string; value: string }[] = [];
  setorOptions: { label: string; value: string }[] = [];

  private fb = inject(FormBuilder);
  private dashboardService = inject(DashboardService);
  private authService = inject(AuthService);

  ngOnInit(): void {
    this.loadUnidadeOptions();
    this.initForm();
    this.loadSetores();
    this.loadData();
  }

  private loadUnidadeOptions(): void {
    this.unidadeOptions = this.authService.getUnidadesNegocio().map((u) => ({
      label: `${u.unidadeNegocioCodigo} — ${u.unidadeNegocioNome}`,
      value: u.unidadeNegocioId,
    }));
  }

  private initForm(): void {
    const now = new Date();
    const allIds = this.unidadeOptions.map((u) => u.value);
    this.form = this.fb.group({
      dataInicio: [new Date(now.getFullYear(), 0, 1)],
      dataFim: [new Date(now.getFullYear(), 11, 31)],
      unidades: [allIds],
      setores: [[]],
    });
  }

  onUnidadesChange(): void {
    this.form.get('setores')?.setValue([]);
    this.loadSetores();
  }

  private loadSetores(): void {
    const unidadeIds: string[] = this.form.get('unidades')?.value ?? [];
    if (unidadeIds.length === 0) {
      this.setorOptions = [];
      return;
    }
    this.dashboardService.getSetoresByUnidades(unidadeIds).subscribe({
      next: (data: SetorLookupItemDTO[]) => {
        this.setorOptions = data.map((s) => ({ label: s.nome, value: s.id }));
      },
      error: (err) => this.dashboardService['handleError'](err),
    });
  }

  filtrar(): void {
    this.loadData();
  }

  private loadData(): void {
    const { dataInicio, dataFim, unidades, setores } = this.form.value;
    const unidadeIds: string[] = unidades ?? [];
    const setorIds: string[] = setores ?? [];
    this.dashboardService.getAtendimentosPorMes(dataInicio, dataFim, unidadeIds, setorIds).subscribe({
      next: (data) => {
        const allMonths = this.generateMonths(dataInicio, dataFim);
        this.mesesLabels = allMonths.map((m) => this.formatMesLabel(m));
        this.buildChart(data, allMonths);
        this.buildResumo(data, allMonths);
      },
      error: (err) => this.dashboardService['handleError'](err),
    });
  }

  private buildResumo(data: AtendimentoMesItemDTO[], allMonths: string[]): void {
    const dataMap = new Map<string, number>(data.map((d) => [d.mes, d.total]));
    this.totaisPorMes = allMonths.map((m) => dataMap.get(m) ?? 0);
    this.totalGeral = this.totaisPorMes.reduce((a, b) => a + b, 0);
    let acc = 0;
    this.acumuladoPorMes = this.totaisPorMes.map((v) => (acc += v));
  }

  private buildChart(data: AtendimentoMesItemDTO[], allMonths: string[]): void {
    const years = [...new Set(allMonths.map((m) => m.split('-')[0]))].sort();
    if (years.length > 1) {
      this.buildMultiYearChart(data, years);
    } else {
      this.buildSingleYearChart(data, allMonths);
    }
  }

  private buildSingleYearChart(data: AtendimentoMesItemDTO[], allMonths: string[]): void {
    const dataMap = new Map<string, number>(data.map((d) => [d.mes, d.total]));
    const totais = allMonths.map((m) => dataMap.get(m) ?? 0);
    const labels = allMonths.map((m) => this.formatMesLabel(m));
    this.chartOptions = this.buildChartOptions(labels, [
      { name: $localize`Atendimentos`, data: totais, color: ATENDIMENTO_COLORS[0] },
    ]);
  }

  private buildMultiYearChart(data: AtendimentoMesItemDTO[], years: string[]): void {
    const series: { name: string; data: (number | null)[]; color: string }[] = [];
    years.forEach((year, idx) => {
      const byMonth = new Map<string, number>(
        data.filter((d) => d.mes.startsWith(year)).map((d) => [d.mes.split('-')[1], d.total])
      );
      const totais = MESES_PT.map((_, i) => {
        const key = String(i + 1).padStart(2, '0');
        return byMonth.has(key) ? byMonth.get(key)! : null;
      });
      series.push({
        name: `${$localize`Atendimentos`} ${year}`,
        data: totais,
        color: ATENDIMENTO_COLORS[idx % ATENDIMENTO_COLORS.length],
      });
    });
    this.chartOptions = this.buildChartOptions(MESES_PT, series);
  }

  private buildChartOptions(
    xLabels: string[],
    series: { name: string; data: (number | null)[]; color: string }[]
  ): EChartsOption {
    return {
      tooltip: {
        trigger: 'axis',
        formatter: (params: unknown) => {
          const items = params as { marker: string; seriesName: string;
            value: number | null; axisValue: string }[];
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
      grid: { top: 24, right: 16, bottom: 56, left: 48 },
      xAxis: { type: 'category', data: xLabels, boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      series: series.map((s) => ({
        name: s.name,
        type: 'line',
        smooth: true,
        connectNulls: true,
        data: s.data,
        itemStyle: { color: s.color },
        lineStyle: { color: s.color, width: 2 },
        symbol: 'circle',
        symbolSize: 6,
      })),
    };
  }

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
