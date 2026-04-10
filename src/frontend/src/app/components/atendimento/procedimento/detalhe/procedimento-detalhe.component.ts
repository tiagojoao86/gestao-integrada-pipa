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
import { ProcedimentoService } from '../procedimento.service';
import { ProcedimentoDTO } from '../model/procedimento-dto';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { AuthService } from '../../../base/auth/auth-service';
import { RouteConstants } from '../../../base/constants/route-constants';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';

@Component({
  selector: 'gi-procedimento-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    CheckboxModule,
  ],
  templateUrl: './procedimento-detalhe.component.html',
  styleUrl: './procedimento-detalhe.component.css',
  providers: [ProcedimentoService],
})
export class ProcedimentoDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup({});
  editMode = false;
  procedimento: ProcedimentoDTO = new ProcedimentoDTO();
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  titulo = $localize`Procedimento: `;
  toolbarActions: ToolbarActionModel[] = [];

  private fb = inject(FormBuilder);
  private service = inject(ProcedimentoService);
  private messages = inject(MessageService);
  private auth = inject(AuthService);

  ngOnInit(): void {
    this.initForm();

    const canEdit = this.auth.hasAuthorityEditarToModulo(SystemModuleKey.ATENDIMENTO_PROCEDIMENTO);

    this.toolbarActions = [
      { action: () => this.goBackFn(), icon: 'close', title: $localize`Cancelar` + ' (esc)', shortcut: 'escape' },
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
      this.titulo += $localize`Novo`;
      this.fillForm();
    } else {
      this.editMode = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.procedimento = response.body!;
        this.titulo += this.procedimento.codigo;
        this.fillForm();
      });
    }
  }

  initForm() {
    const fb = this.fb.nonNullable;
    this.form = fb.group({
      codigo: fb.control('', [Validators.required, Validators.maxLength(30)]),
      descricao: fb.control('', [Validators.required, Validators.maxLength(200)]),
      codigoTiss: fb.control('', [Validators.maxLength(20)]),
      codigoTuss: fb.control('', [Validators.maxLength(20)]),
      ativo: fb.control(true),
    });
  }

  fillForm() {
    this.form.get('codigo')?.setValue(this.procedimento.codigo ?? '');
    this.form.get('descricao')?.setValue(this.procedimento.descricao ?? '');
    this.form.get('codigoTiss')?.setValue(this.procedimento.codigoTiss ?? '');
    this.form.get('codigoTuss')?.setValue(this.procedimento.codigoTuss ?? '');
    this.form.get('ativo')?.setValue(this.procedimento.ativo ?? true);
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    const raw = this.form.getRawValue();
    this.procedimento.codigo = raw.codigo;
    this.procedimento.descricao = raw.descricao;
    this.procedimento.codigoTiss = raw.codigoTiss || undefined;
    this.procedimento.codigoTuss = raw.codigoTuss || undefined;
    this.procedimento.ativo = raw.ativo;

    this.service.save(this.procedimento, {
      onSuccess: () => {
        this.messages.sucesso($localize`Procedimento salvo com sucesso.`);
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
