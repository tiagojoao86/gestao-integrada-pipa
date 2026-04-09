import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { RouteConstants } from '../../../base/constants/route-constants';
import { PessoaService } from '../pessoa.service';
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
import { MessageService } from '../../../base/messages/messages.service';
import { PessoaDTO, TipoPessoa } from '../model/pessoa-dto';
import { AuthService } from '../../../base/auth/auth-service';
import { RadioButtonModule } from 'primeng/radiobutton';
import { CheckboxModule } from 'primeng/checkbox';
import { DatePickerModule } from 'primeng/datepicker';
import { TextareaModule } from 'primeng/textarea';
import { InputMaskModule } from 'primeng/inputmask';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import { EntitySearchService } from '../../../base/entity-search/entity-search.service';
import {
  EntitySearchConfig,
  SearchField,
  ResultField,
} from '../../../base/entity-search/entity-search.model';
import { EntityFieldComponent } from '../../../base/entity-field/entity-field.component';

@Component({
  selector: 'gi-pessoa-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    RadioButtonModule,
    CheckboxModule,
    DatePickerModule,
    TextareaModule,
    InputMaskModule,
    EntityFieldComponent,
  ],
  templateUrl: './pessoa-detalhe.component.html',
  styleUrl: './pessoa-detalhe.component.css',
  providers: [PessoaService],
})
export class PessoaDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup([]);
  editMode = false;
  pessoa: PessoaDTO = {} as PessoaDTO;
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  private service: PessoaService = inject(PessoaService);
  private messages: MessageService = inject(MessageService);
  private entitySearchService: EntitySearchService = inject(EntitySearchService);

  titulo = $localize`Pessoa: `;

  toolbarActions: ToolbarActionModel[] = [];
  private auth: AuthService = inject(AuthService);
  tiposPessoa = TipoPessoa.getList();
  responsavelSelecionado: PessoaDTO | null = null;
  readonly responsavelLabel = $localize`Responsável`;

  ngOnInit(): void {
    this.initForm();

    // configure actions based on permission
    const canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.CADASTRO_PESSOA
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
          this.savePessoa();
        },
        icon: 'save',
        title: $localize`Salvar` + ' (enter)',
        shortcut: 'enter',
      });
    }

    if (this.detailId === RouteConstants.P_ADD) {
      this.editMode = false;
      this.titulo += $localize`Nova`;
      this.pessoa = {
        tipoPessoa: TipoPessoa.FISICA,
        ativa: true,
      } as PessoaDTO;
      this.fillForm();
    } else {
      this.editMode = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.pessoa = response.body!;
        this.titulo += this.pessoa.nome;
        this.fillForm();
        // Desabilita o campo tipoPessoa em modo edição
        this.form.get('tipoPessoa')?.disable();
      });
    }
  }

  initForm() {
    const fb = new FormBuilder().nonNullable;
    this.form.addControl('tipoPessoa', fb.control(TipoPessoa.FISICA));
    this.form.addControl('nome', fb.control(null));
    this.form.addControl('email', fb.control(null));
    this.form.addControl('telefone', fb.control(null));
    this.form.addControl('observacoes', fb.control(null));
    this.form.addControl('ativa', fb.control(true));

    // Pessoa Física
    this.form.addControl('cpf', fb.control(null));
    this.form.addControl('dataNascimento', fb.control(null));

    // Pessoa Jurídica
    this.form.addControl('cnpj', fb.control(null));
    this.form.addControl('razaoSocial', fb.control(null));
    this.form.addControl('inscricaoEstadual', fb.control(null));
  }

  fillForm() {
    this.form
      .get('tipoPessoa')
      ?.setValue(this.pessoa.tipoPessoa || TipoPessoa.FISICA);
    this.form.get('nome')?.setValue(this.pessoa.nome);
    this.form.get('email')?.setValue(this.pessoa.email);
    this.form.get('telefone')?.setValue(this.pessoa.telefone);
    this.form.get('observacoes')?.setValue(this.pessoa.observacoes);
    this.form.get('ativa')?.setValue(this.pessoa.ativa);

    // Pessoa Física
    this.form.get('cpf')?.setValue(this.pessoa.cpf);
    if (this.pessoa.dataNascimento) {
      this.form
        .get('dataNascimento')
        ?.setValue(new Date(this.pessoa.dataNascimento));
    }

    // Pessoa Jurídica
    this.form.get('cnpj')?.setValue(this.pessoa.cnpj);
    this.form.get('razaoSocial')?.setValue(this.pessoa.razaoSocial);
    this.form.get('inscricaoEstadual')?.setValue(this.pessoa.inscricaoEstadual);

    // Responsável
    if (this.pessoa.responsavelId) {
      this.responsavelSelecionado = { id: this.pessoa.responsavelId, nome: this.pessoa.responsavelNome } as PessoaDTO;
    }
  }

  isFisica(): boolean {
    return this.form.get('tipoPessoa')?.value === TipoPessoa.FISICA;
  }

  isJuridica(): boolean {
    return this.form.get('tipoPessoa')?.value === TipoPessoa.JURIDICA;
  }

  onTipoPessoaChange() {
    // Limpa campos ao trocar tipo
    if (this.isFisica()) {
      this.form.get('cnpj')?.setValue(null);
      this.form.get('razaoSocial')?.setValue(null);
      this.form.get('inscricaoEstadual')?.setValue(null);
    } else {
      this.form.get('cpf')?.setValue(null);
      this.form.get('dataNascimento')?.setValue(null);
    }
  }

  savePessoa() {
    if (!this.form.valid) {
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    this.pessoa.tipoPessoa = this.form.value.tipoPessoa;
    this.pessoa.nome = this.form.value.nome;
    this.pessoa.email = this.form.value.email;
    this.pessoa.telefone = this.form.value.telefone;
    this.pessoa.observacoes = this.form.value.observacoes;
    this.pessoa.ativa = this.form.value.ativa;

    if (this.isFisica()) {
      this.pessoa.cpf = this.form.value.cpf;
      this.pessoa.dataNascimento = this.form.value.dataNascimento;
      this.pessoa.responsavelId = this.responsavelSelecionado?.id ?? undefined;
      // Limpa campos de PJ
      this.pessoa.cnpj = undefined;
      this.pessoa.razaoSocial = undefined;
      this.pessoa.inscricaoEstadual = undefined;
    } else {
      this.pessoa.cnpj = this.form.value.cnpj;
      this.pessoa.razaoSocial = this.form.value.razaoSocial;
      this.pessoa.inscricaoEstadual = this.form.value.inscricaoEstadual;
      // Limpa campos de PF e responsável (não se aplica a PJ)
      this.pessoa.cpf = undefined;
      this.pessoa.dataNascimento = undefined;
      this.pessoa.responsavelId = undefined;
    }

    this.service.save(this.pessoa, {
      onSuccess: (data: PessoaDTO) => {
        this.pessoa = data;
        this.messages.sucesso($localize`Pessoa salva com sucesso.`);
        this.goBackFn();
      },
    });
  }

  pesquisarResponsavel(): void {
    const searchFields: SearchField[] = [
      { key: 'nome', label: $localize`Nome` },
      { key: 'cpf', label: $localize`CPF` },
    ];
    const resultFields: ResultField[] = [
      { key: 'nome', label: $localize`Nome` },
      { key: 'cpf', label: $localize`CPF` },
      { key: 'telefone', label: $localize`Telefone` },
    ];
    const config: EntitySearchConfig<PessoaDTO> = {
      service: this.service,
      searchFields,
      resultFields,
      title: $localize`Pesquisar Responsável`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.responsavelSelecionado = result.entity;
      }
    });
  }

  limparResponsavel(): void {
    this.responsavelSelecionado = null;
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
