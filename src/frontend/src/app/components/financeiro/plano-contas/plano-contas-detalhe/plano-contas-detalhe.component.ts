import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';

import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  FormsModule,
} from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { IftaLabelModule } from 'primeng/iftalabel';
import { CheckboxModule } from 'primeng/checkbox';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { SelectModule } from 'primeng/select';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import {
  RegisterActionToolbar,
  BaseComponent,
} from '../../../base/base.component';
import { PlanoContasDTO } from '../model/plano-contas-dto';
import { PlanoContasService } from '../plano-contas.service';
import { TipoPlanoContas } from '../model/tipo-plano-contas.enum';
import { AuthService } from '../../../base/auth/auth-service';
import { MessageService } from '../../../base/messages/messages.service';
import { PageRequest } from '../../../base/model/page-request';
import {
  FilterItem,
  FilterLogicOperator,
  FilterOperator,
} from '../../../base/model/filter-dto';

@Component({
  selector: 'gi-plano-contas-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    CheckboxModule,
    AutoCompleteModule,
    SelectModule,
    IconFieldModule,
    InputIconModule,
  ],
  providers: [PlanoContasService],
  templateUrl: './plano-contas-detalhe.component.html',
  styleUrl: './plano-contas-detalhe.component.css',
})
export class PlanoContasDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup([]);
  modoEdicao = false;
  entity: PlanoContasDTO = this.createEmptyEntity();

  @Input() detailId!: string;
  @Output() closeDetail = new EventEmitter<void>();

  titulo: string =
    $localize`:@@planoContas.details:Detalhes do Plano de Contas` + ': ';

  authService = inject(AuthService);
  messageService = inject(MessageService);
  planoContasService = inject(PlanoContasService);

  tiposList = TipoPlanoContas.getList().map((tipo) => ({
    label: tipo.getLabel(),
    value: tipo.getKey(),
  }));

  planosPaiSuggestions: PlanoContasDTO[] = [];
  selectedPlanoPai: PlanoContasDTO | null = null;

  allUnidadesNegocio: { id: string; nome: string; codigo: string }[] = [];

  acoesTela: RegisterActionToolbar[] = [];

  createEmptyEntity(): PlanoContasDTO {
    return {
      codigo: '',
      descricao: '',
      tipo: '',
      ativo: true,
    };
  }

  ngOnInit(): void {
    this.initForm();

    this.acoesTela = [
      {
        action: () => {
          this.goBackFn();
        },
        icon: 'close',
        title: $localize`:@@common.cancel:Cancelar` + ' (esc)',
        shortcut: 'escape',
      },
    ];

    const canEdit = this.authService.hasAuthorityEditarToModulo(
      'CADASTRO_PLANO_CONTAS'
    );
    if (canEdit) {
      this.acoesTela.push({
        action: () => {
          this.onSave();
        },
        icon: 'save',
        title: $localize`:@@common.save:Salvar` + ' (enter)',
        shortcut: 'enter',
      });
    }

    if (this.detailId === 'add') {
      this.modoEdicao = false;
      this.titulo += $localize`:@@common.new:Novo`;
      this.entity = this.createEmptyEntity();
      // Load unidades and set default after loading
      this.loadUnidadesNegocio(true);
    } else {
      this.modoEdicao = true;
      this.planoContasService.findById(this.detailId).subscribe({
        next: (response) => {
          this.entity = response.body;
          this.titulo += this.entity.descricao;
          this.fillForm();
          // Se está editando e tem planoPaiId, carrega o plano pai completo
          if (this.entity.planoPaiId) {
            this.planoContasService.findById(this.entity.planoPaiId).subscribe({
              next: (response) => {
                const planoPai = response.body;
                this.selectedPlanoPai = {
                  ...planoPai,
                  displayLabel: `${planoPai.codigo} - ${planoPai.descricao}`,
                } as PlanoContasDTO & { displayLabel: string };
              },
            });
          }
        },
      });
    }
  }

  initForm() {
    const fb = new FormBuilder().nonNullable;
    this.form.addControl('codigo', fb.control(null));
    this.form.addControl('descricao', fb.control(null));
    this.form.addControl('tipo', fb.control(null));
    this.form.addControl('ativo', fb.control(true));
    this.form.addControl('unidadeNegocio', fb.control(''));
  }

  fillForm() {
    this.form.get('codigo')?.setValue(this.entity.codigo);
    this.form.get('descricao')?.setValue(this.entity.descricao);
    this.form.get('tipo')?.setValue(this.entity.tipo);
    this.form.get('ativo')?.setValue(this.entity.ativo);
    this.form
      .get('unidadeNegocio')
      ?.setValue(this.entity.unidadeNegocioId || '');
  }

  onSave(): void {
    if (!this.form.valid) {
      this.messageService.erro(
        $localize`:@@common.invalidFields:Existem campos inválidos.`
      );
      return;
    }

    const unidadeNegocioId = this.form.value.unidadeNegocio;
    if (!unidadeNegocioId) {
      this.messageService.erro(
        $localize`:@@planoContas.unidadeNegocioObrigatoria:Unidade de negócio é obrigatória`
      );
      return;
    }

    this.entity.unidadeNegocioId = unidadeNegocioId;
    this.entity.codigo = this.form.value.codigo;
    this.entity.descricao = this.form.value.descricao;
    this.entity.tipo = this.form.value.tipo;
    this.entity.ativo = this.form.value.ativo;

    // Atualiza o planoPaiId baseado na seleção do autocomplete
    if (this.selectedPlanoPai) {
      this.entity.planoPaiId = this.selectedPlanoPai.id;
      this.entity.planoPaiDescricao = this.selectedPlanoPai.descricao;
    } else {
      this.entity.planoPaiId = undefined;
      this.entity.planoPaiDescricao = undefined;
    }

    this.planoContasService.save(this.entity, {
      onSuccess: () => {
        const message = this.entity.id
          ? $localize`:@@common.updateSuccess:Registro atualizado com sucesso`
          : $localize`:@@common.createSuccess:Registro criado com sucesso`;
        this.messageService.sucesso(message);
        this.closeDetail.emit();
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

  searchPlanoPai(event: { query: string }): void {
    const query = event.query.toLowerCase();

    // Busca planos do mesmo tipo, excluindo o próprio plano (se estiver editando)
    const filters: FilterItem[] = [];

    if (this.entity.tipo) {
      filters.push({
        property: 'tipo',
        operator: FilterOperator.EQ.key,
        values: [this.entity.tipo],
      });
    }

    if (query) {
      filters.push({
        property: 'descricao',
        operator: FilterOperator.CONTAINS.key,
        values: [query],
      });
    }

    const pageRequest = new PageRequest(
      { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: filters },
      20,
      0,
      []
    );

    this.planoContasService.list(pageRequest).subscribe({
      next: (response) => {
        if (response.body) {
          // Filtra para excluir o próprio plano se estiver editando
          this.planosPaiSuggestions = response.body.content
            .filter((plano: PlanoContasDTO) => plano.id !== this.entity.id)
            .map((plano: PlanoContasDTO) => ({
              ...plano,
              displayLabel: `${plano.codigo} - ${plano.descricao}`,
            }));
        }
      },
    });
  }
  onTipoChange(): void {
    // Quando o tipo muda, limpa a seleção do plano pai
    // pois o plano pai deve ser do mesmo tipo
    this.selectedPlanoPai = null;
    this.entity.planoPaiId = undefined;
    this.entity.planoPaiDescricao = undefined;
  }

  loadUnidadesNegocio(setDefault = false): void {
    // First, load default unidade from auth cache to ensure it's available immediately
    const defaultUnidade = this.authService.getDefaultUnidadeNegocio();
    if (defaultUnidade) {
      this.allUnidadesNegocio = [defaultUnidade];
      // Set default immediately if needed
      if (setDefault && defaultUnidade.id) {
        this.form.get('unidadeNegocio')?.setValue(defaultUnidade.id);
      }
    }

    // Then load all available unidades from backend
    this.planoContasService.listarUnidadesDisponiveis().subscribe({
      next: (unidades) => {
        this.allUnidadesNegocio = unidades;
        // Set default after backend load if needed and not already set
        if (
          setDefault &&
          defaultUnidade &&
          !this.form.get('unidadeNegocio')?.value
        ) {
          this.form.get('unidadeNegocio')?.setValue(defaultUnidade.id);
        }
      },
      error: (error) => {
        console.error('Erro ao carregar unidades de negócio:', error);
      },
    });
  }

  goBackFn(): void {
    this.closeDetail.emit();
  }
}
