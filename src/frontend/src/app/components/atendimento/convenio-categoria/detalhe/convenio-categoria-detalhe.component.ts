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
import { MessageService } from '../../../base/messages/messages.service';
import { ConvenioCategoriaService } from '../convenio-categoria.service';
import { ConvenioCategoriaDTO } from '../model/convenio-categoria-dto';
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
import { ConvenioDTO } from '../../convenio/model/convenio-dto';
import { ConvenioService } from '../../convenio/convenio.service';
import { ConvenioDetalheComponent } from '../../convenio/detalhe/convenio-detalhe.component';

@Component({
  selector: 'gi-convenio-categoria-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    CheckboxModule,
    EntityFieldComponent,
    ConvenioDetalheComponent,
  ],
  templateUrl: './convenio-categoria-detalhe.component.html',
  styleUrl: './convenio-categoria-detalhe.component.css',
  providers: [ConvenioCategoriaService, ConvenioService],
})
export class ConvenioCategoriaDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup({});
  editMode = false;
  convenioCategoria: ConvenioCategoriaDTO = new ConvenioCategoriaDTO();
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  convenioSelecionado: ConvenioDTO | null = null;
  readonly convenioLabel = $localize`Convênio`;

  titulo = $localize`Categoria de Convênio: `;
  toolbarActions: ToolbarActionModel[] = [];
  canCadastrarConvenio = false;
  showConvenioDetalhe = false;

  private fb = inject(FormBuilder);
  private service = inject(ConvenioCategoriaService);
  private convenioService = inject(ConvenioService);
  private messages = inject(MessageService);
  private auth = inject(AuthService);
  private entitySearchService = inject(EntitySearchService);

  ngOnInit(): void {
    this.initForm();

    const canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.ATENDIMENTO_CONVENIO_CATEGORIA
    );
    this.canCadastrarConvenio = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.ATENDIMENTO_CONVENIO
    );

    this.toolbarActions = [
      {
        action: () => this.goBackFn(),
        icon: 'close',
        title: $localize`Cancelar` + ' (esc)',
        shortcut: 'escape',
      },
    ];

    if (canEdit) {
      this.toolbarActions.push({
        action: () => this.save(),
        icon: 'save',
        title: $localize`Salvar` + ' (enter)',
        shortcut: 'enter',
      });
    }

    if (this.canCadastrarConvenio && this.detailId === RouteConstants.P_ADD) {
      this.toolbarActions.unshift({
        action: () => this.abrirCadastroConvenio(),
        icon: 'handshake',
        title: $localize`Cadastrar novo convênio`,
      });
    }

    if (this.detailId === RouteConstants.P_ADD) {
      this.editMode = false;
      this.titulo += $localize`Nova`;
      this.fillForm();
    } else {
      this.editMode = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.convenioCategoria = response.body!;
        this.titulo += this.convenioCategoria.nome;
        this.fillForm();
      });
    }
  }

  initForm() {
    const fb = this.fb.nonNullable;
    this.form = fb.group({
      convenioId: fb.control('', [Validators.required]),
      nome: fb.control('', [Validators.required, Validators.maxLength(100)]),
      codigoAnsPlano: fb.control('', [Validators.maxLength(20)]),
      ativo: fb.control(true),
    });
  }

  fillForm() {
    this.form.get('nome')?.setValue(this.convenioCategoria.nome ?? '');
    this.form.get('codigoAnsPlano')?.setValue(this.convenioCategoria.codigoAnsPlano ?? '');
    this.form.get('ativo')?.setValue(this.convenioCategoria.ativo ?? true);
    if (this.convenioCategoria.convenioId) {
      this.form.get('convenioId')?.setValue(this.convenioCategoria.convenioId);
      this.convenioSelecionado = {
        id: this.convenioCategoria.convenioId,
        nome: this.convenioCategoria.convenioNome,
      } as ConvenioDTO;
    }
  }

  pesquisarConvenio(): void {
    const searchFields: SearchField[] = [
      { key: 'nome', label: $localize`Nome` },
      { key: 'registroAns', label: $localize`Registro ANS` },
    ];
    const resultFields: ResultField[] = [
      { key: 'nome', label: $localize`Nome` },
      { key: 'registroAns', label: $localize`Registro ANS` },
    ];
    const config: EntitySearchConfig<ConvenioDTO> = {
      service: this.convenioService,
      searchFields,
      resultFields,
      title: $localize`Selecionar Convênio`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.convenioSelecionado = result.entity;
        this.form.get('convenioId')?.setValue(result.entity.id);
      }
    });
  }

  limparConvenio(): void {
    this.convenioSelecionado = null;
    this.form.get('convenioId')?.setValue('');
  }

  abrirCadastroConvenio(): void {
    this.showConvenioDetalhe = true;
  }

  fecharConvenioDetalhe(): void {
    this.showConvenioDetalhe = false;
  }

  onConvenioSalvo(convenio: { id: string; nome: string }): void {
    this.convenioSelecionado = { id: convenio.id, nome: convenio.nome } as ConvenioDTO;
    this.form.get('convenioId')?.setValue(convenio.id);
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    const raw = this.form.getRawValue();
    this.convenioCategoria.convenioId = raw.convenioId;
    this.convenioCategoria.nome = raw.nome;
    this.convenioCategoria.codigoAnsPlano = raw.codigoAnsPlano || undefined;
    this.convenioCategoria.ativo = raw.ativo;

    this.service.save(this.convenioCategoria, {
      onSuccess: () => {
        this.messages.sucesso($localize`Categoria de convênio salva com sucesso.`);
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
