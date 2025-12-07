import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { RouteConstants } from '../../../base/constants/route-constants';
import { ContaBancariaService } from '../conta-bancaria.service';
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
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { MessageService } from '../../../base/messages/messages.service';
import { ContaBancariaDTO } from '../model/conta-bancaria-dto';
import { AuthService } from '../../../base/auth/auth-service';
import { TipoConta } from '../model/tipo-conta.enum';
import { CheckboxModule } from 'primeng/checkbox';

@Component({
  selector: 'gi-conta-bancaria-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    CheckboxModule
],
  templateUrl: './conta-bancaria-detalhe.component.html',
  styleUrl: './conta-bancaria-detalhe.component.css',
  providers: [ContaBancariaService],
})
export class ContaBancariaDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup([]);
  modoEdicao = false;
  contaBancaria: ContaBancariaDTO = {} as ContaBancariaDTO;
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  private service: ContaBancariaService = inject(ContaBancariaService);
  private messages: MessageService = inject(MessageService);

  titulo = $localize`Conta Bancária: `;

  tiposContaOptions = TipoConta.getList().map((tipo) => ({
    label: tipo.getLabel(),
    value: tipo.getKey(),
  }));

  acoesTela: RegisterActionToolbar[] = [];
  private auth: AuthService = inject(AuthService);

  ngOnInit(): void {
    this.initForm();

    // configure actions based on permission
    const canEdit = this.auth.hasAuthorityEditarToModulo(
      'CADASTRO_CONTA_BANCARIA'
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

    if (this.detailId === RouteConstants.P_ADD) {
      this.modoEdicao = false;
      this.titulo += $localize`Nova`;
      this.contaBancaria = {
        nome: '',
        tipo: 'CORRENTE',
        ativa: true,
      } as ContaBancariaDTO;
      this.fillForm();
    } else {
      this.modoEdicao = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.contaBancaria = response.body;
        this.titulo += this.contaBancaria.nome;
        this.fillForm();
      });
    }
  }

  initForm() {
    const fb = new FormBuilder().nonNullable;
    this.form.addControl('nome', fb.control(''));
    this.form.addControl('banco', fb.control(''));
    this.form.addControl('agencia', fb.control(''));
    this.form.addControl('numeroConta', fb.control(''));
    this.form.addControl('tipo', fb.control('CORRENTE'));
    this.form.addControl('saldoInicial', fb.control(0));
    this.form.addControl('ativa', fb.control(true));
  }

  fillForm() {
    this.form.get('nome')?.setValue(this.contaBancaria.nome || '');
    this.form.get('banco')?.setValue(this.contaBancaria.banco || '');
    this.form.get('agencia')?.setValue(this.contaBancaria.agencia || '');
    this.form
      .get('numeroConta')
      ?.setValue(this.contaBancaria.numeroConta || '');
    this.form.get('tipo')?.setValue(this.contaBancaria.tipo || 'CORRENTE');
    this.form
      .get('saldoInicial')
      ?.setValue(this.contaBancaria.saldoInicial || 0);
    this.form.get('ativa')?.setValue(this.contaBancaria.ativa ?? true);
  }

  salvar() {
    if (!this.form.valid) {
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    this.contaBancaria.nome = this.form.value.nome;
    this.contaBancaria.banco = this.form.value.banco;
    this.contaBancaria.agencia = this.form.value.agencia;
    this.contaBancaria.numeroConta = this.form.value.numeroConta;
    this.contaBancaria.tipo = this.form.value.tipo;
    this.contaBancaria.saldoInicial = this.form.value.saldoInicial;
    this.contaBancaria.ativa = this.form.value.ativa;

    this.service.save(this.contaBancaria, {
      onSuccess: (data: ContaBancariaDTO) => {
        this.contaBancaria = data;
        this.messages.sucesso($localize`Conta bancária salva com sucesso.`);
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
