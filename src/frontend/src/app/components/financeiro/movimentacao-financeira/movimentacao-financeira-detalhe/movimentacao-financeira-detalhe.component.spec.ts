import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { MovimentacaoFinanceiraDetalheComponent } from './movimentacao-financeira-detalhe.component';
import { MovimentacaoFinanceiraService } from '../movimentacao-financeira.service';
import { TituloService } from '../../titulo/titulo.service';
import { ContaBancariaService } from '../../conta-bancaria/conta-bancaria.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { DialogService } from '../../../base/dialog/dialog.service';
import { of } from 'rxjs';
import { MovimentacaoFinanceiraDTO } from '../model/movimentacao-financeira.dto';
import { TituloDTO } from '../../titulo/model/titulo-dto';
import { ContaBancariaDTO } from '../../conta-bancaria/model/conta-bancaria-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { Response } from '../../../base/model/response';

describe('MovimentacaoFinanceiraDetalheComponent', () => {
  let component: MovimentacaoFinanceiraDetalheComponent;
  let fixture: ComponentFixture<MovimentacaoFinanceiraDetalheComponent>;
  let movimentacaoService: jest.Mocked<MovimentacaoFinanceiraService>;
  let _contaService: jest.Mocked<ContaBancariaService>;
  let messageService: jest.Mocked<MessageService>;

  const authServiceMock = {
    hasAuthorityEditarToModulo: jest.fn().mockReturnValue(true),
    getDefaultUnidadeNegocio: jest.fn().mockReturnValue({
      unidadeNegocioId: '1',
      unidadeNegocioNome: 'Unidade Padrão',
      unidadeNegocioCodigo: 'UP',
      isDefault: true,
    }),
    getUnidadesNegocio: jest.fn().mockReturnValue([
      { unidadeNegocioId: '1', unidadeNegocioNome: 'Unidade Padrão' },
    ]),
  };

  const messageServiceMock = {
    erro: jest.fn(),
    sucesso: jest.fn(),
  };

  const dialogServiceMock = {
    showOk: jest.fn().mockReturnValue(of({})),
    showYesNo: jest.fn().mockReturnValue(of({})),
  };

  const mockTitulos: TituloDTO[] = [
    { id: '1', descricao: 'Título 1', valor: 100 } as TituloDTO,
    { id: '2', descricao: 'Título 2', valor: 200 } as TituloDTO,
    { id: '3', descricao: 'Título 3', valor: 300 } as TituloDTO,
  ];

  const mockContas: ContaBancariaDTO[] = [
    { id: '1', nome: 'Conta 1', numeroConta: '123' } as ContaBancariaDTO,
    { id: '2', nome: 'Conta 2', numeroConta: '456' } as ContaBancariaDTO,
  ];

  const movimentacaoServiceMock = {
    findById: jest.fn(),
    save: jest.fn(),
  };

  const tituloServiceMock = {
    search: jest.fn(),
    findById: jest.fn(),
  };

  const contaServiceMock = {
    listAll: jest.fn(),
    findById: jest.fn(),
  };

  beforeEach(async () => {
    tituloServiceMock.search.mockReturnValue(of(mockTitulos));
    contaServiceMock.listAll.mockReturnValue(of({ body: mockContas }));

    await TestBed.configureTestingModule({
      imports: [MovimentacaoFinanceiraDetalheComponent],
    })
      .overrideComponent(MovimentacaoFinanceiraDetalheComponent, {
        set: {
          providers: [
            {
              provide: MovimentacaoFinanceiraService,
              useValue: movimentacaoServiceMock,
            },
            { provide: TituloService, useValue: tituloServiceMock },
            { provide: ContaBancariaService, useValue: contaServiceMock },
            { provide: MessageService, useValue: messageServiceMock },
            { provide: AuthService, useValue: authServiceMock },
            { provide: DialogService, useValue: dialogServiceMock },
            CurrencyPipe,
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(MovimentacaoFinanceiraDetalheComponent);
    component = fixture.componentInstance;
    movimentacaoService = movimentacaoServiceMock;
    _contaService = contaServiceMock;
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
      component.id = 'add';
      component.ngOnInit();

      expect(component.form.get('titulos')).toBeTruthy();
      expect(component.form.get('contaBancaria')).toBeTruthy();
      expect(component.form.get('tipo')).toBeTruthy();
      expect(component.form.get('formaPagamento')).toBeTruthy();
      expect(component.form.get('valor')).toBeTruthy();
      expect(component.form.get('data')).toBeTruthy();
      expect(component.form.get('unidadeNegocio')).toBeTruthy();
      expect(component.form.get('observacoes')).toBeTruthy();
    });

    it('deve configurar toolbar com ações de cancelar e salvar quando tem permissão', () => {
      authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
      component.id = 'add';

      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(3); // addTitulo, cancelar, salvar
      expect(component.toolbarActions.some((a) => a.icon === 'close')).toBe(true);
      expect(component.toolbarActions.some((a) => a.icon === 'save')).toBe(true);
    });

    it('deve configurar toolbar apenas com ação cancelar quando não tem permissão', () => {
      authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(false);
      component.id = 'add';

      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(1);
      expect(component.toolbarActions[0].icon).toBe('close');
    });

    it('deve carregar unidades disponíveis ao inicializar', () => {
      component.id = 'add';

      component.ngOnInit();

      expect(authServiceMock.getUnidadesNegocio).toHaveBeenCalled();
    });

    it('deve definir data atual ao criar nova movimentação', () => {
      component.id = 'add';

      component.ngOnInit();

      const dataValue = component.form.get('data')?.value;
      expect(dataValue).toBeInstanceOf(Date);
    });

    it('deve definir unidade padrão ao criar nova movimentação', () => {
      component.id = 'add';

      component.ngOnInit();

      expect(authServiceMock.getDefaultUnidadeNegocio).toHaveBeenCalled();
    });
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados da movimentação ao editar', () => {
      const mockMovimentacao: MovimentacaoFinanceiraDTO = {
        id: '1',
        titulos: [{ id: '1', descricao: 'Título 1' }],
        contaBancariaId: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 100,
        data: '2024-01-01',
        unidadeNegocioId: '1',
        observacoes: 'Teste',
      } as MovimentacaoFinanceiraDTO;
      movimentacaoService.findById.mockReturnValue(
        of({ body: mockMovimentacao } as Response<MovimentacaoFinanceiraDTO>)
      );

      component.id = '1';
      component.ngOnInit();

      expect(movimentacaoService.findById).toHaveBeenCalledWith('1');
      expect(component.movimentacao).toEqual(mockMovimentacao);
    });

    it('NÃO deve carregar dados quando id é "add"', () => {
      component.id = 'add';

      component.ngOnInit();

      expect(movimentacaoService.findById).not.toHaveBeenCalled();
    });

    it('deve preencher form com dados carregados', () => {
      const mockMovimentacao: MovimentacaoFinanceiraDTO = {
        id: '1',
        titulos: [],
        contaBancariaId: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 100,
        data: '2024-01-01',
        unidadeNegocioId: '1',
        observacoes: 'Teste',
      } as MovimentacaoFinanceiraDTO;
      movimentacaoService.findById.mockReturnValue(
        of({ body: mockMovimentacao } as Response<MovimentacaoFinanceiraDTO>)
      );

      component.id = '1';
      component.ngOnInit();

      expect(component.form.get('contaBancaria')?.value).toBe('1');
      expect(component.form.get('tipo')?.value).toBe('PAGAMENTO');
      expect(component.form.get('formaPagamento')?.value).toBe('PIX');
      expect(component.form.get('valor')?.value).toBe(100);
      expect(component.form.get('unidadeNegocio')?.value).toBe('1');
      expect(component.form.get('observacoes')?.value).toBe('Teste');
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.id = 'add';
      component.ngOnInit();
    });

    it('deve salvar movimentação com sucesso e fechar detalhe', () => {
      component.selectedTitulos = [mockTitulos[0]];
      component.form.patchValue({
        titulos: [mockTitulos[0]],
        contaBancaria: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 100,
        data: new Date(),
        unidadeNegocio: '1',
        observacoes: 'Teste',
      });

      movimentacaoService.save.mockImplementation(
        (
          _data: MovimentacaoFinanceiraDTO,
          callbacks: ExecutionCallbacks<MovimentacaoFinanceiraDTO>
        ) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({} as MovimentacaoFinanceiraDTO);
          }
        }
      );

      const backSpy = jest.spyOn(component.backEvent, 'emit');

      component.salvar();

      expect(movimentacaoService.save).toHaveBeenCalled();
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(backSpy).toHaveBeenCalled();
    });

    it('NÃO deve salvar se não há títulos selecionados', () => {
      component.selectedTitulos = [];
      component.form.patchValue({
        contaBancaria: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 100,
        data: new Date(),
        unidadeNegocio: '1',
      });

      component.salvar();

      expect(movimentacaoService.save).not.toHaveBeenCalled();
      expect(messageService.erro).toHaveBeenCalled();
    });

    it('NÃO deve salvar se conta bancária não está preenchida', () => {
      component.selectedTitulos = [mockTitulos[0]];
      component.form.patchValue({
        titulos: [mockTitulos[0]],
        contaBancaria: '',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 100,
        data: new Date(),
        unidadeNegocio: '1',
      });

      component.salvar();

      expect(movimentacaoService.save).not.toHaveBeenCalled();
      expect(messageService.erro).toHaveBeenCalled();
    });

    it('NÃO deve salvar se tipo não está preenchido', () => {
      component.selectedTitulos = [mockTitulos[0]];
      component.form.patchValue({
        titulos: [mockTitulos[0]],
        contaBancaria: '1',
        tipo: null,
        formaPagamento: 'PIX',
        valor: 100,
        data: new Date(),
        unidadeNegocio: '1',
      });

      component.salvar();

      expect(movimentacaoService.save).not.toHaveBeenCalled();
      expect(messageService.erro).toHaveBeenCalled();
    });

    it('NÃO deve salvar se forma de pagamento não está preenchida', () => {
      component.selectedTitulos = [mockTitulos[0]];
      component.form.patchValue({
        titulos: [mockTitulos[0]],
        contaBancaria: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: '',
        valor: 100,
        data: new Date(),
        unidadeNegocio: '1',
      });

      component.salvar();

      expect(movimentacaoService.save).not.toHaveBeenCalled();
      expect(messageService.erro).toHaveBeenCalled();
    });

    it('NÃO deve salvar se valor é zero ou negativo', () => {
      component.selectedTitulos = [mockTitulos[0]];
      component.form.patchValue({
        titulos: [mockTitulos[0]],
        contaBancaria: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 0,
        data: new Date(),
        unidadeNegocio: '1',
      });

      component.salvar();

      expect(movimentacaoService.save).not.toHaveBeenCalled();
      expect(messageService.erro).toHaveBeenCalled();
    });

    it('NÃO deve salvar se data não está preenchida', () => {
      component.selectedTitulos = [mockTitulos[0]];
      component.form.patchValue({
        titulos: [mockTitulos[0]],
        contaBancaria: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 100,
        data: null,
        unidadeNegocio: '1',
      });

      component.salvar();

      expect(movimentacaoService.save).not.toHaveBeenCalled();
      expect(messageService.erro).toHaveBeenCalled();
    });

    it('NÃO deve salvar se unidade de negócio não está preenchida', () => {
      component.selectedTitulos = [mockTitulos[0]];
      component.form.patchValue({
        titulos: [mockTitulos[0]],
        contaBancaria: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 100,
        data: new Date(),
        unidadeNegocio: '',
      });

      component.salvar();

      expect(movimentacaoService.save).not.toHaveBeenCalled();
      expect(messageService.erro).toHaveBeenCalled();
    });

    it('deve incluir títulos como objetos MovimentacaoTituloDTO no payload', () => {
      component.selectedTitulos = [mockTitulos[0], mockTitulos[1]];
      component.form.patchValue({
        titulos: [mockTitulos[0], mockTitulos[1]],
        contaBancaria: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 300, // 100 + 200 = soma de selectedTitulos
        data: new Date(),
        unidadeNegocio: '1',
      });

      movimentacaoService.save.mockImplementation(
        (
          _data: MovimentacaoFinanceiraDTO,
          callbacks: ExecutionCallbacks<MovimentacaoFinanceiraDTO>
        ) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({} as MovimentacaoFinanceiraDTO);
          }
        }
      );

      component.salvar();

      const callArgs = movimentacaoService.save.mock.calls[0][0];
      expect(callArgs.titulos.length).toBe(2);
      expect(callArgs.titulos[0]).toHaveProperty('id', '1');
      expect(callArgs.titulos[0]).toHaveProperty('descricao', 'Título 1');
    });

    it('deve incluir id ao salvar quando está editando', () => {
      const mockMovimentacao: MovimentacaoFinanceiraDTO = {
        id: '123',
        titulos: [],
        contaBancariaId: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 100,
        data: '2024-01-01',
        unidadeNegocioId: '1',
      } as MovimentacaoFinanceiraDTO;
      movimentacaoService.findById.mockReturnValue(
        of({ body: mockMovimentacao } as Response<MovimentacaoFinanceiraDTO>)
      );

      component.id = '123';
      component.ngOnInit();

      component.selectedTitulos = [mockTitulos[0]];
      component.form.patchValue({
        titulos: [mockTitulos[0]],
        contaBancaria: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 100,
        data: new Date(),
        unidadeNegocio: '1',
      });

      movimentacaoService.save.mockImplementation(
        (
          _data: MovimentacaoFinanceiraDTO,
          callbacks: ExecutionCallbacks<MovimentacaoFinanceiraDTO>
        ) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({} as MovimentacaoFinanceiraDTO);
          }
        }
      );

      component.salvar();

      const callArgs = movimentacaoService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('123');
    });
  });

  describe('Ações da Toolbar', () => {
    beforeEach(() => {
      component.id = 'add';
      component.ngOnInit();
    });

    it('deve emitir evento ao clicar em cancelar', () => {
      const backSpy = jest.spyOn(component.backEvent, 'emit');

      component.goBackFn();

      expect(backSpy).toHaveBeenCalled();
    });
  });

  describe('Validação de Controles', () => {
    beforeEach(() => {
      component.id = 'add';
      component.ngOnInit();
    });

    it('deve retornar true quando campo é inválido e foi tocado', () => {
      const valorControl = component.form.get('valor');
      valorControl?.markAsTouched();
      valorControl?.setErrors({ required: true });

      expect(component.isControlInvalid('valor')).toBe(true);
    });

    it('deve retornar false quando campo é válido', () => {
      const valorControl = component.form.get('valor');
      valorControl?.setValue(100);
      valorControl?.markAsTouched();

      expect(component.isControlInvalid('valor')).toBe(false);
    });

    it('deve retornar false quando campo não foi tocado', () => {
      const valorControl = component.form.get('valor');
      valorControl?.setErrors({ required: true });

      expect(component.isControlInvalid('valor')).toBe(false);
    });
  });

  describe('Mensagens de Erro do Backend', () => {
    beforeEach(() => {
      component.id = 'add';
      component.ngOnInit();
    });

    it('deve chamar onError quando backend retorna erro', () => {
      component.selectedTitulos = [mockTitulos[0]];
      component.form.patchValue({
        titulos: [mockTitulos[0]],
        contaBancaria: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 100,
        data: new Date(),
        unidadeNegocio: '1',
      });

      movimentacaoService.save.mockImplementation(
        (
          _data: MovimentacaoFinanceiraDTO,
          callbacks: ExecutionCallbacks<MovimentacaoFinanceiraDTO>
        ) => {
          if (callbacks.onError) {
            callbacks.onError({
              error: {
                message: 'ERRO_CONTA_INVALIDA',
              },
            } as unknown as HttpErrorResponse);
          }
        }
      );

      component.salvar();

      expect(movimentacaoService.save).toHaveBeenCalled();
    });

    it('NÃO deve emitir backEvent quando há erro do backend', () => {
      component.selectedTitulos = [mockTitulos[0]];
      component.form.patchValue({
        titulos: [mockTitulos[0]],
        contaBancaria: '1',
        tipo: 'PAGAMENTO',
        formaPagamento: 'PIX',
        valor: 100,
        data: new Date(),
        unidadeNegocio: '1',
      });

      movimentacaoService.save.mockImplementation(
        (
          _data: MovimentacaoFinanceiraDTO,
          callbacks: ExecutionCallbacks<MovimentacaoFinanceiraDTO>
        ) => {
          if (callbacks.onError) {
            callbacks.onError({
              error: {
                message: 'ERRO_CONTA_INVALIDA',
              },
            } as unknown as HttpErrorResponse);
          }
        }
      );

      const backSpy = jest.spyOn(component.backEvent, 'emit');

      component.salvar();

      expect(backSpy).not.toHaveBeenCalled();
    });
  });

  describe('Cleanup', () => {
    it('deve limpar subscriptions ao destruir', () => {
      component.id = 'add';
      component.ngOnInit();

      const destroySpy = jest.spyOn(component['destroy$'], 'next');
      const completeSpy = jest.spyOn(component['destroy$'], 'complete');

      component.ngOnDestroy();

      expect(destroySpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    });
  });
});
