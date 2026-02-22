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
import { TextareaModule } from 'primeng/textarea';
import { MessageService } from '../../../base/messages/messages.service';
import { SetorService } from '../setor.service';
import { SetorDTO } from '../model/setor-dto';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { AuthService } from '../../../base/auth/auth-service';
import { Response } from '../../../base/model/response';
import { CentroCustoService } from '../../../financeiro/centro-custo/centro-custo.service';
import { CentroCustoGridDTO } from '../../../financeiro/centro-custo/model/centro-custo-grid-dto';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import { PageRequest } from '../../../base/model/page-request';

@Component({
  selector: 'gi-setor-detalhe',
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    SelectModule,
    TextareaModule,
  ],
  templateUrl: './setor-detalhe.component.html',
  styleUrl: './setor-detalhe.component.css',
  providers: [SetorService, CentroCustoService],
})
export class SetorDetalheComponent implements OnInit {
  @Input() detailId?: string | number;
  @Output() closeDetail = new EventEmitter<void>();

  form: FormGroup = new FormGroup({});
  titulo = $localize`Setor`;

  toolbarActions: ToolbarActionModel[] = [];
  centrosCustoOptions: CentroCustoGridDTO[] = [];

  private fb = inject(FormBuilder);
  private service = inject(SetorService);
  private centroCustoService = inject(CentroCustoService);
  private auth = inject(AuthService);
  private messages = inject(MessageService);

  ngOnInit(): void {
    this.initForm();

    // configure actions based on permission
    const canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.CADASTRO_SETOR
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

    // Carregar centros de custo
    this.loadCentrosCusto();

    if (this.detailId && this.detailId !== 'add') {
      this.service
        .findById(String(this.detailId))
        .subscribe((response: Response<SetorDTO>) => {
          const dto = (response.body ?? response) as SetorDTO;
          this.form.patchValue(dto);
        });
    }
  }

  loadCentrosCusto(): void {
    this.centroCustoService.listAll(PageRequest.empty()).subscribe((response) => {
      this.centrosCustoOptions = response.body ?? [];
    });
  }

  initForm() {
    const nonNull = this.fb.nonNullable;
    this.form = nonNull.group({
      nome: nonNull.control('', [
        Validators.required,
        Validators.maxLength(200),
      ]),
      descricao: nonNull.control('', [Validators.maxLength(500)]),
      centroCustoId: nonNull.control('', [Validators.required]),
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
    const dto: SetorDTO = this.form.getRawValue();
    if (this.detailId && this.detailId !== 'add')
      dto.id = String(this.detailId);

    this.service.save(dto, {
      onSuccess: () => this.closeDetail.emit(),
    });
  }

  goBackFn() {
    this.closeDetail.emit();
  }
}
