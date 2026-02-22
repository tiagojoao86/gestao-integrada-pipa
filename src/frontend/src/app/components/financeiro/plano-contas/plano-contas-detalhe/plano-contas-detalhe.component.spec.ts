import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { PlanoContasDetalheComponent } from './plano-contas-detalhe.component';
import { PlanoContasService } from '../plano-contas.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { of } from 'rxjs';
import { PlanoContasDTO } from '../model/plano-contas-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { UsuarioUnidadeNegocioDTO } from '../../../cadastro/usuario/model/usuario-unidade-negocio-dto';
import { Response } from '../../../base/model/response';

describe('PlanoContasDetalheComponent', () => {
  let component: PlanoContasDetalheComponent;
  let fixture: ComponentFixture<PlanoContasDetalheComponent>;
  let planoContasService: jest.Mocked<PlanoContasService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;

  const mockUnidadesNegocio: UsuarioUnidadeNegocioDTO[] = [
    { id: 'un-1', nome: 'Unidade 1', codigo: 'UN01' } as UsuarioUnidadeNegocioDTO,
    { id: 'un-2', nome: 'Unidade 2', codigo: 'UN02' } as UsuarioUnidadeNegocioDTO,
  ];

  beforeEach(async () => {
    const planoContasServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
      list: jest.fn(),
      listarUnidadesDisponiveis: jest.fn(),
    };

    const messageServiceMock = {
      sucesso: jest.fn(),
      erro: jest.fn(),
    };

    const authServiceMock = {
      hasAuthorityEditarToModulo: jest.fn(),
      getDefaultUnidadeNegocio: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [PlanoContasDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    })
      .overrideComponent(PlanoContasDetalheComponent, {
        set: {
          providers: [
            { provide: PlanoContasService, useValue: planoContasServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(PlanoContasDetalheComponent);
    component = fixture.componentInstance;
    planoContasService = fixture.debugElement.injector.get(
      PlanoContasService
    ) as jest.Mocked<PlanoContasService>;
    messageService = TestBed.inject(
      MessageService
    ) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    // Mocks padrão
    planoContasServiceMock.findById.mockReturnValue(
      of({ body: new PlanoContasDTO() } as Response<PlanoContasDTO>)
    );

    planoContasServiceMock.listarUnidadesDisponiveis.mockReturnValue(
      of(mockUnidadesNegocio)
    );

    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
    authServiceMock.getDefaultUnidadeNegocio.mockReturnValue(mockUnidadesNegocio[0]);
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

      expect(component.form.get('codigo')).toBeTruthy();
      expect(component.form.get('descricao')).toBeTruthy();
      expect(component.form.get('tipo')).toBeTruthy();
      expect(component.form.get('ativo')).toBeTruthy();
      expect(component.form.get('unidadeNegocio')).toBeTruthy();
    });

    it('deve inicializar form com ativo=true como padrão', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.form.get('ativo')?.value).toBe(true);
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

    it('deve carregar unidades de negócio ao inicializar', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(planoContasService.listarUnidadesDisponiveis).toHaveBeenCalled();
    });

    it('deve definir unidade de negócio padrão do usuário ao criar novo', () => {
      component.detailId = 'add';
      component.ngOnInit();

      // A unidade default é setada imediatamente
      expect(authService.getDefaultUnidadeNegocio).toHaveBeenCalled();
      expect(component.form.get('unidadeNegocio')?.value).toBe('un-1');
    });
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados do plano de contas ao editar', () => {
      const mockPlano = new PlanoContasDTO(
        'pc-1',
        '1.01',
        'Caixa',
        'ATIVO',
        undefined,
        undefined,
        'un-1',
        'Unidade 1',
        true
      );

      planoContasService.findById.mockReturnValue(
        of({ body: mockPlano } as Response<PlanoContasDTO>)
      );

      component.detailId = 'pc-1';
      component.ngOnInit();

      expect(planoContasService.findById).toHaveBeenCalledWith('pc-1');
      expect(component.entity.codigo).toBe('1.01');
      expect(component.entity.descricao).toBe('Caixa');
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(planoContasService.findById).not.toHaveBeenCalled();
      expect(component.editMode).toBe(false);
    });

    it('deve preencher form com dados carregados usando fillForm', () => {
      const mockPlano = new PlanoContasDTO(
        'pc-2',
        '1.02',
        'Bancos',
        'ATIVO',
        undefined,
        undefined,
        'un-2',
        'Unidade 2',
        false
      );

      planoContasService.findById.mockReturnValue(
        of({ body: mockPlano } as Response<PlanoContasDTO>)
      );

      component.detailId = 'pc-2';
      component.ngOnInit();

      expect(component.form.get('codigo')?.value).toBe('1.02');
      expect(component.form.get('descricao')?.value).toBe('Bancos');
      expect(component.form.get('tipo')?.value).toBe('ATIVO');
      expect(component.form.get('ativo')?.value).toBe(false);
      expect(component.form.get('unidadeNegocio')?.value).toBe('un-2');
    });

    it('deve carregar plano pai ao editar plano com planoPaiId', () => {
      const mockPlanoPai = new PlanoContasDTO(
        'pc-pai',
        '1',
        'Ativo',
        'ATIVO'
      );

      const mockPlano = new PlanoContasDTO(
        'pc-filho',
        '1.01',
        'Caixa',
        'ATIVO',
        'pc-pai',
        'Ativo',
        'un-1'
      );

      planoContasService.findById.mockImplementation((id: string) => {
        if (id === 'pc-filho') {
          return of({ body: mockPlano } as Response<PlanoContasDTO>);
        }
        if (id === 'pc-pai') {
          return of({ body: mockPlanoPai } as Response<PlanoContasDTO>);
        }
        return of({ body: null });
      });

      component.detailId = 'pc-filho';
      component.ngOnInit();

      expect(planoContasService.findById).toHaveBeenCalledWith('pc-filho');
      expect(planoContasService.findById).toHaveBeenCalledWith('pc-pai');
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve salvar plano de contas com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        codigo: '1.01',
        descricao: 'Caixa',
        tipo: 'ATIVO',
        ativo: true,
        unidadeNegocio: 'un-1',
      });

      planoContasService.save.mockImplementation(
        (_data: PlanoContasDTO, callbacks: ExecutionCallbacks<PlanoContasDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess();
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.onSave();

      expect(planoContasService.save).toHaveBeenCalled();
      const callArgs = planoContasService.save.mock.calls[0][0];
      expect(callArgs.codigo).toBe('1.01');
      expect(callArgs.descricao).toBe('Caixa');
      expect(callArgs.tipo).toBe('ATIVO');
      expect(callArgs.ativo).toBe(true);
      expect(callArgs.unidadeNegocioId).toBe('un-1');
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('NÃO deve salvar se unidadeNegocio não foi selecionada', () => {
      component.form.patchValue({
        codigo: '1.01',
        descricao: 'Caixa',
        tipo: 'ATIVO',
        ativo: true,
        unidadeNegocio: '',
      });

      component.onSave();

      expect(messageService.erro).toHaveBeenCalled();
      expect(planoContasService.save).not.toHaveBeenCalled();
    });

    it('deve incluir id ao salvar quando está editando', () => {
      component.entity.id = 'pc-456';
      component.form.patchValue({
        codigo: '1.02',
        descricao: 'Bancos',
        tipo: 'ATIVO',
        ativo: true,
        unidadeNegocio: 'un-1',
      });

      planoContasService.save.mockImplementation(
        (_data: PlanoContasDTO, callbacks: ExecutionCallbacks<PlanoContasDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess();
          }
        }
      );

      component.onSave();

      const callArgs = planoContasService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('pc-456');
    });

    it('NÃO deve incluir id ao salvar quando é novo', () => {
      component.entity = component.createEmptyEntity();
      component.form.patchValue({
        codigo: '1.NEW',
        descricao: 'Novo Plano',
        tipo: 'ATIVO',
        ativo: true,
        unidadeNegocio: 'un-1',
      });

      planoContasService.save.mockImplementation(
        (_data: PlanoContasDTO, callbacks: ExecutionCallbacks<PlanoContasDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess();
          }
        }
      );

      component.onSave();

      const callArgs = planoContasService.save.mock.calls[0][0];
      expect(callArgs.id).toBeUndefined();
    });

    it('deve salvar plano de contas inativo (ativo=false)', () => {
      component.form.patchValue({
        codigo: '1.99',
        descricao: 'Plano Inativo',
        tipo: 'DESPESA',
        ativo: false,
        unidadeNegocio: 'un-1',
      });

      planoContasService.save.mockImplementation(
        (_data: PlanoContasDTO, callbacks: ExecutionCallbacks<PlanoContasDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess();
          }
        }
      );

      component.onSave();

      const callArgs = planoContasService.save.mock.calls[0][0];
      expect(callArgs.ativo).toBe(false);
    });

    it('deve salvar plano pai quando selectedPlanoPai está definido', () => {
      component.selectedPlanoPai = new PlanoContasDTO(
        'pc-pai',
        '1',
        'Ativo',
        'ATIVO'
      );

      component.form.patchValue({
        codigo: '1.01',
        descricao: 'Caixa',
        tipo: 'ATIVO',
        ativo: true,
        unidadeNegocio: 'un-1',
      });

      planoContasService.save.mockImplementation(
        (_data: PlanoContasDTO, callbacks: ExecutionCallbacks<PlanoContasDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess();
          }
        }
      );

      component.onSave();

      const callArgs = planoContasService.save.mock.calls[0][0];
      expect(callArgs.planoPaiId).toBe('pc-pai');
      expect(callArgs.planoPaiDescricao).toBe('Ativo');
    });

    it('NÃO deve incluir planoPaiId quando selectedPlanoPai é null', () => {
      component.selectedPlanoPai = null;

      component.form.patchValue({
        codigo: '1',
        descricao: 'Ativo',
        tipo: 'ATIVO',
        ativo: true,
        unidadeNegocio: 'un-1',
      });

      planoContasService.save.mockImplementation(
        (_data: PlanoContasDTO, callbacks: ExecutionCallbacks<PlanoContasDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess();
          }
        }
      );

      component.onSave();

      const callArgs = planoContasService.save.mock.calls[0][0];
      expect(callArgs.planoPaiId).toBeUndefined();
      expect(callArgs.planoPaiDescricao).toBeUndefined();
    });
  });

  describe('Funcionalidade de Plano Pai', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve limpar selectedPlanoPai ao mudar tipo', () => {
      component.selectedPlanoPai = new PlanoContasDTO('pc-1', '1', 'Teste', 'ATIVO');

      component.onTipoChange();

      expect(component.selectedPlanoPai).toBeNull();
      expect(component.entity.planoPaiId).toBeUndefined();
      expect(component.entity.planoPaiDescricao).toBeUndefined();
    });

    it('deve buscar planos pai com filtro de tipo', () => {
      component.entity.tipo = 'ATIVO';

      const mockResponse = {
        body: {
          content: [
            new PlanoContasDTO('pc-1', '1', 'Ativo', 'ATIVO'),
            new PlanoContasDTO('pc-2', '2', 'Ativo Circulante', 'ATIVO'),
          ],
        },
      };

      planoContasService.list.mockReturnValue(of(mockResponse));

      component.searchPlanoPai({ query: 'Ativo' });

      expect(planoContasService.list).toHaveBeenCalled();
      expect(component.planosPaiSuggestions.length).toBe(2);
    });

    it('deve excluir próprio plano dos resultados ao buscar plano pai', () => {
      component.entity.id = 'pc-1';
      component.entity.tipo = 'ATIVO';

      const mockResponse = {
        body: {
          content: [
            new PlanoContasDTO('pc-1', '1', 'Ativo', 'ATIVO'), // próprio plano
            new PlanoContasDTO('pc-2', '2', 'Ativo Circulante', 'ATIVO'),
          ],
        },
      };

      planoContasService.list.mockReturnValue(of(mockResponse));

      component.searchPlanoPai({ query: '' });

      expect(component.planosPaiSuggestions.length).toBe(1);
      expect(component.planosPaiSuggestions[0].id).toBe('pc-2');
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

    it('deve retornar false quando controle é válido', () => {
      component.form.get('descricao')?.setValue('Plano Válido');

      expect(component.isControlInvalid('descricao')).toBe(false);
    });

    it('deve retornar false quando controle não foi tocado', () => {
      component.form.get('descricao')?.setValue('');

      expect(component.isControlInvalid('descricao')).toBe(false);
    });
  });

  describe('Mensagens de Erro do Backend', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve chamar onError quando backend retorna erro', () => {
      component.form.patchValue({
        codigo: '1.DUP',
        descricao: 'Plano Duplicado',
        tipo: 'ATIVO',
        ativo: true,
        unidadeNegocio: 'un-1',
      });

      const mockError = {
        status: 400,
        error: { message: 'plano-contas.codigo.unique' },
      };

      planoContasService.save.mockImplementation(
        (_data: PlanoContasDTO, callbacks: ExecutionCallbacks<PlanoContasDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.onSave();

      expect(planoContasService.save).toHaveBeenCalled();
      // BaseService deve processar o erro e exibir mensagem via MessageService
    });

    it('NÃO deve fechar detalhe quando há erro do backend', () => {
      component.form.patchValue({
        codigo: '1.ERR',
        descricao: 'Plano Erro',
        tipo: 'ATIVO',
        ativo: true,
        unidadeNegocio: 'un-1',
      });

      const mockError = {
        status: 500,
        error: { message: 'Erro interno do servidor' },
      };

      planoContasService.save.mockImplementation(
        (_data: PlanoContasDTO, callbacks: ExecutionCallbacks<PlanoContasDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.onSave();

      expect(planoContasService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });

    it('deve permitir BaseService tratar erro de constraint unique', () => {
      component.form.patchValue({
        codigo: '1.EXIST',
        descricao: 'Plano Existente',
        tipo: 'ATIVO',
        ativo: true,
        unidadeNegocio: 'un-1',
      });

      const mockError = {
        status: 409,
        error: {
          message: 'plano-contas.codigo.unique',
          constraintName: 'UK_PLANO_CONTAS_CODIGO'
        },
      };

      planoContasService.save.mockImplementation(
        (_data: PlanoContasDTO, callbacks: ExecutionCallbacks<PlanoContasDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.onSave();

      expect(planoContasService.save).toHaveBeenCalled();
      // BaseService deve processar constraint e exibir mensagem amigável
    });

    it('deve permitir BaseService tratar erro de foreign key', () => {
      component.selectedPlanoPai = new PlanoContasDTO('pc-inexistente', '99', 'Inexistente', 'ATIVO');

      component.form.patchValue({
        codigo: '1.FK',
        descricao: 'Plano FK',
        tipo: 'ATIVO',
        ativo: true,
        unidadeNegocio: 'un-1',
      });

      const mockError = {
        status: 400,
        error: {
          message: 'plano-contas.planoPai.foreignKey',
          constraintName: 'FK_PLANO_CONTAS_PLANO_PAI'
        },
      };

      planoContasService.save.mockImplementation(
        (_data: PlanoContasDTO, callbacks: ExecutionCallbacks<PlanoContasDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.onSave();

      expect(planoContasService.save).toHaveBeenCalled();
      // BaseService deve processar FK e exibir mensagem amigável
    });
  });
});
