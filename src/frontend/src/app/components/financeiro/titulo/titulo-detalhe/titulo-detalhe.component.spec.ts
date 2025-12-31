import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TituloDetalheComponent } from './titulo-detalhe.component';
import { TituloService } from '../titulo.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { of } from 'rxjs';
import { RouteConstants } from '../../../base/constants/route-constants';
import { TituloDTO } from '../model/titulo-dto';
import { ExecutionCallbacks } from '../../../base/base-service';

describe('TituloDetalheComponent', () => {
  let component: TituloDetalheComponent;
  let fixture: ComponentFixture<TituloDetalheComponent>;
  let tituloService: jest.Mocked<TituloService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;

  beforeEach(async () => {
    // Criar mocks dos serviços
    const tituloServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
      listarPessoasDisponiveis: jest.fn(),
      listarUnidadesDisponiveis: jest.fn(),
      listarCategoriasDisponiveis: jest.fn(),
    };

    const messageServiceMock = {
      sucesso: jest.fn(),
      erro: jest.fn(),
      info: jest.fn(),
    };

    const authServiceMock = {
      hasAuthorityEditarToModulo: jest.fn(),
      getDefaultUnidadeNegocio: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [TituloDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    })
      .overrideComponent(TituloDetalheComponent, {
        set: {
          providers: [{ provide: TituloService, useValue: tituloServiceMock }],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(TituloDetalheComponent);
    component = fixture.componentInstance;
    tituloService = fixture.debugElement.injector.get(
      TituloService
    ) as jest.Mocked<TituloService>;
    messageService = TestBed.inject(
      MessageService
    ) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    // Setup padrão
    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
    authServiceMock.getDefaultUnidadeNegocio.mockReturnValue({
      id: 'un-default',
      nome: 'Unidade Padrão',
      codigo: '001',
    });
    tituloServiceMock.listarPessoasDisponiveis.mockReturnValue(of([]));
    tituloServiceMock.listarUnidadesDisponiveis.mockReturnValue(of([]));
    tituloServiceMock.listarCategoriasDisponiveis.mockReturnValue(of([]));
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Inicialização', () => {
    it('deve criar o componente', () => {
      expect(component).toBeTruthy();
    });

    it('deve inicializar formulário com todos os campos', () => {
      component.id = RouteConstants.P_ADD; // Define como novo para evitar chamada a findById
      component.ngOnInit();

      expect(component.form.get('tipo')).toBeTruthy();
      expect(component.form.get('status')).toBeTruthy();
      expect(component.form.get('numeroDocumento')).toBeTruthy();
      expect(component.form.get('descricao')).toBeTruthy();
      expect(component.form.get('tituloCategoria')).toBeTruthy();
      expect(component.form.get('valorOriginal')).toBeTruthy();
      expect(component.form.get('dataEmissao')).toBeTruthy();
      expect(component.form.get('dataVencimento')).toBeTruthy();
      expect(component.form.get('unidadeNegocio')).toBeTruthy();
    });

    it('deve configurar toolbar com ações de cancelar e salvar', () => {
      component.id = RouteConstants.P_ADD;
      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(2);
      expect(component.toolbarActions[0].icon).toBe('close');
      expect(component.toolbarActions[1].icon).toBe('save');
    });
  });

  describe('Carregar Dados - Comboboxes', () => {
    it('deve carregar categorias disponíveis no combobox', async () => {
      // Arrange
      const mockCategorias = [
        { id: 'cat-1', codigo: '001', nome: 'Despesa Operacional' },
        { id: 'cat-2', codigo: '002', nome: 'Receita de Serviços' },
      ];
      tituloService.listarCategoriasDisponiveis.mockReturnValue(
        of(mockCategorias)
      );

      // Act
      component.loadCategorias();
      await fixture.whenStable();

      // Assert
      expect(component.allCategorias).toEqual(mockCategorias);
      expect(component.allCategorias.length).toBe(2);
      expect(component.allCategorias[0].nome).toBe('Despesa Operacional');
    });

    it('deve carregar pessoas disponíveis para autocomplete', async () => {
      // Arrange
      const mockPessoas = [
        { id: 'pessoa-1', nome: 'João Silva' },
        { id: 'pessoa-2', nome: 'Maria Santos' },
      ];
      tituloService.listarPessoasDisponiveis.mockReturnValue(of(mockPessoas));

      // Act
      component.loadPessoas();
      await fixture.whenStable();

      // Assert
      expect(component.allPessoas).toEqual(mockPessoas);
      expect(component.allPessoas.length).toBe(2);
    });

    it('deve carregar unidades de negócio disponíveis', async () => {
      // Arrange
      const mockUnidades = [
        { id: 'un-1', codigo: '001', nome: 'Matriz' },
        { id: 'un-2', codigo: '002', nome: 'Filial' },
      ];
      tituloService.listarUnidadesDisponiveis.mockReturnValue(of(mockUnidades));

      // Act
      component.loadUnidadesNegocio();
      await fixture.whenStable();

      // Assert
      expect(component.allUnidadesNegocio).toEqual(mockUnidades);
    });

    it('deve definir unidade padrão ao criar novo título', async () => {
      // Arrange
      component.id = RouteConstants.P_ADD;
      const defaultUnidade = {
        id: 'un-default',
        codigo: '001',
        nome: 'Unidade Padrão',
      };
      authService.getDefaultUnidadeNegocio.mockReturnValue(defaultUnidade);
      tituloService.listarUnidadesDisponiveis.mockReturnValue(
        of([defaultUnidade])
      );

      // Act
      component.ngOnInit();
      await fixture.whenStable();

      // Assert
      expect(component.form.get('unidadeNegocio')?.value).toBe('un-default');
    });
  });

  describe('Autocomplete de Pessoa', () => {
    beforeEach(() => {
      component.allPessoas = [
        { id: '1', nome: 'João Silva' },
        { id: '2', nome: 'Maria Santos' },
        { id: '3', nome: 'José Oliveira' },
      ];
    });

    it('deve filtrar pessoas por nome', () => {
      component.searchPessoas({ query: 'maria' });

      expect(component.pessoaSuggestions.length).toBe(1);
      expect(component.pessoaSuggestions[0].nome).toBe('Maria Santos');
    });

    it('deve filtrar parcialmente', () => {
      component.searchPessoas({ query: 'jo' });

      expect(component.pessoaSuggestions.length).toBe(2); // João e José
    });

    it('deve definir pessoa ao selecionar no autocomplete', () => {
      const pessoa = { id: 'pessoa-123', nome: 'João Silva' };

      component.onPessoaSelect(pessoa);

      expect(component.titulo.pessoaId).toBe('pessoa-123');
      expect(component.titulo.pessoaNome).toBe('João Silva');
    });
  });

  describe('Rateio de Setores', () => {
    it('deve atualizar setores selecionados quando mudança ocorre', () => {
      const novosSetores = [
        {
          setorId: 'setor-1',
          setorNome: 'Administrativo',
          percentualRateio: 60,
        },
        { setorId: 'setor-2', setorNome: 'Comercial', percentualRateio: 40 },
      ];

      component.onSetoresChange(novosSetores);

      expect(component.setoresSelecionados).toEqual(novosSetores);
    });

    it('deve retornar valorOriginal para rateio', () => {
      component.initForm();
      component.form.patchValue({ valorOriginal: 5000 });

      expect(component.valorOriginalForRateio).toBe(5000);
    });

    it('deve retornar 0 se valorOriginal não definido', () => {
      component.initForm();
      component.form.patchValue({ valorOriginal: null });

      expect(component.valorOriginalForRateio).toBe(0);
    });

    it('deve validar que setoresValidos retorna false quando soma != 100%', () => {
      component.setoresSelecionados = [
        { setorId: 'setor-1', setorNome: 'Admin', percentualRateio: 70 },
      ];

      expect(component.setoresValidos).toBe(false);
    });

    it('deve validar que setoresValidos retorna true quando soma = 100%', () => {
      component.setoresSelecionados = [
        { setorId: 'setor-1', setorNome: 'Admin', percentualRateio: 60 },
        { setorId: 'setor-2', setorNome: 'Comercial', percentualRateio: 40 },
      ];

      expect(component.setoresValidos).toBe(true);
    });
  });

  describe('Controles do Formulário', () => {
    beforeEach(() => {
      component.initForm();
    });

    it('deve retornar opções de tipo corretas', () => {
      expect(component.tiposOptions.length).toBe(2);
      expect(component.tiposOptions[0].value).toBe('A_PAGAR');
      expect(component.tiposOptions[1].value).toBe('A_RECEBER');
    });

    it('deve retornar opções de status corretas', () => {
      expect(component.statusOptions.length).toBe(5);
      expect(component.statusOptions.map((s) => s.value)).toContain('ABERTO');
      expect(component.statusOptions.map((s) => s.value)).toContain('PAGO');
      expect(component.statusOptions.map((s) => s.value)).toContain(
        'CANCELADO'
      );
    });
  });

  describe('Validações ao Salvar', () => {
    beforeEach(() => {
      component.id = RouteConstants.P_ADD;
      component.ngOnInit();
      component.initForm();
    });

    it('NÃO deve salvar se formulário está inválido', () => {
      // Arrange
      component.form.setErrors({ invalid: true });

      // Act
      component.salvar();

      // Assert
      expect(messageService.erro).toHaveBeenCalled();
      const callArgs = messageService.erro.mock.calls[0][0];
      expect(callArgs).toContain('inválido');
      expect(tituloService.save).not.toHaveBeenCalled();
    });

    it('NÃO deve salvar se pessoa não foi selecionada', () => {
      // Arrange
      component.form.patchValue({
        tipo: 'A_PAGAR',
        tituloCategoria: 'cat-1',
        unidadeNegocio: 'un-1',
        valorOriginal: 1000,
      });
      component.setoresSelecionados = [
        { setorId: 'setor-1', setorNome: 'Admin', percentualRateio: 100 },
      ];
      component.titulo.pessoaId = ''; // Pessoa não selecionada

      // Act
      component.salvar();

      // Assert
      expect(messageService.erro).toHaveBeenCalled();
      const callArgs = messageService.erro.mock.calls[0][0];
      expect(callArgs).toContain('Pessoa');
      expect(tituloService.save).not.toHaveBeenCalled();
    });

    it('NÃO deve salvar se unidade de negócio não foi selecionada', () => {
      // Arrange
      component.titulo.pessoaId = 'pessoa-1';
      component.form.patchValue({
        tipo: 'A_PAGAR',
        tituloCategoria: 'cat-1',
        unidadeNegocio: '', // Vazio
        valorOriginal: 1000,
      });
      component.setoresSelecionados = [
        { setorId: 'setor-1', setorNome: 'Admin', percentualRateio: 100 },
      ];

      // Act
      component.salvar();

      // Assert
      expect(messageService.erro).toHaveBeenCalled();
      const callArgs = messageService.erro.mock.calls[0][0];
      expect(callArgs).toContain('Unidade');
      expect(tituloService.save).not.toHaveBeenCalled();
    });

    it('NÃO deve salvar se categoria não foi selecionada', () => {
      // Arrange
      component.titulo.pessoaId = 'pessoa-1';
      component.form.patchValue({
        tipo: 'A_PAGAR',
        tituloCategoria: null, // Categoria não selecionada
        unidadeNegocio: 'un-1',
        valorOriginal: 1000,
      });
      component.setoresSelecionados = [
        { setorId: 'setor-1', setorNome: 'Admin', percentualRateio: 100 },
      ];

      // Act
      component.salvar();

      // Assert
      expect(messageService.erro).toHaveBeenCalled();
      const callArgs = messageService.erro.mock.calls[0][0];
      expect(callArgs).toContain('Categoria');
      expect(tituloService.save).not.toHaveBeenCalled();
    });

    it('NÃO deve salvar se setores não somam 100%', () => {
      // Arrange
      component.titulo.pessoaId = 'pessoa-1';
      component.form.patchValue({
        tipo: 'A_PAGAR',
        tituloCategoria: 'cat-1',
        unidadeNegocio: 'un-1',
        valorOriginal: 1000,
      });
      component.setoresSelecionados = [
        { setorId: 'setor-1', setorNome: 'Admin', percentualRateio: 60 },
        { setorId: 'setor-2', setorNome: 'Vendas', percentualRateio: 30 },
        // Soma = 90%, não 100%
      ];

      // Act
      component.salvar();

      // Assert
      expect(messageService.erro).toHaveBeenCalled();
      const callArgs = messageService.erro.mock.calls[0][0];
      expect(callArgs).toContain('100');
      expect(tituloService.save).not.toHaveBeenCalled();
    });

    it('NÃO deve salvar se não há setores selecionados', () => {
      // Arrange
      component.titulo.pessoaId = 'pessoa-1';
      component.form.patchValue({
        tipo: 'A_PAGAR',
        tituloCategoria: 'cat-1',
        unidadeNegocio: 'un-1',
        valorOriginal: 1000,
      });
      component.setoresSelecionados = []; // Nenhum setor

      // Act
      component.salvar();

      // Assert
      expect(messageService.erro).toHaveBeenCalled();
      const callArgs = messageService.erro.mock.calls[0][0];
      expect(callArgs).toContain('setor');
      expect(tituloService.save).not.toHaveBeenCalled();
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.id = RouteConstants.P_ADD;
      component.ngOnInit();
      component.initForm();
    });

    it('deve salvar título com sucesso e exibir mensagem', async () => {
      // Arrange
      component.titulo.pessoaId = 'pessoa-1';
      component.titulo.pessoaNome = 'João Silva';
      component.form.patchValue({
        tipo: 'A_PAGAR',
        status: 'ABERTO',
        numeroDocumento: 'NF-001',
        descricao: 'Título Teste',
        tituloCategoria: 'cat-1',
        unidadeNegocio: 'un-1',
        valorOriginal: 1000,
        valorDesconto: 50,
        valorJuros: 0,
        valorMulta: 0,
        dataEmissao: new Date('2025-01-01'),
        dataVencimento: new Date('2025-01-31'),
        observacoes: 'Obs teste',
        rateioAutomatico: false,
      });
      component.setoresSelecionados = [
        { setorId: 'setor-1', setorNome: 'Admin', percentualRateio: 60 },
        { setorId: 'setor-2', setorNome: 'Vendas', percentualRateio: 40 },
      ];

      const mockSavedTitulo = {
        id: 'titulo-123',
        descricao: 'Título Teste',
      };

      tituloService.save.mockImplementation(
        (_data: TituloDTO, callbacks: ExecutionCallbacks<TituloDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess(mockSavedTitulo as TituloDTO);
          }
        }
      );

      const backEventSpy = jest.fn();
      component.backEvent.subscribe(backEventSpy);

      // Act
      component.salvar();
      await fixture.whenStable();

      // Assert
      expect(tituloService.save).toHaveBeenCalled();
      const callArgs = tituloService.save.mock.calls[0][0];
      expect(callArgs.pessoaId).toBe('pessoa-1');
      expect(callArgs.unidadeNegocioId).toBe('un-1');
      expect(callArgs.tituloCategoriaId).toBe('cat-1');
      expect(callArgs.tipo).toBe('A_PAGAR');
      // Status NÃO é enviado - será calculado pelo backend
      expect(callArgs.numeroDocumento).toBe('NF-001');
      expect(callArgs.descricao).toBe('Título Teste');
      expect(callArgs.valorOriginal).toBe(1000);
      expect(callArgs.valorDesconto).toBe(50);
      if (callArgs.setores) {
        expect(callArgs.setores.length).toBe(2);
        expect(callArgs.setores[0].setorId).toBe('setor-1');
        expect(callArgs.setores[0].percentualRateio).toBe(60);
        expect(callArgs.setores[1].setorId).toBe('setor-2');
        expect(callArgs.setores[1].percentualRateio).toBe(40);
      }
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(backEventSpy).toHaveBeenCalled();
    });

    it('deve mapear setores corretamente ao salvar', () => {
      // Arrange
      component.titulo.pessoaId = 'pessoa-1';
      component.form.patchValue({
        tipo: 'A_RECEBER',
        tituloCategoria: 'cat-2',
        unidadeNegocio: 'un-2',
        valorOriginal: 5000,
      });
      component.setoresSelecionados = [
        {
          setorId: 'setor-adm',
          setorNome: 'Administrativo',
          percentualRateio: 100,
        },
      ];

      tituloService.save.mockImplementation(
        (_data: TituloDTO, callbacks: ExecutionCallbacks<TituloDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'titulo-456' } as TituloDTO);
          }
        }
      );

      // Act
      component.salvar();

      // Assert
      const callArgs = tituloService.save.mock.calls[0][0];
      expect(callArgs.setores).toBeDefined();
      if (callArgs.setores) {
        expect(callArgs.setores.length).toBe(1);
        expect(callArgs.setores[0]).toEqual({
          setorId: 'setor-adm',
          setorNome: 'Administrativo',
          percentualRateio: 100,
        });
      }
    });

    it('deve definir valores padrão para campos opcionais ao salvar', () => {
      // Arrange
      component.titulo.pessoaId = 'pessoa-1';
      component.form.patchValue({
        tipo: 'A_PAGAR',
        tituloCategoria: 'cat-1',
        unidadeNegocio: 'un-1',
        valorOriginal: 1000,
        valorDesconto: null,
        valorJuros: null,
        valorMulta: null,
        rateioAutomatico: null,
      });
      component.setoresSelecionados = [
        { setorId: 'setor-1', setorNome: 'Admin', percentualRateio: 100 },
      ];

      tituloService.save.mockImplementation(
        (_data: TituloDTO, callbacks: ExecutionCallbacks<TituloDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'titulo-789' } as TituloDTO);
          }
        }
      );

      // Act
      component.salvar();

      // Assert
      const callArgs = tituloService.save.mock.calls[0][0];
      expect(callArgs.valorDesconto).toBe(0);
      expect(callArgs.valorJuros).toBe(0);
      expect(callArgs.valorMulta).toBe(0);
      expect(callArgs.rateioAutomatico).toBe(false);
    });
  });
});
