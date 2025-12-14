import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { RouteConstants } from '../../../base/constants/route-constants';
import { CategoriaTituloService } from '../categoria-titulo.service';
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
import { TextareaModule } from 'primeng/textarea';
import { MessageService } from '../../../base/messages/messages.service';
import { CategoriaTituloDTO } from '../model/categoria-titulo.dto';
import { AuthService } from '../../../base/auth/auth-service';

@Component({
  selector: 'gi-categoria-titulo-detalhe',
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    TextareaModule,
  ],
  templateUrl: './categoria-titulo-detalhe.component.html',
  styleUrls: ['./categoria-titulo-detalhe.component.css'],
  providers: [CategoriaTituloService],
})
export class CategoriaTituloDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup([]);
  editMode = false;
  categoria: CategoriaTituloDTO = {} as CategoriaTituloDTO;
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  private service: CategoriaTituloService = inject(CategoriaTituloService);
  private messages: MessageService = inject(MessageService);

  titulo = $localize`Categoria de Título: `;

  toolbarActions: ToolbarActionModel[] = [];
  private auth: AuthService = inject(AuthService);

  ngOnInit(): void {
    this.initForm();

    const canEdit = this.auth.hasAuthorityEditarToModulo(
      'CADASTRO_CATEGORIA_TITULO'
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
      this.titulo += $localize`Nova`;
      this.categoria = { nome: '' } as CategoriaTituloDTO;
      this.fillForm();
    } else {
      this.editMode = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.categoria = response.body;
        this.titulo += this.categoria.nome;
        this.fillForm();
      });
    }
  }

  initForm() {
    const fb = new FormBuilder().nonNullable;
    this.form.addControl('nome', fb.control(''));
    this.form.addControl('descricao', fb.control(''));
  }

  fillForm() {
    this.form.get('nome')?.setValue(this.categoria.nome || '');
    this.form.get('descricao')?.setValue(this.categoria.descricao || '');
  }

  salvar() {
    if (!this.form.valid) {
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    this.categoria.nome = this.form.value.nome;
    this.categoria.descricao = this.form.value.descricao;

    this.service.save(this.categoria, {
      onSuccess: (data: CategoriaTituloDTO) => {
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
