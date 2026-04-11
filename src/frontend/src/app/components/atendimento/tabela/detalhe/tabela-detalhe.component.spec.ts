import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TabelaDetalheComponent } from './tabela-detalhe.component';
import { TabelaService } from '../tabela.service';
import { ProcedimentoService } from '../../procedimento/procedimento.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { EntitySearchService } from '../../../base/entity-search/entity-search.service';
import { of } from 'rxjs';
import { TabelaDTO } from '../model/tabela-dto';
import { TabelaItemDTO } from '../model/tabela-item-dto';
import { TipoTabela } from '../model/tipo-tabela.enum';
import { ProcedimentoDTO } from '../../procedimento/model/procedimento-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { Response } from '../../../base/model/response';

describe('TabelaDetalheComponent', () => {
  let component: TabelaDetalheComponent;
  let fixture: ComponentFixture<TabelaDetalheComponent>;
  let tabelaService: jest.Mocked<TabelaService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;

  beforeEach(async () => {
    const tabelaServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
    };

    const procedimentoServiceMock = {
      findById: jest.fn(),
      list: jest.fn(),
    };

    const messageServiceMock = {
      sucesso: jest.fn(),
      erro: jest.fn(),
    };

    const authServiceMock = {
      hasAuthorityEditarToModulo: jest.fn(),
    };

    const entitySearchServiceMock = {
      search: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [TabelaDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: EntitySearchService, useValue: entitySearchServiceMock },
      ],
    })
      .overrideComponent(TabelaDetalheComponent, {
        set: {
          providers: [
            { provide: TabelaService, useValue: tabelaServiceMock },
            { provide: ProcedimentoService, useValue: procedimentoServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(TabelaDetalheComponent);
    component = fixture.componentInstance;
    tabelaService = fixture.debugElement.injector.get(
      TabelaService
    ) as jest.Mocked<TabelaService>;
    messageService = TestBed.inject(MessageService) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
    tabelaServiceMock.findById.mockReturnValue(
      of({ body: new TabelaDTO() } as Response<TabelaDTO>)
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
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.form.get('nome')).toBeTruthy();
      expect(component.form.get('tipo')).toBeTruthy();
      expect(component.form.get('ativo')).toBeTruthy();
    });

    it('deve inicializar form com ativo = true como padrão', () => {
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

    it('deve inicializar lista de tipos de tabela', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.tiposTabela.length).toBe(2);
    });
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados da tabela ao editar', () => {
      const mockTabela = new TabelaDTO();
      mockTabela.id = 'tab-1';
      mockTabela.nome = 'Tabela Particular';
      mockTabela.tipo = TipoTabela.PARTICULAR;
      mockTabela.ativo = true;
      mockTabela.itens = [];

      tabelaService.findById.mockReturnValue(
        of({ body: mockTabela } as Response<TabelaDTO>)
      );

      component.detailId = 'tab-1';
      component.ngOnInit();

      expect(tabelaService.findById).toHaveBeenCalledWith('tab-1');
      expect(component.tabela.nome).toBe('Tabela Particular');
      expect(component.editMode).toBe(true);
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(tabelaService.findById).not.toHaveBeenCalled();
      expect(component.editMode).toBe(false);
    });

    it('deve preencher form com dados carregados', () => {
      const mockTabela = new TabelaDTO();
      mockTabela.nome = 'Tabela Convênio';
      mockTabela.tipo = TipoTabela.CONVENIO;
      mockTabela.ativo = false;
      mockTabela.itens = [];

      tabelaService.findById.mockReturnValue(
        of({ body: mockTabela } as Response<TabelaDTO>)
      );

      component.detailId = 'tab-2';
      component.ngOnInit();

      expect(component.form.get('nome')?.value).toBe('Tabela Convênio');
      expect(component.form.get('tipo')?.value).toBe(TipoTabela.CONVENIO);
      expect(component.form.get('ativo')?.value).toBe(false);
    });

    it('deve carregar itens da tabela ao editar', () => {
      const itemMock = new TabelaItemDTO();
      itemMock.procedimentoId = 'proc-1';
      itemMock.valor = 150;
      itemMock.vigenciaInicio = '2026-01-01';

      const mockTabela = new TabelaDTO();
      mockTabela.nome = 'Tabela';
      mockTabela.itens = [itemMock];

      tabelaService.findById.mockReturnValue(
        of({ body: mockTabela } as Response<TabelaDTO>)
      );

      component.detailId = 'tab-3';
      component.ngOnInit();

      expect(component.itens).toHaveLength(1);
      expect(component.itens[0].valor).toBe(150);
    });
  });

  describe('Validações ao Salvar', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve exibir erro e não salvar quando form é inválido', () => {
      component.form.patchValue({ nome: '', tipo: null });

      component.save();

      expect(messageService.erro).toHaveBeenCalled();
      expect(tabelaService.save).not.toHaveBeenCalled();
    });

    it('deve marcar todos os campos como tocados quando form é inválido', () => {
      component.form.patchValue({ nome: '' });
      const markAllAsTouchedSpy = jest.spyOn(component.form, 'markAllAsTouched');

      component.save();

      expect(markAllAsTouchedSpy).toHaveBeenCalled();
    });

    it('deve isControlInvalid retornar false quando controle não foi tocado', () => {
      expect(component.isControlInvalid('nome')).toBe(false);
    });

    it('deve isControlInvalid retornar true quando controle é inválido e tocado', () => {
      const control = component.form.get('nome');
      control?.markAsTouched();
      control?.setValue('');

      expect(component.isControlInvalid('nome')).toBe(true);
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve salvar tabela com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        nome: 'Tabela Particular',
        tipo: TipoTabela.PARTICULAR,
        ativo: true,
      });

      tabelaService.save.mockImplementation(
        (_data: TabelaDTO, callbacks: ExecutionCallbacks<TabelaDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'tab-new' } as TabelaDTO);
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(tabelaService.save).toHaveBeenCalled();
      const callArgs = tabelaService.save.mock.calls[0][0];
      expect(callArgs.nome).toBe('Tabela Particular');
      expect(callArgs.tipo).toBe(TipoTabela.PARTICULAR);
      expect(callArgs.ativo).toBe(true);
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('deve incluir itens ao salvar', () => {
      const item = new TabelaItemDTO();
      item.procedimentoId = 'proc-1';
      item.valor = 200;
      item.vigenciaInicio = '2026-01-01';
      component.itens = [item];

      component.form.patchValue({ nome: 'Tabela', tipo: TipoTabela.CONVENIO, ativo: true });

      tabelaService.save.mockImplementation(
        (_data: TabelaDTO, callbacks: ExecutionCallbacks<TabelaDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'tab-itens' } as TabelaDTO);
        }
      );

      component.save();

      const callArgs = tabelaService.save.mock.calls[0][0];
      expect(callArgs.itens).toHaveLength(1);
      expect(callArgs.itens![0].valor).toBe(200);
    });
  });

  describe('Gestão de Itens', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve exibir erro ao adicionar item sem procedimento selecionado', () => {
      component.procedimentoSelecionado = null;
      component.valorInputTemp = 100;
      component.vigenciaInicioTemp = new Date('2026-01-01');

      component.adicionarItem();

      expect(messageService.erro).toHaveBeenCalled();
      expect(component.itens).toHaveLength(0);
    });

    it('deve exibir erro ao adicionar item sem valor', () => {
      component.procedimentoSelecionado = { id: 'proc-1', codigo: 'P001' } as ProcedimentoDTO;
      component.valorInputTemp = null;
      component.vigenciaInicioTemp = new Date('2026-01-01');

      component.adicionarItem();

      expect(messageService.erro).toHaveBeenCalled();
      expect(component.itens).toHaveLength(0);
    });

    it('deve exibir erro ao adicionar item sem vigência inicial', () => {
      component.procedimentoSelecionado = { id: 'proc-1', codigo: 'P001' } as ProcedimentoDTO;
      component.valorInputTemp = 150;
      component.vigenciaInicioTemp = null;

      component.adicionarItem();

      expect(messageService.erro).toHaveBeenCalled();
      expect(component.itens).toHaveLength(0);
    });

    it('deve adicionar item com sucesso', () => {
      component.procedimentoSelecionado = {
        id: 'proc-1',
        codigo: 'PROC-001',
        descricao: 'Terapia ABA',
      } as ProcedimentoDTO;
      component.valorInputTemp = 150;
      component.vigenciaInicioTemp = new Date('2026-01-01');

      component.adicionarItem();

      expect(component.itens).toHaveLength(1);
      expect(component.itens[0].valor).toBe(150);
      expect(component.itens[0].procedimentoId).toBe('proc-1');
    });

    it('deve exibir erro ao tentar adicionar procedimento sem vigência final duplicado', () => {
      const itemExistente = new TabelaItemDTO();
      itemExistente.procedimentoId = 'proc-1';
      itemExistente.vigor = undefined;
      itemExistente.vigenciaFim = undefined;
      component.itens = [itemExistente];

      component.procedimentoSelecionado = { id: 'proc-1', codigo: 'PROC-001' } as ProcedimentoDTO;
      component.valorInputTemp = 200;
      component.vigenciaInicioTemp = new Date('2026-02-01');

      component.adicionarItem();

      expect(messageService.erro).toHaveBeenCalled();
      expect(component.itens).toHaveLength(1);
    });

    it('deve remover item pelo índice', () => {
      const i1 = new TabelaItemDTO();
      i1.procedimentoId = 'proc-1';
      const i2 = new TabelaItemDTO();
      i2.procedimentoId = 'proc-2';
      component.itens = [i1, i2];

      component.removerItem(0);

      expect(component.itens).toHaveLength(1);
      expect(component.itens[0].procedimentoId).toBe('proc-2');
    });

    it('deve limpar campos temporários após adicionar item', () => {
      component.procedimentoSelecionado = {
        id: 'proc-2',
        codigo: 'PROC-002',
        descricao: 'Fisioterapia',
      } as ProcedimentoDTO;
      component.valorInputTemp = 120;
      component.vigenciaInicioTemp = new Date('2026-01-01');

      component.adicionarItem();

      expect(component.procedimentoSelecionado).toBeNull();
      expect(component.valorInputTemp).toBeNull();
      expect(component.vigenciaInicioTemp).toBeNull();
    });

    it('deve formatar valor em moeda BRL', () => {
      const resultado = component.formatarValor(150);
      expect(resultado).toContain('150');
    });

    it('deve retornar string vazia para valor undefined', () => {
      expect(component.formatarValor(undefined)).toBe('');
    });
  });

  describe('Ações da Toolbar', () => {
    it('deve fechar detalhe ao chamar goBackFn', () => {
      component.detailId = 'add';
      component.ngOnInit();

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.goBackFn();

      expect(closeDetailSpy).toHaveBeenCalled();
    });
  });

  describe('Mensagens de Erro do Backend', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('NÃO deve fechar detalhe quando há erro do backend', () => {
      component.form.patchValue({
        nome: 'Tabela Erro',
        tipo: TipoTabela.PARTICULAR,
        ativo: true,
      });

      const mockError = {
        status: 400,
        error: { messages: ['Erro ao salvar tabela'] },
      };

      tabelaService.save.mockImplementation(
        (_data: TabelaDTO, callbacks: ExecutionCallbacks<TabelaDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(tabelaService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });
  });
});
