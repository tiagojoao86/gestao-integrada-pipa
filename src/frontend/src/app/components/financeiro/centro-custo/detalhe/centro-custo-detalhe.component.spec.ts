import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { CentroCustoDetalheComponent } from './centro-custo-detalhe.component';
import { CentroCustoService } from '../centro-custo.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { of } from 'rxjs';
import { CentroCustoDTO } from '../model/centro-custo-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { UsuarioUnidadeNegocioDTO } from '../../../cadastro/usuario/model/usuario-unidade-negocio-dto';
import { Response } from '../../../base/model/response';

describe('CentroCustoDetalheComponent', () => {
  let component: CentroCustoDetalheComponent;
  let fixture: ComponentFixture<CentroCustoDetalheComponent>;
  let centroCustoService: jest.Mocked<CentroCustoService>;
  let authService: jest.Mocked<AuthService>;

  const mockUnidadesNegocio: UsuarioUnidadeNegocioDTO[] = [
    { unidadeNegocioId: 'un-1', unidadeNegocioNome: 'Matriz', unidadeNegocioCodigo: 'UN01', isDefault: true },
    { unidadeNegocioId: 'un-2', unidadeNegocioNome: 'Filial', unidadeNegocioCodigo: 'UN02', isDefault: false },
  ];

  beforeEach(async () => {
    const centroCustoServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
    };

    const messageServiceMock = {
      sucesso: jest.fn(),
      erro: jest.fn(),
    };

    const authServiceMock = {
      hasAuthorityEditarToModulo: jest.fn(),
      getUnidadesNegocio: jest.fn(),
      getDefaultUnidadeNegocio: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [CentroCustoDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    })
      .overrideComponent(CentroCustoDetalheComponent, {
        set: {
          providers: [
            { provide: CentroCustoService, useValue: centroCustoServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(CentroCustoDetalheComponent);
    component = fixture.componentInstance;
    centroCustoService = fixture.debugElement.injector.get(
      CentroCustoService
    ) as jest.Mocked<CentroCustoService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    // Mocks padrão
    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
    authServiceMock.getUnidadesNegocio.mockReturnValue(mockUnidadesNegocio);
    authServiceMock.getDefaultUnidadeNegocio.mockReturnValue(
      mockUnidadesNegocio[0]
    );
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Inicialização', () => {
    it('deve criar o componente', () => {
      expect(component).toBeTruthy();
    });

    it('deve inicializar formulário com campos corretos', () => {
      component.ngOnInit();

      expect(component.form.get('nome')).toBeTruthy();
      expect(component.form.get('centroResultado')).toBeTruthy();
      expect(component.form.get('unidadeNegocioId')).toBeTruthy();
    });

    it('deve configurar toolbar com ações de cancelar e salvar quando tem permissão', () => {
      authService.hasAuthorityEditarToModulo.mockReturnValue(true);

      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(2);
      expect(component.toolbarActions[0].icon).toBe('close');
      expect(component.toolbarActions[1].icon).toBe('save');
    });

    it('deve configurar toolbar apenas com ação cancelar quando não tem permissão', () => {
      authService.hasAuthorityEditarToModulo.mockReturnValue(false);

      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(1);
      expect(component.toolbarActions[0].icon).toBe('close');
    });

    it('deve definir unidade de negócio padrão do usuário ao inicializar', () => {
      component.ngOnInit();

      expect(authService.getDefaultUnidadeNegocio).toHaveBeenCalled();
      expect(component.form.get('unidadeNegocioId')?.value).toBe('un-1');
    });

    it('deve carregar unidades de negócio do AuthService', () => {
      component.ngOnInit();

      expect(authService.getUnidadesNegocio).toHaveBeenCalled();
      expect(component.unidadesNegocioOptions.length).toBe(2);
      expect(component.unidadesNegocioOptions[0].unidadeNegocioNome).toBe('Matriz');
    });
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados do centro de custo ao editar', () => {
      const mockCentroCusto: CentroCustoDTO = {
        id: 'cc-1',
        nome: 'Centro Administrativo',
        centroResultado: true,
        unidadeNegocioId: 'un-2',
      };

      centroCustoService.findById.mockReturnValue(
        of({ body: mockCentroCusto } as Response<CentroCustoDTO>)
      );

      component.detailId = 'cc-1';
      component.ngOnInit();

      expect(centroCustoService.findById).toHaveBeenCalledWith('cc-1');
      expect(component.form.get('nome')?.value).toBe('Centro Administrativo');
      expect(component.form.get('centroResultado')?.value).toBe(true);
      expect(component.form.get('unidadeNegocioId')?.value).toBe('un-2');
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(centroCustoService.findById).not.toHaveBeenCalled();
    });

    it('deve manter unidade padrão quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.form.get('unidadeNegocioId')?.value).toBe('un-1');
    });
  });

  describe('Validações ao Salvar', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('NÃO deve salvar se formulário está inválido', () => {
      component.form.patchValue({
        nome: '',
        unidadeNegocioId: '',
      });
      component.form.markAllAsTouched();

      component.save();

      expect(centroCustoService.save).not.toHaveBeenCalled();
    });

    it('NÃO deve salvar se nome está vazio', () => {
      component.form.patchValue({
        nome: '',
        unidadeNegocioId: 'un-1',
      });

      component.save();

      expect(component.form.invalid).toBe(true);
      expect(centroCustoService.save).not.toHaveBeenCalled();
    });

    it('NÃO deve salvar se unidadeNegocioId está vazio', () => {
      component.form.patchValue({
        nome: 'Centro Teste',
        unidadeNegocioId: '',
      });

      component.save();

      expect(component.form.invalid).toBe(true);
      expect(centroCustoService.save).not.toHaveBeenCalled();
    });

    it('deve validar maxLength de nome (200 caracteres)', () => {
      const nomeGrande = 'a'.repeat(201);

      component.form.patchValue({
        nome: nomeGrande,
        unidadeNegocioId: 'un-1',
      });

      expect(component.form.get('nome')?.hasError('maxlength')).toBe(true);
      expect(component.form.invalid).toBe(true);
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('deve salvar centro de custo com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        nome: 'Centro Vendas',
        centroResultado: true,
        unidadeNegocioId: 'un-1',
      });

      centroCustoService.save.mockImplementation(
        (_data: CentroCustoDTO, callbacks: ExecutionCallbacks<CentroCustoDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({
              id: 'cc-123',
              nome: 'Centro Vendas',
              unidadeNegocioId: 'un-1',
            } as CentroCustoDTO);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(centroCustoService.save).toHaveBeenCalled();
      const callArgs = centroCustoService.save.mock.calls[0][0];
      expect(callArgs.nome).toBe('Centro Vendas');
      expect(callArgs.centroResultado).toBe(true);
      expect(callArgs.unidadeNegocioId).toBe('un-1');
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('deve incluir id ao salvar quando está editando', () => {
      component.detailId = 'cc-456';
      component.form.patchValue({
        nome: 'Centro Administrativo',
        unidadeNegocioId: 'un-2',
      });

      centroCustoService.save.mockImplementation(
        (_data: CentroCustoDTO, callbacks: ExecutionCallbacks<CentroCustoDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cc-456' } as CentroCustoDTO);
          }
        }
      );

      component.save();

      const callArgs = centroCustoService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('cc-456');
      expect(callArgs.nome).toBe('Centro Administrativo');
    });

    it('NÃO deve incluir id ao salvar quando é novo (add)', () => {
      component.detailId = 'add';
      component.form.patchValue({
        nome: 'Novo Centro',
        unidadeNegocioId: 'un-1',
      });

      centroCustoService.save.mockImplementation(
        (_data: CentroCustoDTO, callbacks: ExecutionCallbacks<CentroCustoDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cc-new' } as CentroCustoDTO);
          }
        }
      );

      component.save();

      const callArgs = centroCustoService.save.mock.calls[0][0];
      expect(callArgs.id).toBeUndefined();
      expect(callArgs.nome).toBe('Novo Centro');
    });

    it('deve salvar com centroResultado false (padrão)', () => {
      component.form.patchValue({
        nome: 'Centro Produção',
        centroResultado: false,
        unidadeNegocioId: 'un-1',
      });

      centroCustoService.save.mockImplementation(
        (_data: CentroCustoDTO, callbacks: ExecutionCallbacks<CentroCustoDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cc-789' } as CentroCustoDTO);
          }
        }
      );

      component.save();

      expect(centroCustoService.save).toHaveBeenCalled();
      const callArgs = centroCustoService.save.mock.calls[0][0];
      expect(callArgs.nome).toBe('Centro Produção');
      expect(callArgs.centroResultado).toBe(false);
    });

    it('deve salvar com unidade de negócio padrão do usuário', () => {
      // Não altera a unidade, usa a padrão setada no ngOnInit
      component.form.patchValue({
        nome: 'Centro RH',
      });

      centroCustoService.save.mockImplementation(
        (_data: CentroCustoDTO, callbacks: ExecutionCallbacks<CentroCustoDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cc-999' } as CentroCustoDTO);
          }
        }
      );

      component.save();

      const callArgs = centroCustoService.save.mock.calls[0][0];
      expect(callArgs.unidadeNegocioId).toBe('un-1'); // Unidade padrão
    });
  });

  describe('Ações da Toolbar', () => {
    it('deve fechar detalhe ao clicar em cancelar', () => {
      component.ngOnInit();

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.goBackFn();

      expect(closeDetailSpy).toHaveBeenCalled();
    });
  });

  describe('Validação de Controles', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('deve retornar true quando controle é inválido e foi tocado', () => {
      const nomeControl = component.form.get('nome');
      nomeControl?.setValue('');
      nomeControl?.markAsTouched();

      expect(component.isControlInvalid('nome')).toBe(true);
    });

    it('deve retornar true quando controle é inválido e está dirty', () => {
      const nomeControl = component.form.get('nome');
      nomeControl?.setValue('');
      nomeControl?.markAsDirty();

      expect(component.isControlInvalid('nome')).toBe(true);
    });

    it('deve retornar false quando controle é válido', () => {
      component.form.get('nome')?.setValue('Centro Teste');

      expect(component.isControlInvalid('nome')).toBe(false);
    });

    it('deve retornar false quando controle é inválido mas não foi tocado', () => {
      component.form.get('nome')?.setValue('');

      expect(component.isControlInvalid('nome')).toBe(false);
    });
  });

  describe('Mensagens de Erro do Backend', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('deve chamar onError quando backend retorna erro', () => {
      component.form.patchValue({
        nome: 'Centro Duplicado',
        unidadeNegocioId: 'un-1',
      });

      const mockError = {
        status: 400,
        error: { message: 'centroCusto.nome.unique' },
      };

      centroCustoService.save.mockImplementation(
        (_data: CentroCustoDTO, callbacks: ExecutionCallbacks<CentroCustoDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.save();

      expect(centroCustoService.save).toHaveBeenCalled();
      // BaseService deve processar o erro e exibir mensagem via MessageService
    });

    it('NÃO deve fechar detalhe quando há erro do backend', () => {
      component.form.patchValue({
        nome: 'Centro Erro',
        unidadeNegocioId: 'un-1',
      });

      const mockError = {
        status: 500,
        error: { message: 'Erro interno do servidor' },
      };

      centroCustoService.save.mockImplementation(
        (_data: CentroCustoDTO, callbacks: ExecutionCallbacks<CentroCustoDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(centroCustoService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });

    it('deve permitir BaseService tratar erro de constraint unique', () => {
      component.form.patchValue({
        nome: 'Centro Existente',
        unidadeNegocioId: 'un-1',
      });

      const mockError = {
        status: 409,
        error: {
          message: 'centroCusto.nome.unique',
          constraintName: 'UK_CENTRO_CUSTO_NOME',
        },
      };

      centroCustoService.save.mockImplementation(
        (_data: CentroCustoDTO, callbacks: ExecutionCallbacks<CentroCustoDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.save();

      expect(centroCustoService.save).toHaveBeenCalled();
      // BaseService deve processar constraint e exibir mensagem amigável
    });

    it('deve permitir BaseService tratar erro de foreign key', () => {
      component.form.patchValue({
        nome: 'Novo Centro',
        unidadeNegocioId: 'un-inexistente',
      });

      const mockError = {
        status: 400,
        error: {
          message: 'centroCusto.unidadeNegocio.foreignKey',
          constraintName: 'FK_CENTRO_CUSTO_UNIDADE_NEGOCIO',
        },
      };

      centroCustoService.save.mockImplementation(
        (_data: CentroCustoDTO, callbacks: ExecutionCallbacks<CentroCustoDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.save();

      expect(centroCustoService.save).toHaveBeenCalled();
      // BaseService deve processar FK e exibir mensagem amigável
    });
  });
});
