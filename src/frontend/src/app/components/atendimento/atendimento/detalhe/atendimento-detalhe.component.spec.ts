import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AtendimentoDetalheComponent } from './atendimento-detalhe.component';
import { AtendimentoService } from '../atendimento.service';
import { PessoaService } from '../../../cadastro/pessoa/pessoa.service';
import { SetorService } from '../../../cadastro/setor/setor.service';
import { ProfissionalService } from '../../profissional/profissional.service';
import { ConvenioService } from '../../convenio/convenio.service';
import { ConvenioCategoriaService } from '../../convenio-categoria/convenio-categoria.service';
import { ProcedimentoService } from '../../procedimento/procedimento.service';
import { TabelaRegraService } from '../../tabelaregra/tabela-regra.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { EntitySearchService } from '../../../base/entity-search/entity-search.service';
import { of } from 'rxjs';
import { AtendimentoDTO } from '../model/atendimento-dto';
import { ProcedimentoDTO } from '../../procedimento/model/procedimento-dto';
import { SetorDTO } from '../../../cadastro/setor/model/setor-dto';
import { ConvenioDTO } from '../../convenio/model/convenio-dto';
import { ConvenioCategoriaGridDTO } from '../../convenio-categoria/model/convenio-categoria-grid-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { Response } from '../../../base/model/response';

