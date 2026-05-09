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
  Validators,
} from '@angular/forms';
import { SelectModule } from 'primeng/select';
import { MessageService } from '../../../base/messages/messages.service';
import { TabelaRegraService } from '../tabela-regra.service';
import { TabelaRegraDTO } from '../model/tabela-regra-dto';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { AuthService } from '../../../base/auth/auth-service';
import { RouteConstants } from '../../../base/constants/route-constants';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import { ConvenioService } from '../../convenio/convenio.service';
import { ConvenioCategoriaService } from '../../convenio-categoria/convenio-categoria.service';
import { TabelaService } from '../../tabela/tabela.service';
import { ConvenioGridDTO } from '../../convenio/model/convenio-grid-dto';
import { ConvenioCategoriaGridDTO } from '../../convenio-categoria/model/convenio-categoria-grid-dto';
import { TabelaGridDTO } from '../../tabela/model/tabela-grid-dto';

@Component({
  selector: 'gi-tabela-regra-detalhe',
  standalone: true,
  imports: [BaseComponent, IftaLabelModule, ReactiveFormsModule, SelectModule],
  templateUrl: './tabela-regra-detalhe.component.html',
  providers: [
    TabelaRegraService,
    ConvenioService,
    ConvenioCategoriaService,
    TabelaService,
  ],
})
export class TabelaRegraDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup({});
  editMode = false;
  regra: TabelaRegraDTO = new TabelaRegraDTO();

  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  conveniosOptions: ConvenioGridDTO[] = [];
  categoriasOptions: ConvenioCategoriaGridDTO[] = [];
  tabelasOptions: TabelaGridDTO[] = [];

  titulo = $localize`Regra de Tabela: `;
  toolbarActions: ToolbarActionModel[] = [];

  private fb = inject(FormBuilder);
  private service = inject(TabelaRegraService);
  private convenioService = inject(ConvenioService);
  private convenioCategoriaService = inject(ConvenioCategoriaService);
  private tabelaService = inject(TabelaService);
  private messages = inject(MessageService);
  private auth = inject(AuthService);

  ngOnInit(): void {
    this.initForm();
    this.carregarConvenios();
    this.carregarTabelas();

    const canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.ATENDIMENTO_TABELA_REGRA,
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

    if (this.detailId === RouteConstants.P_ADD) {
      this.editMode = false;
      this.titulo += $localize`Nova`;
    } else {
      this.editMode = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.regra = response.body!;
        this.titulo += this.regra.convenioNome ?? '';
        this.fillForm();
      });
    }
  }

  initForm() {
    const fb = this.fb.nonNullable;
    this.form = fb.group({
      convenioId: fb.control('', [Validators.required]),
      convenioCategoriaId: fb.control<string | null>(null),
      tabelaId: fb.control('', [Validators.required]),
    });
  }

  fillForm() {
    this.form.get('convenioId')?.setValue(this.regra.convenioId ?? '');
    this.form.get('tabelaId')?.setValue(this.regra.tabelaId ?? '');
    if (this.regra.convenioId) {
      this.carregarCategorias(
        this.regra.convenioId,
        this.regra.convenioCategoriaId,
      );
    }
  }

  carregarConvenios(): void {
    this.convenioService.listarAtivos().subscribe((list) => {
      this.conveniosOptions = list;
    });
  }

  carregarTabelas(): void {
    this.tabelaService.listarAtivas().subscribe((list) => {
      this.tabelasOptions = list;
    });
  }

  carregarCategorias(convenioId: string, preSelectedId?: string | null): void {
    this.categoriasOptions = [];
    this.form.get('convenioCategoriaId')?.setValue(null);
    this.convenioCategoriaService
      .listarPorConvenio(convenioId)
      .subscribe((categorias) => {
        this.categoriasOptions = categorias;
        if (preSelectedId) {
          this.form.get('convenioCategoriaId')?.setValue(preSelectedId);
        }
      });
  }

  onConvenioChange(convenioId: string): void {
    if (convenioId) {
      this.carregarCategorias(convenioId);
    } else {
      this.categoriasOptions = [];
      this.form.get('convenioCategoriaId')?.setValue(null);
    }
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    const raw = this.form.getRawValue();
    this.regra.convenioId = raw.convenioId || undefined;
    this.regra.convenioCategoriaId = raw.convenioCategoriaId || undefined;
    this.regra.tabelaId = raw.tabelaId || undefined;

    this.service.save(this.regra, {
      onSuccess: () => {
        this.messages.sucesso($localize`Regra de tabela salva com sucesso.`);
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
