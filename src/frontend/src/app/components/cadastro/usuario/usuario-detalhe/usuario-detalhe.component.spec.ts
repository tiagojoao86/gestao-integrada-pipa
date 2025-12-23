import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UsuarioDetalheComponent } from './usuario-detalhe.component';
import { UsuarioService } from '../usuario.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { of, throwError } from 'rxjs';
import { UsuarioDTO } from '../model/usuario-dto';
import { RouteConstants } from '../../../base/constants/route-constants';
import { ExecutionCallbacks } from '../../../base/base-service';
import { PerfilParaVinculoDTO } from '../../perfil/model/perfil-para-vinculo-dto';
import { UnidadeNegocioDTO } from '../../unidade-negocio/model/unidade-negocio-dto';
import { UsuarioUnidadeNegocioDTO } from '../model/usuario-unidade-negocio-dto';
import { PerfilDTO } from '../../perfil/model/perfil-dto';

describe('UsuarioDetalheComponent', () => {
  let component: UsuarioDetalheComponent;
  let fixture: ComponentFixture<UsuarioDetalheComponent>;
  let usuarioService: any;
  let messageService: any;

  const authServiceMock = {
    hasAuthorityEditarToModulo: jest.fn().mockReturnValue(true),
  };

  const messageServiceMock = {
    erro: jest.fn(),
    sucesso: jest.fn(),
  };

  const mockPerfis: PerfilParaVinculoDTO[] = [
    { id: '1', nome: 'Admin' } as PerfilParaVinculoDTO,
    { id: '2', nome: 'Operador' } as PerfilParaVinculoDTO,
    { id: '3', nome: 'Consultor' } as PerfilParaVinculoDTO,
  ];

  const mockUnidades: UnidadeNegocioDTO[] = [
    { id: '1', nome: 'Unidade 1', codigo: 'UN01' } as UnidadeNegocioDTO,
    { id: '2', nome: 'Unidade 2', codigo: 'UN02' } as UnidadeNegocioDTO,
    { id: '3', nome: 'Unidade 3', codigo: 'UN03' } as UnidadeNegocioDTO,
  ];

  const usuarioServiceMock = {
    findById: jest.fn(),
    save: jest.fn(),
    listarPerfisDisponiveis: jest.fn(),
    listarUnidadesDisponiveis: jest.fn(),
  };

  beforeEach(async () => {
    usuarioServiceMock.listarPerfisDisponiveis.mockReturnValue(of(mockPerfis));
    usuarioServiceMock.listarUnidadesDisponiveis.mockReturnValue(
      of(mockUnidades)
    );

    await TestBed.configureTestingModule({
      imports: [UsuarioDetalheComponent],
    })
      .overrideComponent(UsuarioDetalheComponent, {
        set: {
          providers: [
            { provide: UsuarioService, useValue: usuarioServiceMock },
            { provide: MessageService, useValue: messageServiceMock },
            { provide: AuthService, useValue: authServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(UsuarioDetalheComponent);
    component = fixture.componentInstance;
    usuarioService = usuarioServiceMock;
    messageService = messageServiceMock;
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
      expect(component.form.get('login')).toBeTruthy();
      expect(component.form.get('senha')).toBeTruthy();
    });

    it('deve configurar toolbar com ações de cancelar e salvar quando tem permissão', () => {
      authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
      component.detailId = 'add';

      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(2);
      expect(component.toolbarActions[0].icon).toBe('close');
      expect(component.toolbarActions[1].icon).toBe('save');
    });

    it('deve configurar toolbar apenas com ação cancelar quando não tem permissão', () => {
      authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(false);
      component.detailId = 'add';

      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(1);
      expect(component.toolbarActions[0].icon).toBe('close');
    });

    it('deve carregar perfis disponíveis ao inicializar', () => {
      component.detailId = 'add';

      component.ngOnInit();

      expect(usuarioService.listarPerfisDisponiveis).toHaveBeenCalled();
      expect(component.allPerfis).toEqual(mockPerfis);
    });

    it('deve carregar unidades disponíveis ao inicializar', () => {
      component.detailId = 'add';

      component.ngOnInit();

      expect(usuarioService.listarUnidadesDisponiveis).toHaveBeenCalled();
      expect(component.allUnidades).toEqual(mockUnidades);
    });
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados do usuário ao editar', () => {
      const mockUsuario: UsuarioDTO = {
        id: '1',
        nome: 'João Silva',
        login: 'joao.silva',
        senha: '',
        perfis: [],
        unidadesNegocio: [],
      };
      usuarioService.findById.mockReturnValue(
        of({ body: mockUsuario } as any)
      );

      component.detailId = '1';
      component.ngOnInit();

      expect(usuarioService.findById).toHaveBeenCalledWith('1');
      expect(component.usuario).toEqual(mockUsuario);
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = RouteConstants.P_ADD;

      component.ngOnInit();

      expect(usuarioService.findById).not.toHaveBeenCalled();
    });

    it('deve preencher form com dados carregados', () => {
      const mockUsuario: UsuarioDTO = {
        id: '1',
        nome: 'João Silva',
        login: 'joao.silva',
        senha: '',
        perfis: [],
        unidadesNegocio: [],
      };
      usuarioService.findById.mockReturnValue(
        of({ body: mockUsuario } as any)
      );

      component.detailId = '1';
      component.ngOnInit();

      expect(component.form.get('nome')?.value).toBe('João Silva');
      expect(component.form.get('login')?.value).toBe('joao.silva');
    });

    it('deve carregar perfis existentes ao editar usuário', () => {
      const mockUsuario: UsuarioDTO = {
        id: '1',
        nome: 'João Silva',
        login: 'joao.silva',
        senha: '',
        perfis: [{ id: '1', nome: 'Admin' } as PerfilDTO],
        unidadesNegocio: [],
      };
      usuarioService.findById.mockReturnValue(
        of({ body: mockUsuario } as any)
      );

      component.detailId = '1';
      component.ngOnInit();

      expect(component.selectedPerfis.length).toBe(1);
      expect(component.selectedPerfis[0].id).toBe('1');
    });

    it('deve carregar unidades existentes ao editar usuário', () => {
      const mockUnidadesNegocio: UsuarioUnidadeNegocioDTO[] = [
        {
          unidadeNegocioId: '1',
          unidadeNegocioNome: 'Unidade 1',
          unidadeNegocioCodigo: 'UN01',
          isDefault: true,
        },
      ];
      const mockUsuario: UsuarioDTO = {
        id: '1',
        nome: 'João Silva',
        login: 'joao.silva',
        senha: '',
        perfis: [],
        unidadesNegocio: mockUnidadesNegocio,
      };
      usuarioService.findById.mockReturnValue(
        of({ body: mockUsuario } as any)
      );

      component.detailId = '1';
      component.ngOnInit();

      expect(component.selectedUnidades.length).toBe(1);
      expect(component.selectedUnidades[0].unidadeNegocioId).toBe('1');
      expect(component.unidadeDefaultId).toBe('1');
    });
  });

  describe('Gerenciamento de Perfis', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve adicionar perfil à lista de selecionados', async () => {
      const perfil = mockPerfis[0];

      await component.adicionarPerfil(perfil);

      expect(component.selectedPerfis.length).toBe(1);
      expect(component.selectedPerfis[0].id).toBe('1');
    });

    it('NÃO deve adicionar perfil duplicado', async () => {
      const perfil = mockPerfis[0];

      await component.adicionarPerfil(perfil);
      await component.adicionarPerfil(perfil);

      expect(component.selectedPerfis.length).toBe(1);
    });

    it('deve remover perfil da lista de selecionados', async () => {
      const perfil = mockPerfis[0];
      await component.adicionarPerfil(perfil);

      component.removerPerfil(perfil as PerfilDTO);

      expect(component.selectedPerfis.length).toBe(0);
    });

    it('searchPerfis deve filtrar perfis por nome', () => {
      component.searchPerfis({ query: 'admin' });

      expect(component.suggestions.length).toBe(1);
      expect(component.suggestions[0].nome).toBe('Admin');
    });

    it('searchPerfis deve excluir perfis já selecionados', async () => {
      await component.adicionarPerfil(mockPerfis[0]);

      component.searchPerfis({ query: '' });

      expect(component.suggestions.length).toBe(2); // 3 total - 1 selecionado
      expect(component.suggestions.some((s) => s.id === '1')).toBe(false);
    });

    it('onPerfilSelect deve adicionar perfil e limpar input', async () => {
      const perfil = mockPerfis[0];

      await component.onPerfilSelect(perfil);

      expect(component.selectedPerfis.length).toBe(1);
      expect(component.perfilInput).toBe('');
      expect(component.suggestions.length).toBe(0);
    });
  });

  describe('Gerenciamento de Unidades', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('isUnidadeVinculada deve retornar true para unidade vinculada', () => {
      component.selectedUnidades = [
        {
          unidadeNegocioId: '1',
          unidadeNegocioNome: 'Unidade 1',
          unidadeNegocioCodigo: 'UN01',
          isDefault: true,
        },
      ];

      expect(component.isUnidadeVinculada('1')).toBe(true);
      expect(component.isUnidadeVinculada('2')).toBe(false);
    });

    it('toggleUnidade deve adicionar unidade quando checked=true', () => {
      const unidade = mockUnidades[0];

      component.toggleUnidade(unidade, true);

      expect(component.selectedUnidades.length).toBe(1);
      expect(component.selectedUnidades[0].unidadeNegocioId).toBe('1');
    });

    it('primeira unidade adicionada deve ser marcada como default', () => {
      const unidade = mockUnidades[0];

      component.toggleUnidade(unidade, true);

      expect(component.selectedUnidades[0].isDefault).toBe(true);
      expect(component.unidadeDefaultId).toBe('1');
    });

    it('toggleUnidade deve remover unidade quando checked=false', () => {
      const unidade = mockUnidades[0];
      component.toggleUnidade(unidade, true);

      component.toggleUnidade(unidade, false);

      expect(component.selectedUnidades.length).toBe(0);
    });

    it('ao remover unidade default, deve definir primeira como default', () => {
      component.toggleUnidade(mockUnidades[0], true);
      component.toggleUnidade(mockUnidades[1], true);

      // Remove a primeira (default)
      component.toggleUnidade(mockUnidades[0], false);

      expect(component.unidadeDefaultId).toBe('2');
    });

    it('ao remover última unidade, unidadeDefaultId deve ser null', () => {
      component.toggleUnidade(mockUnidades[0], true);

      component.toggleUnidade(mockUnidades[0], false);

      expect(component.unidadeDefaultId).toBeNull();
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve salvar usuário com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        nome: 'João Silva',
        login: 'joao.silva',
        senha: 'senha123',
      });

      usuarioService.save.mockImplementation(
        (_data: UsuarioDTO, callbacks: ExecutionCallbacks<UsuarioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({} as UsuarioDTO);
          }
        }
      );

      const closeSpy = jest.spyOn(component.closeDetail, 'emit');

      component.salvar();

      expect(usuarioService.save).toHaveBeenCalled();
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeSpy).toHaveBeenCalled();
    });

    it('NÃO deve salvar se formulário está inválido', () => {
      component.form.patchValue({
        nome: null,
        login: null,
        senha: null,
      });
      component.form.setErrors({ invalid: true });

      component.salvar();

      expect(usuarioService.save).not.toHaveBeenCalled();
      expect(messageService.erro).toHaveBeenCalled();
    });

    it('deve incluir perfis selecionados no payload', () => {
      component.form.patchValue({
        nome: 'João Silva',
        login: 'joao.silva',
        senha: 'senha123',
      });
      component.selectedPerfis = [mockPerfis[0] as PerfilDTO];

      usuarioService.save.mockImplementation(
        (_data: UsuarioDTO, callbacks: ExecutionCallbacks<UsuarioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({} as UsuarioDTO);
          }
        }
      );

      component.salvar();

      const callArgs = usuarioService.save.mock.calls[0][0];
      expect(callArgs.perfis?.length).toBe(1);
      expect(callArgs.perfis?.[0].id).toBe('1');
    });

    it('deve incluir unidades selecionadas com flag isDefault correto', () => {
      component.form.patchValue({
        nome: 'João Silva',
        login: 'joao.silva',
        senha: 'senha123',
      });
      component.toggleUnidade(mockUnidades[0], true);
      component.toggleUnidade(mockUnidades[1], true);
      component.unidadeDefaultId = '2'; // Segunda unidade como default

      usuarioService.save.mockImplementation(
        (_data: UsuarioDTO, callbacks: ExecutionCallbacks<UsuarioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({} as UsuarioDTO);
          }
        }
      );

      component.salvar();

      const callArgs = usuarioService.save.mock.calls[0][0];
      expect(callArgs.unidadesNegocio?.length).toBe(2);
      expect(
        callArgs.unidadesNegocio?.find((u) => u.unidadeNegocioId === '1')
          ?.isDefault
      ).toBe(false);
      expect(
        callArgs.unidadesNegocio?.find((u) => u.unidadeNegocioId === '2')
          ?.isDefault
      ).toBe(true);
    });

    it('deve enviar null para senha quando campo está vazio (modo edição)', () => {
      component.editMode = true;
      component.form.patchValue({
        nome: 'João Silva',
        login: 'joao.silva',
        senha: '   ', // whitespace only
      });

      usuarioService.save.mockImplementation(
        (_data: UsuarioDTO, callbacks: ExecutionCallbacks<UsuarioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({} as UsuarioDTO);
          }
        }
      );

      component.salvar();

      const callArgs = usuarioService.save.mock.calls[0][0];
      expect(callArgs.senha).toBeNull();
    });

    it('deve incluir id ao salvar quando está editando', () => {
      const mockUsuario: UsuarioDTO = {
        id: '123',
        nome: 'João Silva',
        login: 'joao.silva',
        senha: '',
        perfis: [],
        unidadesNegocio: [],
      };
      usuarioService.findById.mockReturnValue(
        of({ body: mockUsuario } as any)
      );

      component.detailId = '123';
      component.ngOnInit();

      component.form.patchValue({
        nome: 'João Silva Atualizado',
        login: 'joao.silva',
      });

      usuarioService.save.mockImplementation(
        (_data: UsuarioDTO, callbacks: ExecutionCallbacks<UsuarioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({} as UsuarioDTO);
          }
        }
      );

      component.salvar();

      const callArgs = usuarioService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('123');
    });
  });

  describe('Ações da Toolbar', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve fechar detalhe ao clicar em cancelar', () => {
      const closeSpy = jest.spyOn(component.closeDetail, 'emit');

      component.goBackFn();

      expect(closeSpy).toHaveBeenCalled();
    });
  });

  describe('Validação de Controles', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve retornar true quando campo nome é inválido e foi tocado', () => {
      const nomeControl = component.form.get('nome');
      nomeControl?.markAsTouched();
      nomeControl?.setErrors({ required: true });

      expect(component.isControlInvalid('nome')).toBe(true);
    });

    it('deve retornar false quando campo nome é válido', () => {
      const nomeControl = component.form.get('nome');
      nomeControl?.setValue('João Silva');
      nomeControl?.markAsTouched();

      expect(component.isControlInvalid('nome')).toBe(false);
    });

    it('deve retornar false quando campo não foi tocado', () => {
      const nomeControl = component.form.get('nome');
      nomeControl?.setErrors({ required: true });

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
        nome: 'João Silva',
        login: 'joao.silva',
        senha: 'senha123',
      });

      usuarioService.save.mockImplementation(
        (_data: UsuarioDTO, callbacks: ExecutionCallbacks<UsuarioDTO>) => {
          if (callbacks.onError) {
            callbacks.onError({
              error: {
                message: 'ERRO_LOGIN_DUPLICADO',
              },
            } as any);
          }
        }
      );

      component.salvar();

      expect(usuarioService.save).toHaveBeenCalled();
      // BaseService.onError is called internally
    });

    it('NÃO deve fechar detalhe quando há erro do backend', () => {
      component.form.patchValue({
        nome: 'João Silva',
        login: 'joao.silva',
        senha: 'senha123',
      });

      usuarioService.save.mockImplementation(
        (_data: UsuarioDTO, callbacks: ExecutionCallbacks<UsuarioDTO>) => {
          if (callbacks.onError) {
            callbacks.onError({
              error: {
                message: 'ERRO_LOGIN_DUPLICADO',
              },
            } as any);
          }
        }
      );

      const closeSpy = jest.spyOn(component.closeDetail, 'emit');

      component.salvar();

      expect(closeSpy).not.toHaveBeenCalled();
    });

    it('deve permitir BaseService tratar erro de constraint unique', () => {
      component.form.patchValue({
        nome: 'João Silva',
        login: 'joao.silva',
        senha: 'senha123',
      });

      usuarioService.save.mockImplementation(
        (_data: UsuarioDTO, callbacks: ExecutionCallbacks<UsuarioDTO>) => {
          if (callbacks.onError) {
            callbacks.onError({
              error: {
                message:
                  'could not execute statement [ERROR: duplicate key value violates unique constraint "usuario_login_key"',
              },
            } as any);
          }
        }
      );

      component.salvar();

      expect(usuarioService.save).toHaveBeenCalled();
      // BaseService handles the constraint error message
    });
  });
});
