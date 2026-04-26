import {
  Component,
  inject,
  Input,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumberModule } from 'primeng/inputnumber';
import { MultiSelectModule } from 'primeng/multiselect';
import { CheckboxModule } from 'primeng/checkbox';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { IftaLabelModule } from 'primeng/iftalabel';
import { FormsModule } from '@angular/forms';
import { FilterDTO, FilterLogicOperator } from '../../../base/model/filter-dto';
import { PageRequest } from '../../../base/model/page-request';
import { MessageService } from '../../../base/messages/messages.service';
import { DialogService } from '../../../base/dialog/dialog.service';
import { DialogResult } from '../../../base/dialog/dialog.model';
import { AgendaRegraService, ConflitoPar } from './agenda-regra.service';
import { AgendaRegraDTO } from './model/agenda-regra-dto';
import { AgendaRegraGridDTO } from './model/agenda-regra-grid-dto';
import { DIA_SEMANA_OPTIONS, DiaSemanaOption } from './model/dia-semana.enum';
import { ConvenioService } from '../../convenio/convenio.service';
import { ConvenioGridDTO } from '../../convenio/model/convenio-grid-dto';
import { ProcedimentoService } from '../../procedimento/procedimento.service';
import { ProcedimentoGridDTO } from '../../procedimento/model/procedimento-grid-dto';

@Component({
  selector: 'gi-agenda-regra',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    DatePickerModule,
    InputNumberModule,
    MultiSelectModule,
    CheckboxModule,
    TableModule,
    ButtonModule,
    MessageModule,
    IftaLabelModule,
  ],
  providers: [AgendaRegraService, ConvenioService, ProcedimentoService],
  templateUrl: './agenda-regra.component.html',
  styleUrl: './agenda-regra.component.css',
})
export class AgendaRegraComponent implements OnChanges {
  @Input() agendaId!: string;

  form!: FormGroup;
  regras: AgendaRegraGridDTO[] = [];
  conflitos: ConflitoPar[] = [];
  editandoId: string | null = null;
  mostrarFormulario = false;

  diasSemanaOptions: DiaSemanaOption[] = DIA_SEMANA_OPTIONS;
  diasSelecionados: string[] = ['SEG', 'TER', 'QUA', 'QUI', 'SEX'];

  convenioOptions: ConvenioGridDTO[] = [];
  procedimentoOptions: ProcedimentoGridDTO[] = [];

  // Valores temporários para DatePicker (o componente trabalha com Date)
  dataInicioTemp: Date | null = null;
  dataFimTemp: Date | null = null;
  horaInicioTemp: Date | null = null;
  horaFimTemp: Date | null = null;

