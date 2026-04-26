import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { BaseComponent } from '../../../../base/base.component';
import { IftaLabelModule } from 'primeng/iftalabel';
import { InputTextModule } from 'primeng/inputtext';
import { CheckboxModule } from 'primeng/checkbox';
import { AgendaService } from '../agenda.service';
import { AgendaDTO } from '../model/agenda-dto';
import { MessageService } from '../../../../base/messages/messages.service';
import { AuthService } from '../../../../base/auth/auth-service';
import { RouteConstants } from '../../../../base/constants/route-constants';
import { SystemModuleKey } from '../../../../base/enum/system-module-key.enum';
import { ToolbarActionModel } from '../../../../base/model/toolbar-action.model';
import { EntityFieldComponent } from '../../../../base/entity-field/entity-field.component';
import { EntitySearchService } from '../../../../base/entity-search/entity-search.service';
import { EntitySearchConfig } from '../../../../base/entity-search/entity-search.model';
import { ProfissionalDTO } from '../../../profissional/model/profissional-dto';
import { ProfissionalService } from '../../../profissional/profissional.service';
import { SetorDTO } from '../../../../cadastro/setor/model/setor-dto';
import { SetorService } from '../../../../cadastro/setor/setor.service';
import { AgendaRegraComponent } from '../../agendaregra/agenda-regra.component';

@Component({
  selector: 'gi-agenda-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    InputTextModule,
    CheckboxModule,
    EntityFieldComponent,
    AgendaRegraComponent,
  ],
  providers: [AgendaService, ProfissionalService, SetorService],
  templateUrl: './agenda-detalhe.component.html',
  styleUrl: './agenda-detalhe.component.css',
})
export class AgendaDetalheComponent implements OnInit {
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  form!: FormGroup;
  dto: AgendaDTO = new AgendaDTO();
  titulo = $localize`Agenda: `;
  toolbarActions: ToolbarActionModel[] = [];
  canEdit = false;

  profissionalSelecionado: ProfissionalDTO | null = null;
  setorSelecionado: SetorDTO | null = null;

  readonly profissionalLabel = $localize`Profissional`;
  readonly setorLabel = $localize`Setor`;

  readonly profissionalSearchConfig: EntitySearchConfig<ProfissionalDTO>;
  readonly setorSearchConfig: EntitySearchConfig<SetorDTO>;

  private fb = inject(FormBuilder);
  private service = inject(AgendaService);
  private profissionalService = inject(ProfissionalService);
  private setorService = inject(SetorService);
  private messages = inject(MessageService);
  private auth = inject(AuthService);
  private entitySearchService = inject(EntitySearchService);

  constructor() {
    this.profissionalSearchConfig = {
      service: this.profissionalService,
      searchFields: [{ key: 'pessoaNome', label: $localize`Nome` }],
      resultFields: [
        { key: 'pessoaNome', label: $localize`Nome` },
        { key: 'conselho',   label: $localize`Conselho` },
      ],
    };

    this.setorSearchConfig = {
      service: this.setorService,
      searchFields: [{ key: 'nome', label: $localize`Nome` }],
      resultFields: [
        { key: 'nome',             label: $localize`Nome` },
        { key: 'centroCustoNome',  label: $localize`Centro de Custo` },
      ],
    };
  }

  ngOnInit(): void {
    this.initForm();
    this.checkPermissions();
    this.createToolbarActions();

    if (this.detailId === RouteConstants.P_ADD) {
      this.prepareForNew();
    } else if (this.detailId) {
      this.prepareForEdit();
    }
  }

  private initForm(): void {
    this.form = this.fb.nonNullable.group({
      nome:          ['', [Validators.required, Validators.maxLength(255)]],
      profissionalId: ['', Validators.required],
      setorId:        ['', Validators.required],
      ativo:          [true],
    });
  }

  private checkPermissions(): void {
    this.canEdit = this.auth.hasAuthorityEditarToModulo(SystemModuleKey.AGENDAMENTO_AGENDA);
  }

  private createToolbarActions(): void {
    this.toolbarActions = [
      { action: () => this.goBackFn(), icon: 'close', title: $localize`Cancelar (esc)`, shortcut: 'escape' },
    ];
    if (this.canEdit) {
      this.toolbarActions.push({
        action: () => this.salvar(),
        icon: 'save',
        title: $localize`Salvar (enter)`,
        shortcut: 'enter',
      });
    }
  }

  private prepareForNew(): void {
    this.titulo += $localize`Nova`;
  }

  private prepareForEdit(): void {
    this.service.findById(String(this.detailId!)).subscribe((response) => {
      this.dto = response.body!;
      this.titulo += this.dto.nome;
      this.fillForm();
    });
  }

  private fillForm(): void {
    this.form.patchValue({
      nome:           this.dto.nome ?? '',
      profissionalId: this.dto.profissionalId ?? '',
      setorId:        this.dto.setorId ?? '',
      ativo:          this.dto.ativo ?? true,
    });
    if (this.dto.profissionalId) {
      this.profissionalSelecionado = {
        id: this.dto.profissionalId,
        pessoaNome: this.dto.profissionalNome,
      } as ProfissionalDTO;
    }
    if (this.dto.setorId) {
      this.setorSelecionado = {
        id: this.dto.setorId,
        nome: this.dto.setorNome,
      } as SetorDTO;
    }
  }

  pesquisarProfissional(): void {
    this.entitySearchService.search(this.profissionalSearchConfig).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.profissionalSelecionado = result.entity;
        this.form.get('profissionalId')?.setValue(result.entity.id);
      }
    });
  }

  onProfissionalSelected(entity: unknown): void {
    const prof = entity as ProfissionalDTO;
    this.profissionalSelecionado = prof;
    this.form.get('profissionalId')?.setValue(prof.id ?? '');
  }

  limparProfissional(): void {
    this.profissionalSelecionado = null;
    this.form.get('profissionalId')?.setValue('');
  }

  pesquisarSetor(): void {
    this.entitySearchService.search(this.setorSearchConfig).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.setorSelecionado = result.entity;
        this.form.get('setorId')?.setValue(result.entity.id);
      }
    });
  }

  onSetorSelected(entity: unknown): void {
    const setor = entity as SetorDTO;
    this.setorSelecionado = setor;
    this.form.get('setorId')?.setValue(setor.id ?? '');
  }

  limparSetor(): void {
    this.setorSelecionado = null;
    this.form.get('setorId')?.setValue('');
  }

  salvar(): void {
    if (!this.validateBeforeSave()) return;
    this.populateDTOBeforeSend();
    this.service.save(this.dto, {
      onSuccess: () => {
        this.messages.sucesso($localize`Agenda salva com sucesso.`);
        this.goBackFn();
      },
    });
  }

  private validateBeforeSave(): boolean {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return false;
    }
    return true;
  }

  private populateDTOBeforeSend(): void {
    const raw = this.form.getRawValue();
    this.dto.nome          = raw.nome;
    this.dto.profissionalId = raw.profissionalId;
    this.dto.setorId        = raw.setorId;
    this.dto.ativo          = raw.ativo;
  }

  isInvalid(campo: string): boolean {
    const fc: AbstractControl | null = this.form.get(campo);
    return fc !== null && fc.invalid && (fc.touched || fc.dirty);
  }

  goBackFn = (): void => {
    this.closeDetail.emit();
  };
}
