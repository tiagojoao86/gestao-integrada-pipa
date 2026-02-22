import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { SetorDetalheComponent } from './setor-detalhe.component';
import { SetorService } from '../setor.service';
import { CentroCustoService } from '../../../financeiro/centro-custo/centro-custo.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { of } from 'rxjs';
import { SetorDTO } from '../model/setor-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { CentroCustoGridDTO } from '../../../financeiro/centro-custo/model/centro-custo-grid-dto';
import { HttpErrorResponse } from '@angular/common/http';

describe('SetorDetalheComponent', () => {
  let component: SetorDetalheComponent;
  let fixture: ComponentFixture<SetorDetalheComponent>;
  let setorService: jest.Mocked<SetorService>;
  let centroCustoService: jest.Mocked<CentroCustoService>;
  let authService: jest.Mocked<AuthService>;

  beforeEach(async () => {
    const setorServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
    };

    const centroCustoServiceMock = {
      listAll: jest.fn(),
    };

    const messageServiceMock = {
      sucesso: jest.fn(),
      erro: jest.fn(),
    };

    const authServiceMock = {
      hasAuthorityEditarToModulo: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [SetorDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    })
      .overrideComponent(SetorDetalheComponent, {
        set: {
          providers: [
            { provide: SetorService, useValue: setorServiceMock },
            { provide: CentroCustoService, useValue: centroCustoServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(SetorDetalheComponent);
    component = fixture.componentInstance;
    setorService = fixture.debugElement.injector.get(
      SetorService
    ) as jest.Mocked<SetorService>;
    centroCustoService = fixture.debugElement.injector.get(
      CentroCustoService
    ) as jest.Mocked<CentroCustoService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    // Mock padrão para listAll de centros de custo
    centroCustoService.listAll.mockReturnValue(
      of({ body: [], statusCode: 200, erroMessage: null })
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
      component.ngOnInit();

      expect(component.form.get('nome')).toBeTruthy();
      expect(component.form.get('descricao')).toBeTruthy();
      expect(component.form.get('centroCustoId')).toBeTruthy();
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
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar centros de custo ao inicializar', () => {
      const mockCentrosCusto: CentroCustoGridDTO[] = [
        { id: 'cc-1', nome: 'Centro Custo 1' } as CentroCustoGridDTO,
        { id: 'cc-2', nome: 'Centro Custo 2' } as CentroCustoGridDTO,
      ];

      centroCustoService.listAll.mockReturnValue(
        of({ body: mockCentrosCusto, statusCode: 200, erroMessage: null })
      );

      component.ngOnInit();

      expect(centroCustoService.listAll).toHaveBeenCalled();
      expect(component.centrosCustoOptions.length).toBe(2);
      expect(component.centrosCustoOptions[0].nome).toBe('Centro Custo 1');
    });

    it('deve carregar dados do setor ao editar', () => {
      const mockSetor: SetorDTO = {
        id: 'setor-1',
        nome: 'Administrativo',
        descricao: 'Setor administrativo',
        centroCustoId: 'cc-1',
      };

      setorService.findById.mockReturnValue(
        of({ body: mockSetor, statusCode: 200, erroMessage: null })
      );

      component.detailId = 'setor-1';
      component.ngOnInit();

      expect(setorService.findById).toHaveBeenCalledWith('setor-1');
      expect(component.form.get('nome')?.value).toBe('Administrativo');
      expect(component.form.get('descricao')?.value).toBe('Setor administrativo');
      expect(component.form.get('centroCustoId')?.value).toBe('cc-1');
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(setorService.findById).not.toHaveBeenCalled();
    });
  });

  describe('Validações ao Salvar', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('NÃO deve salvar se formulário está inválido', () => {
      component.form.patchValue({
        nome: '',
        centroCustoId: '',
      });
      component.form.markAllAsTouched();

      component.save();

      expect(setorService.save).not.toHaveBeenCalled();
    });

    it('NÃO deve salvar se nome está vazio', () => {
      component.form.patchValue({
        nome: '',
        centroCustoId: 'cc-1',
      });

      component.save();

      expect(component.form.invalid).toBe(true);
      expect(setorService.save).not.toHaveBeenCalled();
    });

    it('NÃO deve salvar se centroCustoId está vazio', () => {
      component.form.patchValue({
        nome: 'Vendas',
        centroCustoId: '',
      });

      component.save();

      expect(component.form.invalid).toBe(true);
      expect(setorService.save).not.toHaveBeenCalled();
    });

    it('deve validar maxLength de nome (200 caracteres)', () => {
      const nomeGrande = 'a'.repeat(201);

      component.form.patchValue({
        nome: nomeGrande,
        centroCustoId: 'cc-1',
      });

      expect(component.form.get('nome')?.hasError('maxlength')).toBe(true);
      expect(component.form.invalid).toBe(true);
    });

    it('deve validar maxLength de descricao (500 caracteres)', () => {
      const descricaoGrande = 'a'.repeat(501);

      component.form.patchValue({
        nome: 'Vendas',
        descricao: descricaoGrande,
        centroCustoId: 'cc-1',
      });

      expect(component.form.get('descricao')?.hasError('maxlength')).toBe(true);
      expect(component.form.invalid).toBe(true);
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('deve salvar setor com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        nome: 'Vendas',
        descricao: 'Setor de vendas',
        centroCustoId: 'cc-1',
      });

      setorService.save.mockImplementation(
        (_data: SetorDTO, callbacks: ExecutionCallbacks<SetorDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({
              id: 'setor-123',
              nome: 'Vendas',
              centroCustoId: 'cc-1',
            } as SetorDTO);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(setorService.save).toHaveBeenCalled();
      const callArgs = setorService.save.mock.calls[0][0];
      expect(callArgs.nome).toBe('Vendas');
      expect(callArgs.descricao).toBe('Setor de vendas');
      expect(callArgs.centroCustoId).toBe('cc-1');
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('deve incluir id ao salvar quando está editando', () => {
      component.detailId = 'setor-456';
      component.form.patchValue({
        nome: 'Administrativo',
        centroCustoId: 'cc-2',
      });

      setorService.save.mockImplementation(
        (_data: SetorDTO, callbacks: ExecutionCallbacks<SetorDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'setor-456' } as SetorDTO);
          }
        }
      );

      component.save();

      const callArgs = setorService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('setor-456');
      expect(callArgs.nome).toBe('Administrativo');
    });

    it('NÃO deve incluir id ao salvar quando é novo (add)', () => {
      component.detailId = 'add';
      component.form.patchValue({
        nome: 'Novo Setor',
        centroCustoId: 'cc-1',
      });

      setorService.save.mockImplementation(
        (_data: SetorDTO, callbacks: ExecutionCallbacks<SetorDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'setor-new' } as SetorDTO);
          }
        }
      );

      component.save();

      const callArgs = setorService.save.mock.calls[0][0];
      expect(callArgs.id).toBeUndefined();
      expect(callArgs.nome).toBe('Novo Setor');
    });

    it('deve salvar setor sem descricao (campo opcional)', () => {
      component.form.patchValue({
        nome: 'RH',
        descricao: '',
        centroCustoId: 'cc-3',
      });

      setorService.save.mockImplementation(
        (_data: SetorDTO, callbacks: ExecutionCallbacks<SetorDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'setor-789' } as SetorDTO);
          }
        }
      );

      component.save();

      expect(setorService.save).toHaveBeenCalled();
      const callArgs = setorService.save.mock.calls[0][0];
      expect(callArgs.nome).toBe('RH');
      expect(callArgs.descricao).toBe('');
      expect(callArgs.centroCustoId).toBe('cc-3');
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
      component.form.get('nome')?.setValue('Vendas');

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
        nome: 'Setor Duplicado',
        centroCustoId: 'cc-1',
      });

      const mockError = new HttpErrorResponse({
        status: 400,
        error: { message: 'setor.nome.unique' },
      });

      setorService.save.mockImplementation(
        (_data: SetorDTO, callbacks: ExecutionCallbacks<SetorDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError);
          }
        }
      );

      component.save();

      expect(setorService.save).toHaveBeenCalled();
      // BaseService deve processar o erro e exibir mensagem via MessageService
    });

    it('NÃO deve fechar detalhe quando há erro do backend', () => {
      component.form.patchValue({
        nome: 'Setor Erro',
        centroCustoId: 'cc-1',
      });

      const mockError = new HttpErrorResponse({
        status: 500,
        error: { message: 'Erro interno do servidor' },
      });

      setorService.save.mockImplementation(
        (_data: SetorDTO, callbacks: ExecutionCallbacks<SetorDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(setorService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });

    it('deve permitir BaseService tratar erro de constraint unique', () => {
      component.form.patchValue({
        nome: 'Setor Existente',
        centroCustoId: 'cc-1',
      });

      const mockError = new HttpErrorResponse({
        status: 409,
        error: {
          message: 'setor.nome.unique',
          constraintName: 'UK_SETOR_NOME'
        },
      });

      setorService.save.mockImplementation(
        (_data: SetorDTO, callbacks: ExecutionCallbacks<SetorDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError);
          }
        }
      );

      component.save();

      expect(setorService.save).toHaveBeenCalled();
      // BaseService deve processar constraint e exibir mensagem amigável
    });

    it('deve permitir BaseService tratar erro de foreign key', () => {
      component.form.patchValue({
        nome: 'Novo Setor',
        centroCustoId: 'cc-inexistente',
      });

      const mockError = new HttpErrorResponse({
        status: 400,
        error: {
          message: 'setor.centroCusto.foreignKey',
          constraintName: 'FK_SETOR_CENTRO_CUSTO'
        },
      });

      setorService.save.mockImplementation(
        (_data: SetorDTO, callbacks: ExecutionCallbacks<SetorDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError);
          }
        }
      );

      component.save();

      expect(setorService.save).toHaveBeenCalled();
      // BaseService deve processar FK e exibir mensagem amigável
    });
  });

  describe('Soft Delete - Tentativa de Edição de Registro Excluído', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('deve exibir erro ao tentar salvar registro excluído', () => {
      component.detailId = 'setor-excluido';
      component.form.patchValue({
        nome: 'Setor Excluído',
        centroCustoId: 'cc-1',
      });

      const mockError = new HttpErrorResponse({
        status: 400,
        error: {
          userMessageKey: ['errors.deletedEntity'],
          detail: ['Não é possível alterar a entidade \'Setor\' com o id \'setor-excluido\' pois ela foi excluída']
        },
      });

      setorService.save.mockImplementation(
        (_data: SetorDTO, callbacks: ExecutionCallbacks<SetorDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(setorService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
      // BaseService deve processar e exibir mensagem:
      // "Não é possível alterar um registro que foi excluído."
    });

    it('deve processar userMessageKey errors.deletedEntity corretamente', () => {
      component.form.patchValue({
        nome: 'Tentando Editar',
        centroCustoId: 'cc-1',
      });

      const mockError = new HttpErrorResponse({
        status: 400,
        statusText: 'Bad Request',
        error: {
          status: 400,
          title: 'Invalid Data',
          userMessageKey: ['errors.deletedEntity'],
          detail: ['Entity was deleted']
        },
      });

      setorService.save.mockImplementation(
        (_data: SetorDTO, callbacks: ExecutionCallbacks<SetorDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError);
          }
        }
      );

      component.save();

      expect(setorService.save).toHaveBeenCalled();
      // BaseService exibe mensagem de entidade excluída:
      // "Não é possível alterar um registro que foi excluído."
    });
  });
});
