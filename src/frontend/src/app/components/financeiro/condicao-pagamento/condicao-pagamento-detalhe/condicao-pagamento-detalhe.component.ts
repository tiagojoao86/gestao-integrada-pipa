import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { RouteConstants } from '../../../base/constants/route-constants';
import { CondicaoPagamentoService } from '../condicao-pagamento.service';
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
import { CheckboxModule } from 'primeng/checkbox';
import { MessageService } from '../../../base/messages/messages.service';
import { CondicaoPagamentoDTO } from '../model/condicao-pagamento.dto';
import { AuthService } from '../../../base/auth/auth-service';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';

@Component({
  selector: 'gi-condicao-pagamento-detalhe',
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    TextareaModule,
    CheckboxModule,
  ],
  templateUrl: './condicao-pagamento-detalhe.component.html',
  styleUrls: ['./condicao-pagamento-detalhe.component.css'],
  providers: [CondicaoPagamentoService],
})
export class CondicaoPagamentoDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup([]);
  editMode = false;
  condicaoPagamento: CondicaoPagamentoDTO = {} as CondicaoPagamentoDTO;
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  private service: CondicaoPagamentoService = inject(CondicaoPagamentoService);
  private messages: MessageService = inject(MessageService);

  title = $localize`Condição de Pagamento: `;

  toolbarActions: ToolbarActionModel[] = [];
  private auth: AuthService = inject(AuthService);

  ngOnInit(): void {
    this.initForm();

    const canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.FINANCEIRO_CONDICAO_PAGAMENTO
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
      this.condicaoPagamento = {
        ativo: true,
      } as CondicaoPagamentoDTO;
      this.fillForm();
    } else {
      this.editMode = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.condicaoPagamento = response.body!;
        this.title += this.condicaoPagamento.condicao;
        this.fillForm();
      });
    }
  }

  initForm() {
    const fb = new FormBuilder().nonNullable;
    this.form.addControl('condicao', fb.control(''));
    this.form.addControl('ativo', fb.control(true));
    this.form.addControl('descricao', fb.control(''));
  }

  fillForm() {
    this.form.get('condicao')?.setValue(this.condicaoPagamento.condicao || '');
    this.form.get('ativo')?.setValue(this.condicaoPagamento.ativo ?? true);
    this.form
      .get('descricao')
      ?.setValue(this.condicaoPagamento.descricao || '');
  }

  salvar() {
    if (!this.form.valid) {
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    this.condicaoPagamento.condicao = this.form.value.condicao;
    this.condicaoPagamento.ativo = this.form.value.ativo;
    this.condicaoPagamento.descricao = this.form.value.descricao;

    this.service.save(this.condicaoPagamento, {
      onSuccess: (data: CondicaoPagamentoDTO) => {
        this.condicaoPagamento = data;
        this.messages.sucesso($localize`Condição de pagamento salva com sucesso.`);
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
