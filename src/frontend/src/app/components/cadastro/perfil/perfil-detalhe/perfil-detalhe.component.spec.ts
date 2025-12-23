import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { PerfilDetalheComponent } from './perfil-detalhe.component';
import { PerfilService } from '../perfil.service';
import { ModuloService } from '../modulo.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { of } from 'rxjs';
import { PerfilDTO } from '../model/perfil-dto';
import { ModuloDTO } from '../model/modulo-dto';
import { ExecutionCallbacks } from '../../../base/base-service';

describe('PerfilDetalheComponent', () => {
  let component: PerfilDetalheComponent;
  let fixture: ComponentFixture<PerfilDetalheComponent>;
  let perfilService: jest.Mocked<PerfilService>;
  let moduloService: jest.Mocked<ModuloService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;

  const mockModulosAgrupados = {
    'CADASTRO': [
      { id: 'm-1', nome: 'Pessoa', chave: 'CADASTRO_PESSOA', grupoEnum: 'CADASTRO' } as ModuloDTO,
      { id: 'm-2', nome: 'Perfil', chave: 'CADASTRO_PERFIL', grupoEnum: 'CADASTRO' } as ModuloDTO,
    ],
    'FINANCEIRO': [
      { id: 'm-3', nome: 'Título', chave: 'FINANCEIRO_TITULO', grupoEnum: 'FINANCEIRO' } as ModuloDTO,
    ],
  };

  beforeEach(async () => {
    const perfilServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
    };

    const moduloServiceMock = {
      getGroupedModules: jest.fn(),
    };

    const messageServiceMock = {
      sucesso: jest.fn(),
      erro: jest.fn(),
    };

    const authServiceMock = {
      hasAuthorityEditarToModulo: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [PerfilDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    })
      .overrideComponent(PerfilDetalheComponent, {
        set: {
          providers: [
            { provide: PerfilService, useValue: perfilServiceMock },
            { provide: ModuloService, useValue: moduloServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(PerfilDetalheComponent);
    component = fixture.componentInstance;
    perfilService = fixture.debugElement.injector.get(
      PerfilService
    ) as jest.Mocked<PerfilService>;
    moduloService = fixture.debugElement.injector.get(
      ModuloService
    ) as jest.Mocked<ModuloService>;
    messageService = TestBed.inject(
      MessageService
    ) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    // Mocks padrão
    moduloServiceMock.getGroupedModules.mockReturnValue(
      of({ body: mockModulosAgrupados } as any)
    );

    perfilServiceMock.findById.mockReturnValue(
      of({ body: { id: '', nome: '', permissoes: [] } } as any)
    );

    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Inicialização', () => {
    it('deve criar o componente', () => {
      expect(component).toBeTruthy();
    });

    it('deve inicializar formulário com campos corretos', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.form.get('nome')).toBeTruthy();
      expect(component.form.get('permissoes')).toBeTruthy();
    });

    it('deve configurar toolbar com ações de cancelar e salvar quando tem permissão', () => {
      authService.hasAuthorityEditarToModulo.mockReturnValue(true);

      component.detailId = 'add';
      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(2);
      expect(component.toolbarActions[0].icon).toBe('close');
      expect(component.toolbarActions[1].icon).toBe('save');
    });

    it('deve configurar toolbar apenas com ação cancelar quando não tem permissão', () => {
      authService.hasAuthorityEditarToModulo.mockReturnValue(false);

      component.detailId = 'add';
      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(1);
      expect(component.toolbarActions[0].icon).toBe('close');
    });

    it('deve carregar módulos agrupados ao inicializar', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(moduloService.getGroupedModules).toHaveBeenCalled();
    });
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados do perfil ao editar', () => {
      const mockPerfil: PerfilDTO = {
        id: 'perf-1',
        nome: 'Administrador',
        permissoes: [],
      };

      perfilService.findById.mockReturnValue(
        of({ body: mockPerfil } as any)
      );

      component.detailId = 'perf-1';
      component.ngOnInit();

      expect(perfilService.findById).toHaveBeenCalledWith('perf-1');
      expect(component.perfil.nome).toBe('Administrador');
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(perfilService.findById).not.toHaveBeenCalled();
      expect(component.editMode).toBe(false);
    });

    it('deve preencher form com dados carregados', () => {
      const mockPerfil: PerfilDTO = {
        id: 'perf-2',
        nome: 'Usuário Comum',
        permissoes: [],
      };

      perfilService.findById.mockReturnValue(
        of({ body: mockPerfil } as any)
      );

      component.detailId = 'perf-2';
      component.ngOnInit();

      expect(component.form.get('nome')?.value).toBe('Usuário Comum');
    });

    it('deve construir formulário de permissões com módulos carregados', () => {
      component.detailId = 'add';
      component.ngOnInit();

      // 3 módulos mockados devem gerar 3 FormGroups de permissões
      expect(component.permissoes.length).toBe(3);
    });

    it('deve carregar permissões existentes ao editar perfil', () => {
      const mockPerfil: PerfilDTO = {
        id: 'perf-3',
        nome: 'Editor',
        permissoes: [
          {
            id: 'pm-1',
            perfilId: 'perf-3',
            moduloId: 'm-1',
            moduloNome: 'Pessoa',
            podeListar: true,
            podeVisualizar: true,
            podeEditar: true,
            podeDeletar: false,
          },
        ],
      };

      perfilService.findById.mockReturnValue(
        of({ body: mockPerfil } as any)
      );

      component.detailId = 'perf-3';
      component.ngOnInit();

      // Verifica se alguma permissão foi marcada como selecionada
      const permissaoControls = component.permissoes.controls;
      const permissaoPessoa = permissaoControls.find(
        (ctrl) => ctrl.get('moduloId')?.value === 'm-1'
      );
      expect(permissaoPessoa?.get('podeListar')?.value).toBe(true);
      expect(permissaoPessoa?.get('podeEditar')?.value).toBe(true);
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve salvar perfil com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        nome: 'Novo Perfil',
      });

      // Marca algumas permissões
      const firstPermissao = component.permissoes.at(0);
      firstPermissao.patchValue({
        podeListar: true,
        podeVisualizar: true,
      });

      perfilService.save.mockImplementation(
        (_data: PerfilDTO, callbacks: ExecutionCallbacks<PerfilDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({
              id: 'perf-123',
              nome: 'Novo Perfil',
              permissoes: [],
            } as PerfilDTO);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.savePerfil();

      expect(perfilService.save).toHaveBeenCalled();
      const callArgs = perfilService.save.mock.calls[0][0];
      expect(callArgs.nome).toBe('Novo Perfil');
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('NÃO deve salvar se nome está inválido', () => {
      component.form.patchValue({
        nome: '',
      });
      component.form.get('nome')?.markAsTouched();

      component.savePerfil();

      expect(messageService.erro).toHaveBeenCalled();
      expect(perfilService.save).not.toHaveBeenCalled();
    });

    it('deve incluir apenas permissões marcadas no payload', () => {
      component.form.patchValue({
        nome: 'Perfil Teste',
      });

      // Marca apenas a primeira permissão
      const firstPermissao = component.permissoes.at(0);
      firstPermissao.patchValue({
        podeListar: true,
        podeVisualizar: true,
        podeEditar: false,
        podeDeletar: false,
      });

      // Segunda permissão fica desmarcada
      const secondPermissao = component.permissoes.at(1);
      secondPermissao.patchValue({
        podeListar: false,
        podeVisualizar: false,
        podeEditar: false,
        podeDeletar: false,
      });

      perfilService.save.mockImplementation(
        (_data: PerfilDTO, callbacks: ExecutionCallbacks<PerfilDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({} as PerfilDTO);
          }
        }
      );

      component.savePerfil();

      const callArgs = perfilService.save.mock.calls[0][0];
      // Apenas 1 permissão deve ser incluída (a que tem pelo menos um campo marcado)
      expect(callArgs.permissoes.length).toBe(1);
    });

    it('deve incluir id ao salvar quando está editando', () => {
      component.perfil.id = 'perf-456';
      component.form.patchValue({
        nome: 'Perfil Editado',
      });

      perfilService.save.mockImplementation(
        (_data: PerfilDTO, callbacks: ExecutionCallbacks<PerfilDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'perf-456' } as PerfilDTO);
          }
        }
      );

      component.savePerfil();

      const callArgs = perfilService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('perf-456');
    });
  });

  describe('Funcionalidade de Permissões', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('getGrupos deve retornar lista de grupos ordenada', () => {
      const grupos = component.getGrupos();

      expect(grupos.length).toBe(2);
      expect(grupos).toContain('CADASTRO');
      expect(grupos).toContain('FINANCEIRO');
    });

    it('getPermissoesDoGrupo deve filtrar permissões por grupo', () => {
      const permissoesCadastro = component.getPermissoesDoGrupo('CADASTRO');
      const permissoesFinanceiro = component.getPermissoesDoGrupo('FINANCEIRO');

      expect(permissoesCadastro.length).toBe(2); // Pessoa e Perfil
      expect(permissoesFinanceiro.length).toBe(1); // Título
    });

    it('onSelectModuloChange deve marcar todas permissões ao selecionar módulo', () => {
      const permissao = component.permissoes.at(0);

      component.onSelectModuloChange(permissao, { checked: true } as any);

      expect(permissao.get('podeListar')?.value).toBe(true);
      expect(permissao.get('podeVisualizar')?.value).toBe(true);
      expect(permissao.get('podeEditar')?.value).toBe(true);
      expect(permissao.get('podeDeletar')?.value).toBe(true);
    });

    it('onSelectModuloChange deve desmarcar todas permissões ao desselecionar módulo', () => {
      const permissao = component.permissoes.at(0);

      // Marca tudo primeiro
      permissao.patchValue({
        podeListar: true,
        podeVisualizar: true,
        podeEditar: true,
        podeDeletar: true,
      });

      // Desmarca
      component.onSelectModuloChange(permissao, { checked: false } as any);

      expect(permissao.get('podeListar')?.value).toBe(false);
      expect(permissao.get('podeVisualizar')?.value).toBe(false);
      expect(permissao.get('podeEditar')?.value).toBe(false);
      expect(permissao.get('podeDeletar')?.value).toBe(false);
    });
  });

  describe('Ações da Toolbar', () => {
    it('deve fechar detalhe ao clicar em cancelar', () => {
      component.detailId = 'add';
      component.ngOnInit();

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.goBackFn();

      expect(closeDetailSpy).toHaveBeenCalled();
    });
  });

  describe('Validação de Controles', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve retornar true quando campo nome é inválido e foi tocado', () => {
      const nomeControl = component.form.get('nome');
      nomeControl?.setValue('');
      nomeControl?.markAsTouched();

      expect(component.isControlInvalid('nome')).toBe(true);
    });

    it('deve retornar false quando campo nome é válido', () => {
      component.form.get('nome')?.setValue('Perfil Válido');

      expect(component.isControlInvalid('nome')).toBe(false);
    });

    it('deve retornar false quando campo não foi tocado', () => {
      component.form.get('nome')?.setValue('');

      expect(component.isControlInvalid('nome')).toBe(false);
    });
  });

  describe('Mensagens de Erro do Backend', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve chamar onError quando backend retorna erro', () => {
      component.form.patchValue({
        nome: 'Perfil Duplicado',
      });

      const mockError = {
        status: 400,
        error: { message: 'perfil.nome.unique' },
      };

      perfilService.save.mockImplementation(
        (_data: PerfilDTO, callbacks: ExecutionCallbacks<PerfilDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as any);
          }
        }
      );

      component.savePerfil();

      expect(perfilService.save).toHaveBeenCalled();
      // BaseService deve processar o erro e exibir mensagem via MessageService
    });

    it('NÃO deve fechar detalhe quando há erro do backend', () => {
      component.form.patchValue({
        nome: 'Perfil Erro',
      });

      const mockError = {
        status: 500,
        error: { message: 'Erro interno do servidor' },
      };

      perfilService.save.mockImplementation(
        (_data: PerfilDTO, callbacks: ExecutionCallbacks<PerfilDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as any);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.savePerfil();

      expect(perfilService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });

    it('deve permitir BaseService tratar erro de constraint unique', () => {
      component.form.patchValue({
        nome: 'Perfil Existente',
      });

      const mockError = {
        status: 409,
        error: {
          message: 'perfil.nome.unique',
          constraintName: 'UK_PERFIL_NOME'
        },
      };

      perfilService.save.mockImplementation(
        (_data: PerfilDTO, callbacks: ExecutionCallbacks<PerfilDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as any);
          }
        }
      );

      component.savePerfil();

      expect(perfilService.save).toHaveBeenCalled();
      // BaseService/BackendMessageService deve processar constraint e exibir mensagem amigável
    });
  });
});
