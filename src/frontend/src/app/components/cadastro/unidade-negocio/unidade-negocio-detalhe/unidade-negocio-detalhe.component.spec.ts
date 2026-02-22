import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { UnidadeNegocioDetalheComponent } from './unidade-negocio-detalhe.component';
import { UnidadeNegocioService } from '../unidade-negocio.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { of } from 'rxjs';
import { UnidadeNegocioDTO } from '../model/unidade-negocio-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { Response } from '../../../base/model/response';

describe('UnidadeNegocioDetalheComponent', () => {
  let component: UnidadeNegocioDetalheComponent;
  let fixture: ComponentFixture<UnidadeNegocioDetalheComponent>;
  let unidadeNegocioService: jest.Mocked<UnidadeNegocioService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;

  beforeEach(async () => {
    const unidadeNegocioServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
    };

    const messageServiceMock = {
      sucesso: jest.fn(),
      erro: jest.fn(),
    };

    const authServiceMock = {
      hasAuthorityEditarToModulo: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [UnidadeNegocioDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    })
      .overrideComponent(UnidadeNegocioDetalheComponent, {
        set: {
          providers: [
            { provide: UnidadeNegocioService, useValue: unidadeNegocioServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(UnidadeNegocioDetalheComponent);
    component = fixture.componentInstance;
    unidadeNegocioService = fixture.debugElement.injector.get(
      UnidadeNegocioService
    ) as jest.Mocked<UnidadeNegocioService>;
    messageService = TestBed.inject(
      MessageService
    ) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    // Mock padrão para findById
    unidadeNegocioServiceMock.findById.mockReturnValue(
      of({ body: new UnidadeNegocioDTO('', '', '', undefined, undefined, true) } as Response<UnidadeNegocioDTO>)
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
      component.id = 'add';
      component.ngOnInit();

      expect(component.form.get('codigo')).toBeTruthy();
      expect(component.form.get('nome')).toBeTruthy();
      expect(component.form.get('cnpj')).toBeTruthy();
      expect(component.form.get('descricao')).toBeTruthy();
      expect(component.form.get('ativa')).toBeTruthy();
    });

    it('deve inicializar form com ativa=true como padrão', () => {
      component.id = 'add';
      component.ngOnInit();

      expect(component.form.get('ativa')?.value).toBe(true);
    });

    it('deve configurar toolbar com ações de cancelar e salvar quando tem permissão', () => {
      authService.hasAuthorityEditarToModulo.mockReturnValue(true);

      component.id = 'add';
      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(2);
      expect(component.toolbarActions[0].icon).toBe('close');
      expect(component.toolbarActions[1].icon).toBe('save');
    });

    it('deve configurar toolbar apenas com ação cancelar quando não tem permissão', () => {
      authService.hasAuthorityEditarToModulo.mockReturnValue(false);

      component.id = 'add';
      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(1);
      expect(component.toolbarActions[0].icon).toBe('close');
    });

    it('deve definir editMode=false quando id é "add"', () => {
      component.id = 'add';
      component.ngOnInit();

      expect(component.editMode).toBe(false);
    });

    it('deve definir editMode=true quando id não é "add"', () => {
      component.id = 'un-123';
      component.ngOnInit();

      expect(component.editMode).toBe(true);
    });
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados da unidade ao editar', () => {
      const mockUnidade: UnidadeNegocioDTO = new UnidadeNegocioDTO(
        'un-1',
        'UN01',
        'Unidade Teste',
        'Descrição teste',
        '12.345.678/0001-90',
        true
      );

      unidadeNegocioService.findById.mockReturnValue(
        of({ body: mockUnidade } as Response<UnidadeNegocioDTO>)
      );

      component.id = 'un-1';
      component.ngOnInit();

      expect(unidadeNegocioService.findById).toHaveBeenCalledWith('un-1');
      expect(component.unidadeNegocio.codigo).toBe('UN01');
      expect(component.unidadeNegocio.nome).toBe('Unidade Teste');
    });

    it('NÃO deve carregar dados quando id é "add"', () => {
      component.id = 'add';
      component.ngOnInit();

      expect(unidadeNegocioService.findById).not.toHaveBeenCalled();
      expect(component.editMode).toBe(false);
    });

    it('deve preencher form com dados carregados usando fillForm', () => {
      const mockUnidade: UnidadeNegocioDTO = new UnidadeNegocioDTO(
        'un-2',
        'UN02',
        'Unidade Teste 2',
        'Descrição teste 2',
        '98.765.432/0001-01',
        false
      );

      unidadeNegocioService.findById.mockReturnValue(
        of({ body: mockUnidade } as Response<UnidadeNegocioDTO>)
      );

      component.id = 'un-2';
      component.ngOnInit();

      expect(component.form.get('codigo')?.value).toBe('UN02');
      expect(component.form.get('nome')?.value).toBe('Unidade Teste 2');
      expect(component.form.get('descricao')?.value).toBe('Descrição teste 2');
      expect(component.form.get('cnpj')?.value).toBe('98.765.432/0001-01');
      expect(component.form.get('ativa')?.value).toBe(false);
    });

    it('deve desabilitar campo codigo em modo edição', () => {
      const mockUnidade: UnidadeNegocioDTO = new UnidadeNegocioDTO(
        'un-3',
        'UN03',
        'Unidade Teste 3',
        undefined,
        undefined,
        true
      );

      unidadeNegocioService.findById.mockReturnValue(
        of({ body: mockUnidade } as Response<UnidadeNegocioDTO>)
      );

      component.id = 'un-3';
      component.ngOnInit();

      expect(component.form.get('codigo')?.disabled).toBe(true);
    });

    it('NÃO deve desabilitar campo codigo em modo criação', () => {
      component.id = 'add';
      component.ngOnInit();

      expect(component.form.get('codigo')?.disabled).toBe(false);
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.id = 'add';
      component.ngOnInit();
    });

    it('deve salvar unidade com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        codigo: 'UN01',
        nome: 'Nova Unidade',
        descricao: 'Descrição',
        cnpj: '12.345.678/0001-90',
        ativa: true,
      });

      unidadeNegocioService.save.mockImplementation(
        (_data: UnidadeNegocioDTO, callbacks: ExecutionCallbacks<UnidadeNegocioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess(
              new UnidadeNegocioDTO('un-123', 'UN01', 'Nova Unidade', 'Descrição', '12.345.678/0001-90', true)
            );
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetailEvent.subscribe(closeDetailSpy);

      component.salvar();

      expect(unidadeNegocioService.save).toHaveBeenCalled();
      const callArgs = unidadeNegocioService.save.mock.calls[0][0];
      expect(callArgs.codigo).toBe('UN01');
      expect(callArgs.nome).toBe('Nova Unidade');
      expect(callArgs.descricao).toBe('Descrição');
      expect(callArgs.cnpj).toBe('12.345.678/0001-90');
      expect(callArgs.ativa).toBe(true);
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('deve incluir id ao salvar quando está editando', () => {
      component.id = 'un-456';
      component.unidadeNegocio.id = 'un-456';
      component.form.patchValue({
        codigo: 'UN02',
        nome: 'Unidade Editada',
        ativa: true,
      });

      unidadeNegocioService.save.mockImplementation(
        (_data: UnidadeNegocioDTO, callbacks: ExecutionCallbacks<UnidadeNegocioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'un-456' } as UnidadeNegocioDTO);
          }
        }
      );

      component.salvar();

      const callArgs = unidadeNegocioService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('un-456');
      expect(callArgs.codigo).toBe('UN02');
      expect(callArgs.nome).toBe('Unidade Editada');
    });

    it('NÃO deve incluir id ao salvar quando é novo (add)', () => {
      component.id = 'add';
      component.unidadeNegocio = { ativa: true } as UnidadeNegocioDTO;
      component.form.patchValue({
        codigo: 'UN-NEW',
        nome: 'Nova Unidade',
        ativa: true,
      });

      unidadeNegocioService.save.mockImplementation(
        (_data: UnidadeNegocioDTO, callbacks: ExecutionCallbacks<UnidadeNegocioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'un-new' } as UnidadeNegocioDTO);
          }
        }
      );

      component.salvar();

      const callArgs = unidadeNegocioService.save.mock.calls[0][0];
      expect(callArgs.id).toBeUndefined();
      expect(callArgs.codigo).toBe('UN-NEW');
      expect(callArgs.nome).toBe('Nova Unidade');
    });

    it('deve salvar unidade sem descricao (campo opcional)', () => {
      component.form.patchValue({
        codigo: 'UN03',
        nome: 'Unidade Sem Descricao',
        descricao: null,
        cnpj: null,
        ativa: true,
      });

      unidadeNegocioService.save.mockImplementation(
        (_data: UnidadeNegocioDTO, callbacks: ExecutionCallbacks<UnidadeNegocioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'un-789' } as UnidadeNegocioDTO);
          }
        }
      );

      component.salvar();

      expect(unidadeNegocioService.save).toHaveBeenCalled();
      const callArgs = unidadeNegocioService.save.mock.calls[0][0];
      expect(callArgs.codigo).toBe('UN03');
      expect(callArgs.nome).toBe('Unidade Sem Descricao');
      expect(callArgs.descricao).toBeNull();
    });

    it('deve salvar unidade sem CNPJ (campo opcional)', () => {
      component.form.patchValue({
        codigo: 'UN04',
        nome: 'Unidade Sem CNPJ',
        cnpj: null,
        ativa: false,
      });

      unidadeNegocioService.save.mockImplementation(
        (_data: UnidadeNegocioDTO, callbacks: ExecutionCallbacks<UnidadeNegocioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'un-999' } as UnidadeNegocioDTO);
          }
        }
      );

      component.salvar();

      expect(unidadeNegocioService.save).toHaveBeenCalled();
      const callArgs = unidadeNegocioService.save.mock.calls[0][0];
      expect(callArgs.codigo).toBe('UN04');
      expect(callArgs.cnpj).toBeNull();
    });

    it('deve salvar unidade inativa (ativa=false)', () => {
      component.form.patchValue({
        codigo: 'UN05',
        nome: 'Unidade Inativa',
        ativa: false,
      });

      unidadeNegocioService.save.mockImplementation(
        (_data: UnidadeNegocioDTO, callbacks: ExecutionCallbacks<UnidadeNegocioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'un-inactive' } as UnidadeNegocioDTO);
          }
        }
      );

      component.salvar();

      const callArgs = unidadeNegocioService.save.mock.calls[0][0];
      expect(callArgs.ativa).toBe(false);
    });

    it('deve usar getRawValue para obter codigo quando campo está desabilitado', () => {
      component.id = 'un-edit';
      component.unidadeNegocio = new UnidadeNegocioDTO('un-edit', 'UN-EDIT', 'Teste', undefined, undefined, true);
      component.ngOnInit();

      // Simula edição onde codigo fica desabilitado
      component.form.get('codigo')?.disable();
      component.form.patchValue({
        nome: 'Unidade Editada Nome',
        ativa: true,
      });

      unidadeNegocioService.save.mockImplementation(
        (_data: UnidadeNegocioDTO, callbacks: ExecutionCallbacks<UnidadeNegocioDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'un-edit' } as UnidadeNegocioDTO);
          }
        }
      );

      component.salvar();

      const callArgs = unidadeNegocioService.save.mock.calls[0][0];
      // getRawValue deve pegar o valor mesmo com campo desabilitado
      expect(callArgs.codigo).toBeDefined();
    });
  });

  describe('Ações da Toolbar', () => {
    it('deve fechar detalhe ao clicar em cancelar', () => {
      component.id = 'add';
      component.ngOnInit();

      const closeDetailSpy = jest.fn();
      component.closeDetailEvent.subscribe(closeDetailSpy);

      component.goBackFn();

      expect(closeDetailSpy).toHaveBeenCalled();
    });
  });

  describe('Validação de Controles', () => {
    beforeEach(() => {
      component.id = 'add';
      component.ngOnInit();
    });

    it('deve retornar false quando controle é válido', () => {
      component.form.get('nome')?.setValue('Unidade Válida');

      expect(component.isControlInvalid('nome')).toBe(false);
    });

    it('deve retornar false quando controle não foi tocado', () => {
      component.form.get('nome')?.setValue('');

      expect(component.isControlInvalid('nome')).toBe(false);
    });
  });

  describe('Mensagens de Erro do Backend', () => {
    beforeEach(() => {
      component.id = 'add';
      component.ngOnInit();
    });

    it('deve chamar onError quando backend retorna erro', () => {
      component.form.patchValue({
        codigo: 'UN-DUP',
        nome: 'Unidade Duplicada',
        ativa: true,
      });

      const mockError = {
        status: 400,
        error: { message: 'unidade-negocio.codigo.unique' },
      };

      unidadeNegocioService.save.mockImplementation(
        (_data: UnidadeNegocioDTO, callbacks: ExecutionCallbacks<UnidadeNegocioDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.salvar();

      expect(unidadeNegocioService.save).toHaveBeenCalled();
      // BaseService deve processar o erro e exibir mensagem via MessageService
    });

    it('NÃO deve fechar detalhe quando há erro do backend', () => {
      component.form.patchValue({
        codigo: 'UN-ERR',
        nome: 'Unidade Erro',
        ativa: true,
      });

      const mockError = {
        status: 500,
        error: { message: 'Erro interno do servidor' },
      };

      unidadeNegocioService.save.mockImplementation(
        (_data: UnidadeNegocioDTO, callbacks: ExecutionCallbacks<UnidadeNegocioDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetailEvent.subscribe(closeDetailSpy);

      component.salvar();

      expect(unidadeNegocioService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });

    it('deve permitir BaseService tratar erro de constraint unique', () => {
      component.form.patchValue({
        codigo: 'UN-EXIST',
        nome: 'Unidade Existente',
        ativa: true,
      });

      const mockError = {
        status: 409,
        error: {
          message: 'unidade-negocio.codigo.unique',
          constraintName: 'UK_UNIDADE_NEGOCIO_CODIGO'
        },
      };

      unidadeNegocioService.save.mockImplementation(
        (_data: UnidadeNegocioDTO, callbacks: ExecutionCallbacks<UnidadeNegocioDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.salvar();

      expect(unidadeNegocioService.save).toHaveBeenCalled();
      // BaseService deve processar constraint e exibir mensagem amigável
    });

    it('deve permitir BaseService tratar erro de CNPJ inválido', () => {
      component.form.patchValue({
        codigo: 'UN-CNPJ',
        nome: 'Unidade CNPJ Inválido',
        cnpj: '00.000.000/0000-00',
        ativa: true,
      });

      const mockError = {
        status: 400,
        error: {
          message: 'unidade-negocio.cnpj.invalid',
        },
      };

      unidadeNegocioService.save.mockImplementation(
        (_data: UnidadeNegocioDTO, callbacks: ExecutionCallbacks<UnidadeNegocioDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.salvar();

      expect(unidadeNegocioService.save).toHaveBeenCalled();
      // BaseService deve processar erro de validação
    });
  });
});
