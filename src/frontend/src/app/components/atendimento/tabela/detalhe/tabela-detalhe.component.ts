import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { IftaLabelModule } from 'primeng/iftalabel';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  FormsModule,
  Validators,
} from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { CheckboxModule } from 'primeng/checkbox';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageService } from '../../../base/messages/messages.service';
import { TabelaService } from '../tabela.service';
import { TabelaDTO } from '../model/tabela-dto';
import { TabelaItemDTO } from '../model/tabela-item-dto';
import { getTiposTabela, TipoTabela } from '../model/tipo-tabela.enum';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { AuthService } from '../../../base/auth/auth-service';
import { RouteConstants } from '../../../base/constants/route-constants';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import { EntitySearchService } from '../../../base/entity-search/entity-search.service';
import {
  EntitySearchConfig,
  SearchField,
  ResultField,
} from '../../../base/entity-search/entity-search.model';
import { EntityFieldComponent } from '../../../base/entity-field/entity-field.component';
import { ProcedimentoDTO } from '../../procedimento/model/procedimento-dto';
import { ProcedimentoService } from '../../procedimento/procedimento.service';

@Component({
  selector: 'gi-tabela-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    CheckboxModule,
    SelectModule,
    TableModule,
    ButtonModule,
    DatePickerModule,
    InputNumberModule,
    EntityFieldComponent,
  ],
  templateUrl: './tabela-detalhe.component.html',
  styleUrl: './tabela-detalhe.component.css',
  providers: [TabelaService, ProcedimentoService],
})
export class TabelaDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup({});
  editMode = false;
  tabela: TabelaDTO = new TabelaDTO();
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  // Itens inline
  itens: TabelaItemDTO[] = [];
  procedimentoSelecionado: ProcedimentoDTO | null = null;
  valorInputTemp: number | null = null;
  vigenciaInicioTemp: Date | null = null;
  vigenciaFimTemp: Date | null = null;
  readonly procedimentoLabel = $localize`Procedimento`;

  tiposTabela = getTiposTabela();

  titulo = $localize`Tabela de Preços: `;
  toolbarActions: ToolbarActionModel[] = [];
  canEdit = false;

  private fb = inject(FormBuilder);
  private service = inject(TabelaService);
  private procedimentoService = inject(ProcedimentoService);
  private messages = inject(MessageService);
  private auth = inject(AuthService);
  private entitySearchService = inject(EntitySearchService);

  ngOnInit(): void {
    this.initForm();

    this.canEdit = this.auth.hasAuthorityEditarToModulo(SystemModuleKey.ATENDIMENTO_TABELA);

    this.toolbarActions = [
      { action: () => this.goBackFn(), icon: 'close', title: $localize`Cancelar` + ' (esc)', shortcut: 'escape' },
    ];

    if (this.canEdit) {
      this.toolbarActions.push({
        action: () => this.save(),
        icon: 'save',
        title: $localize`Salvar` + ' (enter)',
        shortcut: 'enter',
      });
    }

    if (this.detailId === RouteConstants.P_ADD) {
      this.editMode = false;
      this.titulo += $localize`Nova`;
      this.fillForm();
    } else {
      this.editMode = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.tabela = response.body!;
        this.titulo += this.tabela.nome;
        this.itens = this.tabela.itens ? [...this.tabela.itens] : [];
        this.fillForm();
      });
    }
  }

  initForm() {
    const fb = this.fb.nonNullable;
    this.form = fb.group({
      nome: fb.control('', [Validators.required, Validators.maxLength(100)]),
      tipo: fb.control<TipoTabela | null>(null, [Validators.required]),
      ativo: fb.control(true),
    });
  }

  fillForm() {
    this.form.get('nome')?.setValue(this.tabela.nome ?? '');
    this.form.get('tipo')?.setValue(this.tabela.tipo ?? null);
    this.form.get('ativo')?.setValue(this.tabela.ativo ?? true);
  }

  // =========================================================================
  // Gestão inline de TabelaItem
  // =========================================================================

  pesquisarProcedimento(): void {
    const searchFields: SearchField[] = [
      { key: 'codigo', label: $localize`Código` },
      { key: 'descricao', label: $localize`Descrição` },
    ];
    const resultFields: ResultField[] = [
      { key: 'codigo', label: $localize`Código` },
      { key: 'descricao', label: $localize`Descrição` },
    ];
    const config: EntitySearchConfig<ProcedimentoDTO> = {
      service: this.procedimentoService,
      searchFields,
      resultFields,
      title: $localize`Selecionar Procedimento`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.procedimentoSelecionado = result.entity;
      }
    });
  }

  limparProcedimento(): void {
    this.procedimentoSelecionado = null;
    this.valorInputTemp = null;
    this.vigenciaInicioTemp = null;
    this.vigenciaFimTemp = null;
  }

  adicionarItem(): void {
    if (!this.procedimentoSelecionado) {
      this.messages.erro($localize`Selecione um procedimento.`);
      return;
    }
    if (this.valorInputTemp === null || this.valorInputTemp < 0) {
      this.messages.erro($localize`Informe um valor válido.`);
      return;
    }
    if (!this.vigenciaInicioTemp) {
      this.messages.erro($localize`Informe a vigência inicial.`);
      return;
    }

    const jaExiste = this.itens.some(
      (i) => i.procedimentoId === this.procedimentoSelecionado!.id
        && !i.vigenciaFim
    );
    if (jaExiste) {
      this.messages.erro($localize`Já existe um item sem vigência final para este procedimento.`);
      return;
    }

    const novo = new TabelaItemDTO();
    novo.procedimentoId = this.procedimentoSelecionado.id;
    novo.procedimentoCodigo = this.procedimentoSelecionado.codigo;
    novo.procedimentoDescricao = this.procedimentoSelecionado.descricao;
    novo.valor = this.valorInputTemp;
    novo.vigenciaInicio = this.vigenciaInicioTemp.toISOString().substring(0, 10);
    novo.vigenciaFim = this.vigenciaFimTemp
      ? this.vigenciaFimTemp.toISOString().substring(0, 10)
      : undefined;

    this.itens = [...this.itens, novo];
    this.limparProcedimento();
  }

  removerItem(index: number): void {
    this.itens = this.itens.filter((_, i) => i !== index);
  }

  formatarValor(valor: number | undefined): string {
    if (valor === undefined || valor === null) return '';
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }

  // =========================================================================

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    const raw = this.form.getRawValue();
    this.tabela.nome = raw.nome;
    this.tabela.tipo = raw.tipo;
    this.tabela.ativo = raw.ativo;
    this.tabela.itens = this.itens;

    this.service.save(this.tabela, {
      onSuccess: () => {
        this.messages.sucesso($localize`Tabela salva com sucesso.`);
        this.goBackFn();
      },
    });
  }

  isControlInvalid(campo: string): boolean {
    const fc: AbstractControl | null = this.form.get(campo);
    return fc !== null && fc.invalid && (fc.touched || fc.dirty);
  }

  goBackFn = () => {
    this.closeDetail.emit();
  };
}
