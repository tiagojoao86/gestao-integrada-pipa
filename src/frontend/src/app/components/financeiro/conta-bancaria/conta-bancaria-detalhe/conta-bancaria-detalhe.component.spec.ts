import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ContaBancariaDetalheComponent } from './conta-bancaria-detalhe.component';
import { ContaBancariaService } from '../conta-bancaria.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { of } from 'rxjs';
import { ContaBancariaDTO } from '../model/conta-bancaria-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { UsuarioUnidadeNegocioDTO } from '../../../cadastro/usuario/model/usuario-unidade-negocio-dto';
import { Response } from '../../../base/model/response';

describe('ContaBancariaDetalheComponent', () => {
  let component: ContaBancariaDetalheComponent;
  let fixture: ComponentFixture<ContaBancariaDetalheComponent>;
  let contaBancariaService: jest.Mocked<ContaBancariaService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;

  const mockUnidadesNegocio: UsuarioUnidadeNegocioDTO[] = [
    { id: 'un-1', nome: 'Unidade 1', codigo: 'UN01' } as UsuarioUnidadeNegocioDTO,
    { id: 'un-2', nome: 'Unidade 2', codigo: 'UN02' } as UsuarioUnidadeNegocioDTO,
  ];

  beforeEach(async () => {
    const contaBancariaServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
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
      imports: [ContaBancariaDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    })
      .overrideComponent(ContaBancariaDetalheComponent, {
        set: {
          providers: [
            { provide: ContaBancariaService, useValue: contaBancariaServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ContaBancariaDetalheComponent);
    component = fixture.componentInstance;
    contaBancariaService = fixture.debugElement.injector.get(
      ContaBancariaService
    ) as jest.Mocked<ContaBancariaService>;
    messageService = TestBed.inject(
      MessageService
    ) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    // Mocks padrão
    contaBancariaServiceMock.findById.mockReturnValue(
      of({ body: new ContaBancariaDTO() } as Response<ContaBancariaDTO>)
    );

    contaBancariaServiceMock.listarUnidadesDisponiveis.mockReturnValue(
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

      expect(component.form.get('nome')).toBeTruthy();
      expect(component.form.get('banco')).toBeTruthy();
      expect(component.form.get('agencia')).toBeTruthy();
      expect(component.form.get('numeroConta')).toBeTruthy();
      expect(component.form.get('tipo')).toBeTruthy();
      expect(component.form.get('saldoInicial')).toBeTruthy();
      expect(component.form.get('ativa')).toBeTruthy();
      expect(component.form.get('unidadeNegocio')).toBeTruthy();
    });

    it('deve inicializar form com valores padrão', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.form.get('tipo')?.value).toBe('CORRENTE');
      expect(component.form.get('saldoInicial')?.value).toBe(0);
      expect(component.form.get('ativa')?.value).toBe(true);
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

    it('deve carregar unidades de negócio ao criar nova conta', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(contaBancariaService.listarUnidadesDisponiveis).toHaveBeenCalled();
    });

    it('deve definir unidade de negócio padrão do usuário ao criar novo', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(authService.getDefaultUnidadeNegocio).toHaveBeenCalled();
      expect(component.form.get('unidadeNegocio')?.value).toBe('un-1');
    });
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados da conta bancária ao editar', () => {
      const mockConta = new ContaBancariaDTO(
        'cb-1',
        'Conta Principal',
        'Banco do Brasil',
        '1234',
        '56789-0',
        'CORRENTE',
        1000,
        'un-1',
        'Unidade 1',
        'UN01',
        true
      );

      contaBancariaService.findById.mockReturnValue(
        of({ body: mockConta } as Response<ContaBancariaDTO>)
      );

      component.detailId = 'cb-1';
      component.ngOnInit();

      expect(contaBancariaService.findById).toHaveBeenCalledWith('cb-1');
      expect(component.contaBancaria.nome).toBe('Conta Principal');
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(contaBancariaService.findById).not.toHaveBeenCalled();
      expect(component.editMode).toBe(false);
    });

    it('deve preencher form com dados carregados usando fillForm', () => {
      const mockConta = new ContaBancariaDTO(
        'cb-2',
        'Conta Poupança',
        'Caixa Econômica',
        '9876',
        '12345-6',
        'POUPANCA',
        5000,
        'un-2',
        'Unidade 2',
        'UN02',
        false
      );

      contaBancariaService.findById.mockReturnValue(
        of({ body: mockConta } as Response<ContaBancariaDTO>)
      );

      component.detailId = 'cb-2';
      component.ngOnInit();

      expect(component.form.get('nome')?.value).toBe('Conta Poupança');
      expect(component.form.get('banco')?.value).toBe('Caixa Econômica');
      expect(component.form.get('agencia')?.value).toBe('9876');
      expect(component.form.get('numeroConta')?.value).toBe('12345-6');
      expect(component.form.get('tipo')?.value).toBe('POUPANCA');
      expect(component.form.get('saldoInicial')?.value).toBe(5000);
      expect(component.form.get('ativa')?.value).toBe(false);
      expect(component.form.get('unidadeNegocio')?.value).toBe('un-2');
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve salvar conta bancária com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        nome: 'Conta Nova',
        banco: 'Banco Teste',
        agencia: '1111',
        numeroConta: '22222-3',
        tipo: 'CORRENTE',
        saldoInicial: 1500,
        ativa: true,
        unidadeNegocio: 'un-1',
      });

      contaBancariaService.save.mockImplementation(
        (_data: ContaBancariaDTO, callbacks: ExecutionCallbacks<ContaBancariaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess(
              new ContaBancariaDTO('cb-123', 'Conta Nova', 'Banco Teste', '1111', '22222-3', 'CORRENTE', 1500, 'un-1')
            );
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.salvar();

      expect(contaBancariaService.save).toHaveBeenCalled();
      const callArgs = contaBancariaService.save.mock.calls[0][0];
      expect(callArgs.nome).toBe('Conta Nova');
      expect(callArgs.banco).toBe('Banco Teste');
      expect(callArgs.agencia).toBe('1111');
      expect(callArgs.numeroConta).toBe('22222-3');
      expect(callArgs.tipo).toBe('CORRENTE');
      expect(callArgs.saldoInicial).toBe(1500);
      expect(callArgs.ativa).toBe(true);
      expect(callArgs.unidadeNegocioId).toBe('un-1');
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('NÃO deve salvar se unidadeNegocio não foi selecionada', () => {
      component.form.patchValue({
        nome: 'Conta Teste',
        tipo: 'CORRENTE',
        unidadeNegocio: '',
      });

      component.salvar();

      expect(messageService.erro).toHaveBeenCalled();
      expect(contaBancariaService.save).not.toHaveBeenCalled();
    });

    it('deve incluir id ao salvar quando está editando', () => {
      component.contaBancaria.id = 'cb-456';
      component.form.patchValue({
        nome: 'Conta Editada',
        tipo: 'POUPANCA',
        saldoInicial: 2000,
        ativa: true,
        unidadeNegocio: 'un-1',
      });

      contaBancariaService.save.mockImplementation(
        (_data: ContaBancariaDTO, callbacks: ExecutionCallbacks<ContaBancariaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cb-456' } as ContaBancariaDTO);
          }
        }
      );

      component.salvar();

      const callArgs = contaBancariaService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('cb-456');
    });

    it('NÃO deve incluir id ao salvar quando é novo', () => {
      component.contaBancaria = {
        nome: '',
        tipo: 'CORRENTE',
        ativa: true,
      } as ContaBancariaDTO;

      component.form.patchValue({
        nome: 'Conta Nova',
        tipo: 'CORRENTE',
        unidadeNegocio: 'un-1',
      });

      contaBancariaService.save.mockImplementation(
        (_data: ContaBancariaDTO, callbacks: ExecutionCallbacks<ContaBancariaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cb-new' } as ContaBancariaDTO);
          }
        }
      );

      component.salvar();

      const callArgs = contaBancariaService.save.mock.calls[0][0];
      expect(callArgs.id).toBeUndefined();
    });

    it('deve salvar conta inativa (ativa=false)', () => {
      component.form.patchValue({
        nome: 'Conta Inativa',
        tipo: 'CORRENTE',
        ativa: false,
        unidadeNegocio: 'un-1',
      });

      contaBancariaService.save.mockImplementation(
        (_data: ContaBancariaDTO, callbacks: ExecutionCallbacks<ContaBancariaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cb-inactive' } as ContaBancariaDTO);
          }
        }
      );

      component.salvar();

      const callArgs = contaBancariaService.save.mock.calls[0][0];
      expect(callArgs.ativa).toBe(false);
    });

    it('deve salvar conta do tipo POUPANCA', () => {
      component.form.patchValue({
        nome: 'Poupança',
        tipo: 'POUPANCA',
        saldoInicial: 3000,
        unidadeNegocio: 'un-1',
      });

      contaBancariaService.save.mockImplementation(
        (_data: ContaBancariaDTO, callbacks: ExecutionCallbacks<ContaBancariaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cb-poupanca' } as ContaBancariaDTO);
          }
        }
      );

      component.salvar();

      const callArgs = contaBancariaService.save.mock.calls[0][0];
      expect(callArgs.tipo).toBe('POUPANCA');
    });

    it('deve salvar conta sem campos opcionais (banco, agencia, numeroConta)', () => {
      component.form.patchValue({
        nome: 'Caixa',
        banco: '',
        agencia: '',
        numeroConta: '',
        tipo: 'CAIXA',
        saldoInicial: 500,
        ativa: true,
        unidadeNegocio: 'un-1',
      });

      contaBancariaService.save.mockImplementation(
        (_data: ContaBancariaDTO, callbacks: ExecutionCallbacks<ContaBancariaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cb-caixa' } as ContaBancariaDTO);
          }
        }
      );

      component.salvar();

      const callArgs = contaBancariaService.save.mock.calls[0][0];
      expect(callArgs.nome).toBe('Caixa');
      expect(callArgs.banco).toBe('');
      expect(callArgs.agencia).toBe('');
      expect(callArgs.numeroConta).toBe('');
    });

    it('deve salvar com saldoInicial zero', () => {
      component.form.patchValue({
        nome: 'Conta Zerada',
        tipo: 'CORRENTE',
        saldoInicial: 0,
        unidadeNegocio: 'un-1',
      });

      contaBancariaService.save.mockImplementation(
        (_data: ContaBancariaDTO, callbacks: ExecutionCallbacks<ContaBancariaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cb-zero' } as ContaBancariaDTO);
          }
        }
      );

      component.salvar();

      const callArgs = contaBancariaService.save.mock.calls[0][0];
      expect(callArgs.saldoInicial).toBe(0);
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
      component.form.get('nome')?.setValue('Conta Válida');

      expect(component.isControlInvalid('nome')).toBe(false);
    });

    it('deve retornar false quando controle não foi tocado', () => {
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
        nome: 'Conta Duplicada',
        tipo: 'CORRENTE',
        unidadeNegocio: 'un-1',
      });

      const mockError = {
        status: 400,
        error: { message: 'conta-bancaria.nome.unique' },
      };

      contaBancariaService.save.mockImplementation(
        (_data: ContaBancariaDTO, callbacks: ExecutionCallbacks<ContaBancariaDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.salvar();

      expect(contaBancariaService.save).toHaveBeenCalled();
      // BaseService deve processar o erro e exibir mensagem via MessageService
    });

    it('NÃO deve fechar detalhe quando há erro do backend', () => {
      component.form.patchValue({
        nome: 'Conta Erro',
        tipo: 'CORRENTE',
        unidadeNegocio: 'un-1',
      });

      const mockError = {
        status: 500,
        error: { message: 'Erro interno do servidor' },
      };

      contaBancariaService.save.mockImplementation(
        (_data: ContaBancariaDTO, callbacks: ExecutionCallbacks<ContaBancariaDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.salvar();

      expect(contaBancariaService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });

    it('deve permitir BaseService tratar erro de constraint unique', () => {
      component.form.patchValue({
        nome: 'Conta Existente',
        tipo: 'CORRENTE',
        unidadeNegocio: 'un-1',
      });

      const mockError = {
        status: 409,
        error: {
          message: 'conta-bancaria.nome.unique',
          constraintName: 'UK_CONTA_BANCARIA_NOME'
        },
      };

      contaBancariaService.save.mockImplementation(
        (_data: ContaBancariaDTO, callbacks: ExecutionCallbacks<ContaBancariaDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.salvar();

      expect(contaBancariaService.save).toHaveBeenCalled();
      // BaseService deve processar constraint e exibir mensagem amigável
    });

    it('deve permitir BaseService tratar erro de foreign key', () => {
      component.form.patchValue({
        nome: 'Conta FK',
        tipo: 'CORRENTE',
        unidadeNegocio: 'un-inexistente',
      });

      const mockError = {
        status: 400,
        error: {
          message: 'conta-bancaria.unidadeNegocio.foreignKey',
          constraintName: 'FK_CONTA_BANCARIA_UNIDADE_NEGOCIO'
        },
      };

      contaBancariaService.save.mockImplementation(
        (_data: ContaBancariaDTO, callbacks: ExecutionCallbacks<ContaBancariaDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.salvar();

      expect(contaBancariaService.save).toHaveBeenCalled();
      // BaseService deve processar FK e exibir mensagem amigável
    });
  });
});