describe('AtendimentoDetalheComponent', () => {
  let component: AtendimentoDetalheComponent;
  let fixture: ComponentFixture<AtendimentoDetalheComponent>;
  let atendimentoService: jest.Mocked<AtendimentoService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;
  let convenioCategoriaService: jest.Mocked<ConvenioCategoriaService>;

  const atendimentoServiceMock = {
    findById: jest.fn(),
    save: jest.fn(),
  };

  const pessoaServiceMock = {
    findById: jest.fn(),
    list: jest.fn(),
  };

  const setorServiceMock = {
    findById: jest.fn(),
    list: jest.fn(),
  };

  const profissionalServiceMock = {
    findById: jest.fn(),
    list: jest.fn(),
  };

  const convenioServiceMock = {
    findById: jest.fn(),
    list: jest.fn(),
  };

  const convenioCategoriaServiceMock = {
    findById: jest.fn(),
    list: jest.fn(),
    listarPorConvenio: jest.fn(),
  };

  const procedimentoServiceMock = {
    findById: jest.fn(),
    list: jest.fn(),
  };

  const tabelaRegraServiceMock = {
    resolverProcedimento: jest.fn(),
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

  beforeEach(async () => {
    jest.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [AtendimentoDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: EntitySearchService, useValue: entitySearchServiceMock },
      ],
    })
      .overrideComponent(AtendimentoDetalheComponent, {
        set: {
          providers: [
            { provide: AtendimentoService, useValue: atendimentoServiceMock },
            { provide: PessoaService, useValue: pessoaServiceMock },
            { provide: SetorService, useValue: setorServiceMock },
            { provide: ProfissionalService, useValue: profissionalServiceMock },
            { provide: ConvenioService, useValue: convenioServiceMock },
            { provide: ConvenioCategoriaService, useValue: convenioCategoriaServiceMock },
            { provide: ProcedimentoService, useValue: procedimentoServiceMock },
            { provide: TabelaRegraService, useValue: tabelaRegraServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(AtendimentoDetalheComponent);
    component = fixture.componentInstance;
    atendimentoService = fixture.debugElement.injector.get(
      AtendimentoService
    ) as jest.Mocked<AtendimentoService>;
    convenioCategoriaService = fixture.debugElement.injector.get(
      ConvenioCategoriaService
    ) as jest.Mocked<ConvenioCategoriaService>;
    messageService = TestBed.inject(MessageService) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
    atendimentoServiceMock.findById.mockReturnValue(
      of({ body: new AtendimentoDTO() } as Response<AtendimentoDTO>)
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

      expect(component.form.get('dataInicio')).toBeTruthy();
      expect(component.form.get('dataFim')).toBeTruthy();
      expect(component.form.get('setorId')).toBeTruthy();
      expect(component.form.get('pacienteId')).toBeTruthy();
      expect(component.form.get('responsavelId')).toBeTruthy();
      expect(component.form.get('convenioId')).toBeTruthy();
      expect(component.form.get('convenioCategoriaId')).toBeTruthy();
      expect(component.form.get('profissionalAtendimentoId')).toBeTruthy();
      expect(component.form.get('profissionalResponsavelId')).toBeTruthy();
      expect(component.form.get('observacoes')).toBeTruthy();
    });

    it('deve inicializar convenioCategoriaId desabilitado', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.form.get('convenioCategoriaId')?.disabled).toBe(true);
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
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados do atendimento ao editar', () => {
      const mockAtendimento = new AtendimentoDTO();
      mockAtendimento.id = 'atend-1';
      mockAtendimento.dataInicio = '2026-04-11T09:00:00';
      mockAtendimento.dataFim = '2026-04-11T23:59:59';
      mockAtendimento.setorId = 'setor-1';
      mockAtendimento.setorNome = 'Clínica';
      mockAtendimento.pacienteId = 'pessoa-1';
      mockAtendimento.pacienteNome = 'Maria';
      mockAtendimento.procedimentos = [];

      atendimentoServiceMock.findById.mockReturnValue(
        of({ body: mockAtendimento } as Response<AtendimentoDTO>)
      );

      component.detailId = 'atend-1';
      component.ngOnInit();

      expect(atendimentoService.findById).toHaveBeenCalledWith('atend-1');
      expect(component.atendimento.pacienteNome).toBe('Maria');
      expect(component.editMode).toBe(true);
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(atendimentoService.findById).not.toHaveBeenCalled();
      expect(component.editMode).toBe(false);
    });

    it('deve restaurar seleções ao editar com convenio', () => {
      const mockAtendimento = new AtendimentoDTO();
      mockAtendimento.dataInicio = '2026-04-11T09:00:00';
      mockAtendimento.setorId = 'setor-1';
      mockAtendimento.setorNome = 'Clínica';
      mockAtendimento.convenioId = 'conv-1';
      mockAtendimento.convenioNome = 'Unimed';
      mockAtendimento.procedimentos = [];

      convenioCategoriaServiceMock.listarPorConvenio.mockReturnValue(of([]));

      atendimentoServiceMock.findById.mockReturnValue(
        of({ body: mockAtendimento } as Response<AtendimentoDTO>)
      );

      component.detailId = 'atend-2';
      component.ngOnInit();

      expect(component.convenioSelecionado?.id).toBe('conv-1');
      expect(component.convenioSelecionado?.nome).toBe('Unimed');
      expect(convenioCategoriaService.listarPorConvenio).toHaveBeenCalledWith('conv-1');
    });

    it('deve restaurar seleção do setor', () => {
      const mockAtendimento = new AtendimentoDTO();
      mockAtendimento.dataInicio = '2026-04-11T09:00:00';
      mockAtendimento.setorId = 'setor-1';
      mockAtendimento.setorNome = 'Fisioterapia';
      mockAtendimento.procedimentos = [];

      atendimentoServiceMock.findById.mockReturnValue(
        of({ body: mockAtendimento } as Response<AtendimentoDTO>)
      );

      component.detailId = 'atend-3';
      component.ngOnInit();

      expect(component.setorSelecionado?.id).toBe('setor-1');
      expect(component.setorSelecionado?.nome).toBe('Fisioterapia');
    });
  });

  describe('Validações ao Salvar', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve exibir erro e não salvar quando form é inválido', () => {
      component.form.patchValue({ setorId: '', pacienteId: '' });

      component.save();

      expect(messageService.erro).toHaveBeenCalled();
      expect(atendimentoService.save).not.toHaveBeenCalled();
    });

    it('deve exibir erro quando não há procedimentos e form válido', () => {
      component.form.patchValue({
        setorId: 'setor-1',
        pacienteId: 'pessoa-1',
        profissionalAtendimentoId: 'prof-1',
        profissionalResponsavelId: 'prof-2',
      });
      component.procedimentos = [];

      component.save();

      expect(messageService.erro).toHaveBeenCalled();
      expect(atendimentoService.save).not.toHaveBeenCalled();
    });

    it('deve isControlInvalid retornar false quando controle não foi tocado', () => {
      expect(component.isControlInvalid('setorId')).toBe(false);
    });

    it('deve isControlInvalid retornar true quando controle é inválido e tocado', () => {
      const control = component.form.get('setorId');
      control?.markAsTouched();
      control?.setValue('');

      expect(component.isControlInvalid('setorId')).toBe(true);
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve salvar atendimento com sucesso e fechar detalhe', () => {
      const dataInicio = new Date('2026-04-11T09:00:00');
      const dataFim = new Date('2026-04-11T23:59:59');

      component.form.patchValue({
        dataInicio,
        dataFim,
        setorId: 'setor-1',
        pacienteId: 'pessoa-1',
        profissionalAtendimentoId: 'prof-1',
        profissionalResponsavelId: 'prof-2',
      });
      component.procedimentos = [{
        procedimentoId: 'proc-1',
        procedimentoCodigo: 'P001',
        procedimentoDescricao: 'Terapia ABA',
        dataInicio,
        dataFim,
      }];

      atendimentoServiceMock.save.mockImplementation(
        (_data: AtendimentoDTO, callbacks: ExecutionCallbacks<AtendimentoDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'atend-new' } as AtendimentoDTO);
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(atendimentoService.save).toHaveBeenCalled();
      const callArgs = atendimentoService.save.mock.calls[0][0];
      expect(callArgs.setorId).toBe('setor-1');
      expect(callArgs.pacienteId).toBe('pessoa-1');
      expect(callArgs.procedimentos).toHaveLength(1);
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('deve mapear procedimentos ao salvar', () => {
      const dataInicio = new Date('2026-04-11T09:00:00');
      const dataFim = new Date('2026-04-11T23:59:59');

      component.form.patchValue({
        dataInicio,
        dataFim,
        setorId: 'setor-1',
        pacienteId: 'pessoa-1',
        profissionalAtendimentoId: 'prof-1',
        profissionalResponsavelId: 'prof-2',
      });
      component.procedimentos = [
        { procedimentoId: 'proc-1', procedimentoCodigo: 'P001', procedimentoDescricao: 'T1', dataInicio, dataFim },
        { procedimentoId: 'proc-2', procedimentoCodigo: 'P002', procedimentoDescricao: 'T2', dataInicio, dataFim },
      ];

      atendimentoServiceMock.save.mockImplementation(
        (_data: AtendimentoDTO, callbacks: ExecutionCallbacks<AtendimentoDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'atend-mapped' } as AtendimentoDTO);
        }
      );

      component.save();

      const callArgs = atendimentoService.save.mock.calls[0][0];
      expect(callArgs.procedimentos).toHaveLength(2);
      expect(callArgs.procedimentos![0].procedimentoId).toBe('proc-1');
      expect(callArgs.procedimentos![1].procedimentoId).toBe('proc-2');
    });
  });

  describe('Gestão de Procedimentos', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve adicionar procedimento com datas do form', () => {
      const dataInicio = new Date('2026-04-11T09:00:00');
      component.form.get('dataInicio')?.setValue(dataInicio);
      component.convenioSelecionado = { id: 'conv-1', nome: 'Convênio Teste' } as ConvenioDTO;
      tabelaRegraServiceMock.resolverProcedimento.mockReturnValue(
        of({ body: { tabelaItemId: null, valor: null } })
      );

      const proc: ProcedimentoDTO = { id: 'proc-1', codigo: 'P001', descricao: 'T1' } as ProcedimentoDTO;
      component.adicionarProcedimento(proc);

      expect(component.procedimentos).toHaveLength(1);
      expect(component.procedimentos[0].procedimentoId).toBe('proc-1');
    });

    it('deve remover procedimento pelo índice', () => {
      const dataInicio = new Date();
      component.procedimentos = [
        { procedimentoId: 'p1', dataInicio, dataFim: dataInicio },
        { procedimentoId: 'p2', dataInicio, dataFim: dataInicio },
      ];

      component.removerProcedimento(0);

      expect(component.procedimentos).toHaveLength(1);
      expect(component.procedimentos[0].procedimentoId).toBe('p2');
    });

    it('deve formatar valor em moeda BRL', () => {
      const resultado = component.formatarValor(150);
      expect(resultado).toContain('150');
    });

    it('deve retornar "—" para valor null/undefined', () => {
      expect(component.formatarValor(undefined)).toBe('—');
    });
  });

  describe('Limpar Seleções', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve limpar setor selecionado', () => {
      component.setorSelecionado = { id: 'setor-1', nome: 'Clínica' } as unknown as SetorDTO;
      component.form.get('setorId')?.setValue('setor-1');

      component.limparSetor();

      expect(component.setorSelecionado).toBeNull();
      expect(component.form.get('setorId')?.value).toBe('');
    });

    it('deve limpar convenio e desabilitar categorias', () => {
      component.convenioSelecionado = { id: 'conv-1', nome: 'Unimed' } as unknown as ConvenioDTO;
      component.form.get('convenioId')?.setValue('conv-1');
      component.form.get('convenioCategoriaId')?.enable();
      component.categoriasOptions = [{ id: 'cat-1', nome: 'Plano Básico' } as unknown as ConvenioCategoriaGridDTO];

      component.limparConvenio();

      expect(component.convenioSelecionado).toBeNull();
      expect(component.form.get('convenioId')?.value).toBe('');
      expect(component.form.get('convenioCategoriaId')?.disabled).toBe(true);
      expect(component.categoriasOptions).toHaveLength(0);
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
      const dataInicio = new Date('2026-04-11T09:00:00');
      const dataFim = new Date('2026-04-11T23:59:59');

      component.form.patchValue({
        dataInicio,
        dataFim,
        setorId: 'setor-1',
        pacienteId: 'pessoa-1',
        profissionalAtendimentoId: 'prof-1',
        profissionalResponsavelId: 'prof-2',
      });
      component.procedimentos = [{
        procedimentoId: 'proc-1',
        dataInicio,
        dataFim,
      }];

      const mockError = {
        status: 400,
        error: { messages: ['Erro ao salvar atendimento'] },
      };

      atendimentoServiceMock.save.mockImplementation(
        (_data: AtendimentoDTO, callbacks: ExecutionCallbacks<AtendimentoDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(atendimentoService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });
  });
});
