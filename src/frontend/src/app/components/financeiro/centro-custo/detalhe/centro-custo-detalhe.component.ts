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
import { SelectModule } from 'primeng/select';
import { CheckboxModule } from 'primeng/checkbox';
import { MessageService } from '../../../base/messages/messages.service';
import { CentroCustoService } from '../centro-custo.service';
import { CentroCustoDTO } from '../model/centro-custo-dto';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { AuthService } from '../../../base/auth/auth-service';
import { Response } from '../../../base/model/response';
import { UsuarioUnidadeNegocioDTO } from '../../../cadastro/usuario/model/usuario-unidade-negocio-dto';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';

@Component({
  selector: 'gi-centro-custo-detalhe',
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    SelectModule,
    CheckboxModule,
  ],
  templateUrl: './centro-custo-detalhe.component.html',
  styleUrl: './centro-custo-detalhe.component.css',
  providers: [CentroCustoService],
})
export class CentroCustoDetalheComponent implements OnInit {
  @Input() detailId?: string | number;
  @Output() closeDetail = new EventEmitter<void>();

  form: FormGroup = new FormGroup({});
  titulo = $localize`Centro de Custo`;

  toolbarActions: ToolbarActionModel[] = [];
  unidadesNegocioOptions: UsuarioUnidadeNegocioDTO[] = [];

  private fb = inject(FormBuilder);
  private service = inject(CentroCustoService);
  private auth = inject(AuthService);
  private messages = inject(MessageService);

  ngOnInit(): void {
    this.initForm();

    // configure actions based on permission
    const canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.FINANCEIRO_CENTRO_CUSTO
    );
    this.toolbarActions = [
      {
        action: () => this.goBackFn(),
        icon: 'close',
        title: $localize`Cancelar`,
      },
    ];

    if (canEdit) {
      this.toolbarActions.push({
        action: () => this.save(),
        icon: 'save',
        title: $localize`Salvar`,
      });
    }

    // seta unidade de negocio default do usuário
    this.setDefaultUnidadeNegocio();

    if (this.detailId && this.detailId !== 'add') {
      this.service
        .findById(String(this.detailId))
        .subscribe((response: Response<CentroCustoDTO>) => {
          const dto = (response.body ?? response) as CentroCustoDTO;
          this.form.patchValue(dto);
        });
    }

    this.unidadesNegocioOptions = this.auth.getUnidadesNegocio();
  }

  setDefaultUnidadeNegocio(): void {
    const defaultUnidade = this.auth.getDefaultUnidadeNegocio();
    if (defaultUnidade) {
      this.form.patchValue({
        unidadeNegocioId: defaultUnidade.unidadeNegocioId,
      });
    }
  }

  initForm() {
    const nonNull = this.fb.nonNullable;
    this.form = nonNull.group({
      nome: nonNull.control('', [
        Validators.required,
        Validators.maxLength(200),
      ]),
      centroResultado: nonNull.control(false),
      unidadeNegocioId: nonNull.control('', [Validators.required]),
    });
  }

  isControlInvalid(name: string) {
    const c: AbstractControl | null = this.form.get(name);
    return !!(c && c.invalid && (c.dirty || c.touched));
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }
    const dto: CentroCustoDTO = this.form.getRawValue();
    if (this.detailId && this.detailId !== 'add')
      dto.id = String(this.detailId);

    // Use BaseService.save with callback handlers instead of subscribing
    // diretamente. For save, provide only `onSuccess` — errors are handled
    // internamente pelo `BaseService`/`MessageService` implementation.
    this.service.save(dto, {
      onSuccess: () => this.closeDetail.emit(),
    });
  }

  goBackFn() {
    this.closeDetail.emit();
  }
}
