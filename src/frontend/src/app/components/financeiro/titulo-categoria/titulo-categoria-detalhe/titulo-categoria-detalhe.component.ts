import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { RouteConstants } from '../../../base/constants/route-constants';
import { TituloCategoriaService } from '../titulo-categoria.service';
import { BaseComponent } from '../../../base/base.component';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';

import { IftaLabelModule } from 'primeng/iftalabel';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  FormsModule,
} from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';
import { MessageService } from '../../../base/messages/messages.service';
import { TituloCategoriaDTO } from '../model/titulo-categoria.dto';
import { AuthService } from '../../../base/auth/auth-service';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import { TituloCategoriaTipoEnum } from '../model/titulo-categoria-tipo.enum';

@Component({
  selector: 'gi-titulo-categoria-detalhe',
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    TextareaModule,
    SelectModule,
  ],
  templateUrl: './titulo-categoria-detalhe.component.html',
  styleUrls: ['./titulo-categoria-detalhe.component.css'],
  providers: [TituloCategoriaService],
})
export class CategoriaTituloDetalheComponent implements OnInit {
  tiposCategoria = TituloCategoriaTipoEnum.getList();
  form: FormGroup = new FormGroup([]);
  editMode = false;
  categoria: TituloCategoriaDTO = {} as TituloCategoriaDTO;
  agrupadores: TituloCategoriaDTO[] = [];
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  private service: TituloCategoriaService = inject(TituloCategoriaService);
  private messages: MessageService = inject(MessageService);

  title = $localize`Categoria de Título: `;

  toolbarActions: ToolbarActionModel[] = [];
  private auth: AuthService = inject(AuthService);

  ngOnInit(): void {
    this.initForm();
    this.loadAgrupadores();

    const canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.FINANCEIRO_TITULO_CATEGORIA
    );
    this.toolbarActions = [
      {
        action: () => {
          this.goBackFn();
        },
        icon: 'close',
        title: $localize`Cancelar` + ' (esc)',
        shortcut: 'escape',
      },
    ];

    if (canEdit) {
      this.toolbarActions.push({
        action: () => {
          this.salvar();
        },
        icon: 'save',
        title: $localize`Salvar` + ' (enter)',
        shortcut: 'enter',
      });
    }

    if (this.detailId === RouteConstants.P_ADD) {
      this.editMode = false;
      this.title += $localize`Nova`;
      this.categoria = {
        tipo: TituloCategoriaTipoEnum.DESPESA,
      } as TituloCategoriaDTO;
      this.fillForm();
    } else {
      this.editMode = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.categoria = response.body!;
        this.title += this.categoria.nome;
        this.fillForm();
      });
    }
  }

  initForm() {
    const fb = new FormBuilder().nonNullable;
    this.form.addControl('codigo', fb.control(''));
    this.form.addControl('nome', fb.control(''));
    this.form.addControl('descricao', fb.control(''));
    this.form.addControl('tipo', fb.control(''));
    this.form.addControl('agrupadorId', fb.control(null));
  }

  fillForm() {
    this.form.get('codigo')?.setValue(this.categoria.codigo || '');
    this.form.get('nome')?.setValue(this.categoria.nome || '');
    this.form.get('descricao')?.setValue(this.categoria.descricao || '');
    this.form.get('tipo')?.setValue(this.categoria.tipo || null);
    this.form.get('agrupadorId')?.setValue(this.categoria.agrupadorId || null);
  }

  loadAgrupadores() {
    this.service.listAll().subscribe((response) => {
      this.agrupadores = response.body || [];
    });
  }

  salvar() {
    if (!this.form.valid) {
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    this.categoria.codigo = this.form.value.codigo;
    this.categoria.nome = this.form.value.nome;
    this.categoria.descricao = this.form.value.descricao;
    this.categoria.tipo = this.form.value.tipo;
    this.categoria.agrupadorId = this.form.value.agrupadorId;

    this.service.save(this.categoria, {
      onSuccess: (data: TituloCategoriaDTO) => {
        this.categoria = data;
        this.messages.sucesso($localize`Categoria salva com sucesso.`);
        this.goBackFn();
      },
    });
  }

  isControlInvalid(campo: string) {
    const fc: AbstractControl<unknown, unknown> | null = this.form.get(campo);

    if (fc !== null && fc.invalid && (fc.touched || fc.dirty)) {
      return true;
    }

    return false;
  }

  goBackFn = () => {
    this.closeDetail.emit();
  };
}