  private fb = inject(FormBuilder);
  private service = inject(AgendaRegraService);
  private convenioService = inject(ConvenioService);
  private procedimentoService = inject(ProcedimentoService);
  private messages = inject(MessageService);
  private dialogService = inject(DialogService);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['agendaId'] && this.agendaId) {
      this.initForm();
      this.carregarRegras();
      this.carregarConflitos();
      this.carregarOpcoes();
    }
  }

  private initForm(): void {
    this.form = this.fb.nonNullable.group({
      duracaoSessaoMinutos: [30, [Validators.required, Validators.min(1)]],
      convenioIds: [[]],
      procedimentoIds: [[]],
    });
  }

  private carregarOpcoes(): void {
    const req = new PageRequest(
      { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] } as FilterDTO,
      200, 0, []
    );
    this.convenioService.list(req).subscribe((r) => {
      this.convenioOptions = r.body?.content ?? [];
    });
    this.procedimentoService.list(req).subscribe((r) => {
      this.procedimentoOptions = r.body?.content ?? [];
    });
  }

  carregarRegras(): void {
    this.service.listByAgenda(this.agendaId).subscribe((r) => {
      this.regras = (r.body as unknown as AgendaRegraGridDTO[]) ?? [];
    });
  }

  carregarConflitos(): void {
    this.service.getConflitos(this.agendaId).subscribe((r) => {
      this.conflitos = (r.body as unknown as ConflitoPar[]) ?? [];
    });
  }

  novaRegra(): void {
    this.editandoId = null;
    this.limparCampos();
    this.mostrarFormulario = true;
  }

  editarRegra(id: string | undefined): void {
    if (!id) return;
    this.service.findById(id).subscribe((r) => {
      if (!r.body) return;
      const dto = r.body;
      this.editandoId = id;
      this.dataInicioTemp = dto.dataInicio ? new Date(dto.dataInicio + 'T00:00:00') : null;
      this.dataFimTemp = dto.dataFim ? new Date(dto.dataFim + 'T00:00:00') : null;
      this.horaInicioTemp = dto.horaInicio ? this.parseTime(dto.horaInicio) : null;
      this.horaFimTemp = dto.horaFim ? this.parseTime(dto.horaFim) : null;
      this.diasSelecionados = dto.diasSemana ?? [];
      this.form.patchValue({
        duracaoSessaoMinutos: dto.duracaoSessaoMinutos ?? 30,
        convenioIds: dto.convenioIds ?? [],
        procedimentoIds: dto.procedimentoIds ?? [],
      });
      this.mostrarFormulario = true;
    });
  }

  salvar(): void {
    if (!this.dataInicioTemp) {
      this.messages.erro($localize`Data de início é obrigatória.`);
      return;
    }
    if (!this.horaInicioTemp || !this.horaFimTemp) {
      this.messages.erro($localize`Horário de início e fim são obrigatórios.`);
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }
    const raw = this.form.getRawValue();
    const dto = new AgendaRegraDTO();
    if (this.editandoId) dto.id = this.editandoId;
    dto.agendaId = this.agendaId;
    dto.dataInicio = this.toIsoDate(this.dataInicioTemp);
    dto.dataFim = this.dataFimTemp ? this.toIsoDate(this.dataFimTemp) : undefined;
    dto.horaInicio = this.toHHmm(this.horaInicioTemp);
    dto.horaFim = this.toHHmm(this.horaFimTemp);
    dto.duracaoSessaoMinutos = raw.duracaoSessaoMinutos;
    dto.diasSemana = this.diasSelecionados;
    dto.convenioIds = raw.convenioIds ?? [];
    dto.procedimentoIds = raw.procedimentoIds ?? [];

    this.service.save(dto, {
      onSuccess: () => {
        this.messages.sucesso($localize`Regra salva com sucesso.`);
        this.cancelar();
        this.carregarRegras();
        this.carregarConflitos();
      },
    });
  }

  excluir(id: string | undefined): void {
    if (!id) return;
    this.dialogService
      .showYesNo($localize`Confirmar Exclusão`, $localize`Deseja realmente excluir esta regra?`)
      .subscribe((result) => {
        if (result === DialogResult.YES) {
          this.service.delete(id).subscribe(() => {
            this.carregarRegras();
            this.carregarConflitos();
          });
        }
      });
  }

  cancelar(): void {
    this.mostrarFormulario = false;
    this.editandoId = null;
    this.limparCampos();
  }

  private limparCampos(): void {
    this.dataInicioTemp = null;
    this.dataFimTemp = null;
    this.horaInicioTemp = null;
    this.horaFimTemp = null;
    this.diasSelecionados = ['SEG', 'TER', 'QUA', 'QUI', 'SEX'];
    this.form?.reset({ duracaoSessaoMinutos: 30, convenioIds: [], procedimentoIds: [] });
  }

  toggleDia(key: string, checked: boolean): void {
    if (checked) {
      if (!this.diasSelecionados.includes(key)) {
        this.diasSelecionados = [...this.diasSelecionados, key];
      }
    } else {
      this.diasSelecionados = this.diasSelecionados.filter(d => d !== key);
    }
  }

  formatarData(data?: string): string {
    if (!data) return '';
    const [y, m, d] = data.split('-');
    return `${d}/${m}/${y}`;
  }

  nomeDoDia(key: string): string {
    return this.diasSemanaOptions.find((d) => d.key === key)?.label ?? key;
  }

  temConflito(id: string | undefined): boolean {
    if (!id) return false;
    return this.conflitos.some((c) => c.regraIdA === id || c.regraIdB === id);
  }

  private toIsoDate(date: Date): string {
    return date.toISOString().substring(0, 10);
  }

  private toHHmm(date: Date | null): string {
    if (!date) return '';
    const h = date.getHours().toString().padStart(2, '0');
    const m = date.getMinutes().toString().padStart(2, '0');
    return `${h}:${m}`;
  }

  private parseTime(hhmm: string): Date {
    const [h, m] = hhmm.split(':').map(Number);
    const d = new Date();
    d.setHours(h, m, 0, 0);
    return d;
  }
}
