import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { RouteConstants } from '../../../base/constants/route-constants';
import { UsuarioService } from '../usuario.service';
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
import { PasswordModule } from 'primeng/password';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { CheckboxModule } from 'primeng/checkbox';
import { RadioButtonModule } from 'primeng/radiobutton';
import { MessageService } from '../../../base/messages/messages.service';
import { UsuarioDTO } from '../model/usuario-dto';
import { PerfilDTO } from '../../perfil/model/perfil-dto';
import { PerfilParaVinculoDTO } from '../../perfil/model/perfil-para-vinculo-dto';
import { UnidadeNegocioDTO } from '../../unidade-negocio/model/unidade-negocio-dto';
import { UsuarioUnidadeNegocioDTO } from '../model/usuario-unidade-negocio-dto';
import { AuthService } from '../../../base/auth/auth-service';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';

@Component({
  selector: 'gi-usuario-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    PasswordModule,
    AutoCompleteModule,
    IconFieldModule,
    InputIconModule,
    CheckboxModule,
    RadioButtonModule,
  ],
  templateUrl: './usuario-detalhe.component.html',
  styleUrl: './usuario-detalhe.component.css',
  providers: [UsuarioService],
})
export class UsuarioDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup([]);
  editMode = false;
  usuario: UsuarioDTO = {} as UsuarioDTO;
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  private service: UsuarioService = inject(UsuarioService);
  private messages: MessageService = inject(MessageService);

  titulo = $localize`Usuário: `;

  allPerfis: PerfilParaVinculoDTO[] = [];
  selectedPerfis: PerfilDTO[] = [];
  suggestions: PerfilParaVinculoDTO[] = [];
  perfilInput: PerfilDTO | string | null = null;
  perfilFilter = '';

  allUnidades: UnidadeNegocioDTO[] = [];
  selectedUnidades: UsuarioUnidadeNegocioDTO[] = [];
  unidadeDefaultId: string | null = null;

  toolbarActions: ToolbarActionModel[] = [];
  private auth: AuthService = inject(AuthService);

  ngOnInit(): void {
    this.initForm();

    // configure actions based on permission
    const canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.CADASTRO_USUARIO
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
      this.titulo += $localize`Novo`;
      this.usuario = {} as UsuarioDTO;
      this.loadPerfisAndInitLists();
      this.loadUnidadesAndInitLists();
    } else {
      this.editMode = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.usuario = response.body!;
        this.titulo += this.usuario.nome;
        this.fillForm();
        this.loadPerfisAndInitLists();
        this.loadUnidadesAndInitLists();
      });
    }
  }

  initForm() {
    const fb = new FormBuilder().nonNullable;
    this.form.addControl('nome', fb.control(null));
    this.form.addControl('login', fb.control(null));
    this.form.addControl('senha', fb.control(null));
  }

  fillForm() {
    this.form.get('nome')?.setValue(this.usuario.nome);
    this.form.get('login')?.setValue(this.usuario.login);
  }

  async adicionarPerfil(perfil: PerfilParaVinculoDTO) {
    if (!perfil) return;
    if (this.selectedPerfis.some((p) => p.id === perfil.id)) return;
    this.selectedPerfis.push(perfil as PerfilDTO);
  }

  removerPerfil(perfil: PerfilDTO) {
    if (!perfil) return;
    this.selectedPerfis = this.selectedPerfis.filter((p) => p.id !== perfil.id);
  }

  searchPerfis(event: { query: string }) {
    const q = event.query ? String(event.query).toLowerCase() : '';
    this.suggestions = this.allPerfis.filter((p) => {
      const nome = p?.nome ? String(p.nome).toLowerCase() : '';
      return (
        nome.includes(q) && !this.selectedPerfis.some((sp) => sp.id === p.id)
      );
    });
  }

  async onPerfilSelect(perfil: PerfilParaVinculoDTO) {
    await this.adicionarPerfil(perfil);
    this.perfilInput = '';
    this.suggestions = [];
  }

  private loadPerfisAndInitLists() {
    this.service.listarPerfisDisponiveis().subscribe((perfis) => {
      this.allPerfis = perfis;
      this.selectedPerfis = this.usuario.perfis || [];
    });
  }

  private loadUnidadesAndInitLists() {
    this.service
      .listarUnidadesDisponiveis()
      .subscribe((unidades: UnidadeNegocioDTO[]) => {
        this.allUnidades = unidades;
        this.selectedUnidades = this.usuario.unidadesNegocio || [];
        // Set default unidade if exists
        const defaultUnidade = this.selectedUnidades.find((u) => u.isDefault);
        this.unidadeDefaultId = defaultUnidade?.unidadeNegocioId || null;
      });
  }

  isUnidadeVinculada(unidadeId: string): boolean {
    return this.selectedUnidades.some((u) => u.unidadeNegocioId === unidadeId);
  }

  toggleUnidade(unidade: UnidadeNegocioDTO, checked: boolean) {
    if (checked) {
      // Add unidade
      this.selectedUnidades.push({
        unidadeNegocioId: unidade.id,
        unidadeNegocioNome: unidade.nome,
        unidadeNegocioCodigo: unidade.codigo,
        isDefault: this.selectedUnidades.length === 0, // First one is default
      });
      // Set as default if it's the first
      if (this.selectedUnidades.length === 1) {
        this.unidadeDefaultId = unidade.id;
      }
    } else {
      // Remove unidade
      this.selectedUnidades = this.selectedUnidades.filter(
        (u) => u.unidadeNegocioId !== unidade.id
      );
      // If removed unidade was default, set first as default
      if (this.unidadeDefaultId === unidade.id) {
        this.unidadeDefaultId =
          this.selectedUnidades.length > 0
            ? this.selectedUnidades[0].unidadeNegocioId
            : null;
      }
    }
  }

  salvar() {
    if (!this.form.valid) {
      this.messages.erro($localize`Existem campo inválidos.`);
      return;
    }

    this.usuario.nome = this.form.value.nome;
    this.usuario.login = this.form.value.login;
    // only send senha if non-empty — otherwise send null to avoid re-hashing existing hash
    this.usuario.senha =
      this.form.value.senha && this.form.value.senha.trim() !== ''
        ? this.form.value.senha
        : null;
    this.usuario.perfis = this.selectedPerfis;

    // Update isDefault flags based on selected radio
    this.usuario.unidadesNegocio = this.selectedUnidades.map((u) => ({
      ...u,
      isDefault: u.unidadeNegocioId === this.unidadeDefaultId,
    }));

    this.service.save(this.usuario, {
      onSuccess: (data: UsuarioDTO) => {
        this.usuario = data;
        this.messages.sucesso($localize`Usuário salvo com sucesso.`);
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
