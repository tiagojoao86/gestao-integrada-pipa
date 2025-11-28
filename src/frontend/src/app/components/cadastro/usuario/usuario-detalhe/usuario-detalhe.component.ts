import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { RouteConstants } from '../../../base/constants/route-constants';
import { UsuarioService } from '../usuario.service';
import { PerfilService } from '../../perfil/perfil.service';
import {
  RegisterActionToolbar,
  BaseComponent,
} from '../../../base/base.component';
import { CommonModule } from '@angular/common';
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
import { MessageService } from '../../../base/messages/messages.service';
import { UsuarioDTO } from '../model/usuario-dto';
import { PerfilDTO } from '../../perfil/model/perfil-dto';
import { AuthService } from '../../../base/auth/auth-service';
import { PageRequest } from '../../../base/model/page-request';
import { FilterLogicOperator } from '../../../base/model/filter-dto';

@Component({
  selector: 'gi-usuario-detalhe',
  standalone: true,
  imports: [
    CommonModule,
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    PasswordModule,
    AutoCompleteModule,
  ],
  templateUrl: './usuario-detalhe.component.html',
  styleUrl: './usuario-detalhe.component.css',
  providers: [UsuarioService, PerfilService],
})
export class UsuarioDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup([]);
  modoEdicao = false;
  usuario: UsuarioDTO = {} as UsuarioDTO;
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  private service: UsuarioService = inject(UsuarioService);
  private perfilService: PerfilService = inject(PerfilService);
  private messages: MessageService = inject(MessageService);

  titulo = $localize`Usuário: `;

  
  allPerfis: PerfilDTO[] = [];
  selectedPerfis: PerfilDTO[] = [];
  suggestions: PerfilDTO[] = [];  
  perfilInput: PerfilDTO | string | null = null;
  perfilFilter = '';

  acoesTela: RegisterActionToolbar[] = [];
  private auth: AuthService = inject(AuthService);

  ngOnInit(): void {
    this.initForm();

    // configure actions based on permission
    const canEdit = this.auth.hasAuthorityEditarToModulo('CADASTRO_USUARIO');
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
      this.acoesTela.push({ action: () => { this.salvar(); }, icon: 'save', title: $localize`Salvar` + ' (enter)', shortcut: 'enter' });
    }

    if (this.detailId === RouteConstants.P_ADD) {
      this.modoEdicao = false;
      this.titulo += $localize`Novo`;      
      this.usuario = {} as UsuarioDTO;
      this.loadPerfisAndInitLists();
    } else {
      this.modoEdicao = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.usuario = response.body;
        this.titulo += this.usuario.nome;
        this.fillForm();
        this.loadPerfisAndInitLists();
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

  async adicionarPerfil(perfil: PerfilDTO) {
    if (!perfil) return;
    if (this.selectedPerfis.some((p) => p.id === perfil.id)) return;
    this.selectedPerfis.push(perfil);
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

  async onPerfilSelect(perfil: PerfilDTO) {    
    await this.adicionarPerfil(perfil);    
    this.perfilInput = '';
    this.suggestions = [];
  }

  private loadPerfisAndInitLists() {
    this.perfilService
      .list(
        new PageRequest(
          { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
          9999,
          0,
          []
        )
      )
      .subscribe((r) => {
        this.allPerfis = r.body.content || [];
        this.selectedPerfis = this.usuario.perfis || [];
      });
  }

  salvar() {
    if (!this.form.valid) {
      this.messages.erro($localize`Existem campo inválidos.`);
      return;
    }

    this.usuario.nome = this.form.value.nome;
    this.usuario.login = this.form.value.login;
    // only send senha if non-empty — otherwise send null to avoid re-hashing existing hash
    this.usuario.senha = this.form.value.senha && this.form.value.senha.trim() !== '' ? this.form.value.senha : null;
    this.usuario.perfis = this.selectedPerfis;

    this.service.save(this.usuario, {
      onSuccess: (data: UsuarioDTO) => {
        this.usuario = data;
        this.messages.sucesso($localize`Usuário salvo com sucesso.`);
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
    this.closeDetail.emit();
  };
}
