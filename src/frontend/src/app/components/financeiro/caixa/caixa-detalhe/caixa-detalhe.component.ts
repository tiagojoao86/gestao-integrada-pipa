import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { RouteConstants } from '../../../base/constants/route-constants';
import { CaixaService } from '../caixa.service';
import { BaseComponent } from '../../../base/base.component';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { IftaLabelModule } from 'primeng/iftalabel';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  FormsModule,
  Validators,
} from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { CheckboxModule } from 'primeng/checkbox';
import { SelectModule } from 'primeng/select';
import { MessageService } from '../../../base/messages/messages.service';
import { CaixaDTO } from '../model/caixa-dto';
import { AuthService } from '../../../base/auth/auth-service';
import { UnidadeNegocioService } from '../../../cadastro/unidade-negocio/unidade-negocio.service';

@Component({
  selector: 'gi-caixa-detalhe',
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    InputNumberModule,
    CheckboxModule,
    SelectModule,
  ],
  templateUrl: './caixa-detalhe.component.html',
  styleUrl: './caixa-detalhe.component.css',
  providers: [CaixaService, UnidadeNegocioService],
})
export class CaixaDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup([]);
  caixa: CaixaDTO = {} as CaixaDTO;
  unidades: { id: string; nome: string; codigo: string }[] = [];

  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  private service = inject(CaixaService);
  private messages = inject(MessageService);
  private auth = inject(AuthService);
  private unidadeNegocioService = inject(UnidadeNegocioService);

  title = $localize`Cadastro de Caixas: `;
  toolbarActions: ToolbarActionModel[] = [];

  ngOnInit(): void {
    this.initForm();
    this.loadUnidades();

    const canEdit = this.auth.hasAuthorityEditarToModulo('CADASTRO_CAIXA');

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
        action: () => this.salvar(),
        icon: 'save',
        title: $localize`Salvar` + ' (enter)',
        shortcut: 'enter',
      });
    }

    if (this.detailId === RouteConstants.P_ADD) {
      this.title += $localize`Novo`;
      this.caixa = { ativo: true, valorPadraoAbertura: 0 } as CaixaDTO;
      this.fillForm();
    } else {
      this.title += $localize`Editar`;
      this.loadCaixa();
    }
  }

  initForm(): void {
    const fb = new FormBuilder().nonNullable;
    this.form = new FormGroup({
      nome: fb.control('', [Validators.required, Validators.maxLength(150)]),
      valorPadraoAbertura: fb.control(0, [Validators.required]),
      percentualPagamentoParcial: fb.control<number | null>(null),
      valorMinimoParcela: fb.control<number | null>(null),
      ativo: fb.control(true),
      unidadeNegocioId: fb.control<string | null>(null, [Validators.required]),
    });
  }

  loadUnidades(): void {
    this.unidadeNegocioService.listarParaVinculo().subscribe((list) => {
      this.unidades = list;
    });
  }

  loadCaixa(): void {
    this.service.findById(this.detailId as string).subscribe((response) => {
      if (response.body) {
        this.caixa = response.body;
        this.fillForm();
      }
    });
  }

  fillForm(): void {
    this.form.patchValue({
      nome: this.caixa.nome ?? '',
      valorPadraoAbertura: this.caixa.valorPadraoAbertura ?? 0,
      percentualPagamentoParcial: this.caixa.percentualPagamentoParcial ?? null,
      valorMinimoParcela: this.caixa.valorMinimoParcela ?? null,
      ativo: this.caixa.ativo ?? true,
      unidadeNegocioId: this.caixa.unidadeNegocioId ?? null,
    });
  }

  populateDTOBeforeSend(): void {
    const v = this.form.value;
    this.caixa.nome = v.nome;
    this.caixa.valorPadraoAbertura = v.valorPadraoAbertura;
    this.caixa.percentualPagamentoParcial = v.percentualPagamentoParcial ?? undefined;
    this.caixa.valorMinimoParcela = v.valorMinimoParcela ?? undefined;
    this.caixa.ativo = v.ativo;
    this.caixa.unidadeNegocioId = v.unidadeNegocioId ?? undefined;
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.populateDTOBeforeSend();
    this.service.save(this.caixa, {
      onSuccess: () => {
        this.messages.sucesso($localize`Caixa salvo com sucesso.`);
        this.goBackFn();
      },
    });
  }

  isControlInvalid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  goBackFn(): void {
    this.closeDetail.emit();
  }
}
