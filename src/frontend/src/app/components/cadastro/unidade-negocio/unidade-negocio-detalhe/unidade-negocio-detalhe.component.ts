import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { RouteConstants } from '../../../base/constants/route-constants';
import { UnidadeNegocioService } from '../unidade-negocio.service';
import {
  RegisterActionToolbar,
  BaseComponent,
} from '../../../base/base.component';

import { IftaLabelModule } from 'primeng/iftalabel';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  FormsModule,
} from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { MessageService } from '../../../base/messages/messages.service';
import { UnidadeNegocioDTO } from '../model/unidade-negocio-dto';
import { AuthService } from '../../../base/auth/auth-service';
import { CheckboxModule } from 'primeng/checkbox';
import { TextareaModule } from 'primeng/textarea';
import { InputMaskModule } from 'primeng/inputmask';

@Component({
  selector: 'gi-unidade-negocio-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    CheckboxModule,
    TextareaModule,
    InputMaskModule
],
  templateUrl: './unidade-negocio-detalhe.component.html',
  styleUrl: './unidade-negocio-detalhe.component.css',
  providers: [UnidadeNegocioService],
})
export class UnidadeNegocioDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup([]);
  modoEdicao = false;
  unidadeNegocio: UnidadeNegocioDTO = {} as UnidadeNegocioDTO;

  @Input() id: string | number | null = null;
  @Output() closeDetailEvent = new EventEmitter<void>();

  private service: UnidadeNegocioService = inject(UnidadeNegocioService);
  private messages: MessageService = inject(MessageService);

  titulo = $localize`Unidade de Negócio: `;

  acoesTela: RegisterActionToolbar[] = [];
  private auth: AuthService = inject(AuthService);

  ngOnInit(): void {
    this.initForm();

    const canEdit = this.auth.hasAuthorityEditarToModulo(
      'CADASTRO_UNIDADE_NEGOCIO'
    );

    this.acoesTela = [
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
      this.acoesTela.push({
        action: () => {
          this.salvar();
        },
        icon: 'save',
        title: $localize`Salvar` + ' (enter)',
        shortcut: 'enter',
      });
    }

    if (this.id === RouteConstants.P_ADD) {
      this.modoEdicao = false;
      this.titulo += $localize`Nova`;
      this.unidadeNegocio = {
        ativa: true,
      } as UnidadeNegocioDTO;
      this.fillForm();
    } else {
      this.modoEdicao = true;
      this.service.findById(String(this.id!)).subscribe((response) => {
        this.unidadeNegocio = response.body;
        this.titulo += this.unidadeNegocio.nome;
        this.fillForm();
        // Desabilita o campo código em modo edição
        this.form.get('codigo')?.disable();
      });
    }
  }

  initForm() {
    const fb = new FormBuilder().nonNullable;
    this.form.addControl('codigo', fb.control(null));
    this.form.addControl('nome', fb.control(null));
    this.form.addControl('cnpj', fb.control(null));
    this.form.addControl('descricao', fb.control(null));
    this.form.addControl('ativa', fb.control(true));
  }

  fillForm() {
    this.form.get('codigo')?.setValue(this.unidadeNegocio.codigo);
    this.form.get('nome')?.setValue(this.unidadeNegocio.nome);
    this.form.get('cnpj')?.setValue(this.unidadeNegocio.cnpj);
    this.form.get('descricao')?.setValue(this.unidadeNegocio.descricao);
    this.form.get('ativa')?.setValue(this.unidadeNegocio.ativa);
  }

  salvar() {
    if (!this.form.valid) {
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    this.unidadeNegocio.codigo = this.form.getRawValue().codigo;
    this.unidadeNegocio.nome = this.form.value.nome;
    this.unidadeNegocio.cnpj = this.form.value.cnpj;
    this.unidadeNegocio.descricao = this.form.value.descricao;
    this.unidadeNegocio.ativa = this.form.value.ativa;

    this.service.save(this.unidadeNegocio, {
      onSuccess: (data: UnidadeNegocioDTO) => {
        this.unidadeNegocio = data;
        this.messages.sucesso($localize`Unidade de Negócio salva com sucesso.`);
        this.goBackFn();
      },
    });
  }

  isControlInvalid(campo: string) {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const fc: AbstractControl<any, any> | null = this.form.get(campo);

    if (fc !== null && fc.invalid && (fc.touched || fc.dirty)) {
      return true;
    }

    return false;
  }

  goBackFn = () => {
    this.closeDetailEvent.emit();
  };
}
