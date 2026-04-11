import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ConvenioDetalheComponent } from './convenio-detalhe.component';
import { ConvenioService } from '../convenio.service';
import { PessoaService } from '../../../cadastro/pessoa/pessoa.service';
import { ProcedimentoService } from '../../procedimento/procedimento.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { EntitySearchService } from '../../../base/entity-search/entity-search.service';
import { of } from 'rxjs';
import { ConvenioDTO } from '../model/convenio-dto';
import { CodigoConvenioDTO } from '../model/codigo-convenio-dto';
import { ProcedimentoDTO } from '../../procedimento/model/procedimento-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { Response } from '../../../base/model/response';

describe('ConvenioDetalheComponent', () => {
  let component: ConvenioDetalheComponent;
  let fixture: ComponentFixture<ConvenioDetalheComponent>;
  let convenioService: jest.Mocked<ConvenioService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;
  let _entitySearchService: jest.Mocked<EntitySearchService>;

  beforeEach(async () => {
    const convenioServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
    };

    const pessoaServiceMock = {
      findById: jest.fn(),
      list: jest.fn(),
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
      imports: [ConvenioDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: EntitySearchService, useValue: entitySearchServiceMock },
      ],
    })
      .overrideComponent(ConvenioDetalheComponent, {
        set: {
          providers: [
            { provide: ConvenioService, useValue: convenioServiceMock },
            { provide: PessoaService, useValue: pessoaServiceMock },
            { provide: ProcedimentoService, useValue: procedimentoServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ConvenioDetalheComponent);
    component = fixture.componentInstance;
    convenioService = fixture.debugElement.injector.get(
      ConvenioService
    ) as jest.Mocked<ConvenioService>;
    messageService = TestBed.inject(MessageService) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;
    _entitySearchService = TestBed.inject(EntitySearchService) as jest.Mocked<EntitySearchService>;

    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
    convenioServiceMock.findById.mockReturnValue(
      of({ body: new ConvenioDTO() } as Response<ConvenioDTO>)
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
      expect(component.form.get('pessoaId')).toBeTruthy();
      expect(component.form.get('registroAns')).toBeTruthy();
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
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados do convênio ao editar', () => {
      const mockConvenio = new ConvenioDTO();
      mockConvenio.id = 'conv-1';
      mockConvenio.nome = 'Unimed';
      mockConvenio.pessoaId = 'pessoa-1';
      mockConvenio.pessoaNome = 'Unimed Ltda';
      mockConvenio.registroAns = '123456';
      mockConvenio.ativo = true;
      mockConvenio.codigos = [];

      convenioService.findById.mockReturnValue(
        of({ body: mockConvenio } as Response<ConvenioDTO>)
      );

      component.detailId = 'conv-1';
      component.ngOnInit();

      expect(convenioService.findById).toHaveBeenCalledWith('conv-1');
      expect(component.convenio.nome).toBe('Unimed');
      expect(component.editMode).toBe(true);
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(convenioService.findById).not.toHaveBeenCalled();
      expect(component.editMode).toBe(false);
    });

    it('deve preencher form com dados carregados', () => {
      const mockConvenio = new ConvenioDTO();
      mockConvenio.nome = 'Bradesco Saúde';
      mockConvenio.pessoaId = 'pessoa-2';
      mockConvenio.pessoaNome = 'Bradesco Ltda';
      mockConvenio.registroAns = '654321';
      mockConvenio.ativo = true;
      mockConvenio.codigos = [];

      convenioService.findById.mockReturnValue(
        of({ body: mockConvenio } as Response<ConvenioDTO>)
      );

      component.detailId = 'conv-2';
      component.ngOnInit();

      expect(component.form.get('nome')?.value).toBe('Bradesco Saúde');
      expect(component.form.get('registroAns')?.value).toBe('654321');
      expect(component.form.get('ativo')?.value).toBe(true);
    });

    it('deve carregar codigos do convênio ao editar', () => {
      const codigoMock = new CodigoConvenioDTO();
      codigoMock.procedimentoId = 'proc-1';
      codigoMock.codigo = 'TRP-001';

      const mockConvenio = new ConvenioDTO();
      mockConvenio.nome = 'Amil';
      mockConvenio.codigos = [codigoMock];

      convenioService.findById.mockReturnValue(
        of({ body: mockConvenio } as Response<ConvenioDTO>)
      );

      component.detailId = 'conv-3';
      component.ngOnInit();

      expect(component.codigos).toHaveLength(1);
      expect(component.codigos[0].codigo).toBe('TRP-001');
    });

    it('deve definir pessoaSelecionada ao carregar convênio com pessoa', () => {
      const mockConvenio = new ConvenioDTO();
      mockConvenio.pessoaId = 'pessoa-3';
      mockConvenio.pessoaNome = 'Empresa Saúde SA';
      mockConvenio.codigos = [];

      convenioService.findById.mockReturnValue(
        of({ body: mockConvenio } as Response<ConvenioDTO>)
      );

      component.detailId = 'conv-4';
      component.ngOnInit();

      expect(component.pessoaSelecionada).not.toBeNull();
      expect(component.pessoaSelecionada?.id).toBe('pessoa-3');
      expect(component.pessoaSelecionada?.nome).toBe('Empresa Saúde SA');
    });
  });

  describe('Validações ao Salvar', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve exibir erro e não salvar quando form é inválido', () => {
      component.form.patchValue({ nome: '', pessoaId: '' });

      component.save();

      expect(messageService.erro).toHaveBeenCalled();
      expect(convenioService.save).not.toHaveBeenCalled();
    });

    it('deve marcar todos os campos como tocados quando form é inválido', () => {
      component.form.patchValue({ nome: '', pessoaId: '' });
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

    it('deve isControlInvalid retornar false quando controle é válido', () => {
      component.form.get('nome')?.setValue('Unimed');
      component.form.get('nome')?.markAsTouched();

      expect(component.isControlInvalid('nome')).toBe(false);
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve salvar convênio com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        nome: 'Unimed',
        pessoaId: 'pessoa-1',
        registroAns: '123456',
        ativo: true,
      });

      convenioService.save.mockImplementation(
        (_data: ConvenioDTO, callbacks: ExecutionCallbacks<ConvenioDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'conv-new' } as ConvenioDTO);
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(convenioService.save).toHaveBeenCalled();
      const callArgs = convenioService.save.mock.calls[0][0];
      expect(callArgs.nome).toBe('Unimed');
      expect(callArgs.pessoaId).toBe('pessoa-1');
      expect(callArgs.registroAns).toBe('123456');
      expect(callArgs.ativo).toBe(true);
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('deve salvar registroAns como undefined quando vazio', () => {
      component.form.patchValue({
        nome: 'Convênio Teste',
        pessoaId: 'pessoa-1',
        registroAns: '',
        ativo: true,
      });

      convenioService.save.mockImplementation(
        (_data: ConvenioDTO, callbacks: ExecutionCallbacks<ConvenioDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'conv-ans' } as ConvenioDTO);
        }
      );

      component.save();

      const callArgs = convenioService.save.mock.calls[0][0];
      expect(callArgs.registroAns).toBeUndefined();
    });

    it('deve incluir lista de codigos ao salvar', () => {
      const codigo = new CodigoConvenioDTO();
      codigo.procedimentoId = 'proc-1';
      codigo.codigo = 'TRP-001';
      component.codigos = [codigo];

      component.form.patchValue({ nome: 'Amil', pessoaId: 'pessoa-2', ativo: true });

      convenioService.save.mockImplementation(
        (_data: ConvenioDTO, callbacks: ExecutionCallbacks<ConvenioDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'conv-cod' } as ConvenioDTO);
        }
      );

      component.save();

      const callArgs = convenioService.save.mock.calls[0][0];
      expect(callArgs.codigos).toHaveLength(1);
      expect(callArgs.codigos![0].codigo).toBe('TRP-001');
    });
  });

  describe('Gestão de Códigos', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve exibir erro ao adicionar código sem procedimento selecionado', () => {
      component.procedimentoSelecionado = null;
      component.codigoInputTemp = 'TRP-001';

      component.adicionarCodigo();

      expect(messageService.erro).toHaveBeenCalled();
      expect(component.codigos).toHaveLength(0);
    });

    it('deve exibir erro ao adicionar código sem código informado', () => {
      component.procedimentoSelecionado = { id: 'proc-1', codigo: 'P001' } as ProcedimentoDTO;
      component.codigoInputTemp = '';

      component.adicionarCodigo();

      expect(messageService.erro).toHaveBeenCalled();
      expect(component.codigos).toHaveLength(0);
    });

    it('deve adicionar código com sucesso', () => {
      component.procedimentoSelecionado = {
        id: 'proc-1',
        codigo: 'PROC-001',
        descricao: 'Terapia ABA',
      } as ProcedimentoDTO;
      component.codigoInputTemp = 'CON-ABA';

      component.adicionarCodigo();

      expect(component.codigos).toHaveLength(1);
      expect(component.codigos[0].codigo).toBe('CON-ABA');
      expect(component.codigos[0].procedimentoId).toBe('proc-1');
    });

    it('deve exibir erro ao tentar adicionar procedimento duplicado', () => {
      const codigoExistente = new CodigoConvenioDTO();
      codigoExistente.procedimentoId = 'proc-1';
      codigoExistente.codigo = 'CON-001';
      component.codigos = [codigoExistente];

      component.procedimentoSelecionado = { id: 'proc-1', codigo: 'PROC-001' } as ProcedimentoDTO;
      component.codigoInputTemp = 'CON-002';

      component.adicionarCodigo();

      expect(messageService.erro).toHaveBeenCalled();
      expect(component.codigos).toHaveLength(1);
    });

    it('deve remover código pelo índice', () => {
      const c1 = new CodigoConvenioDTO();
      c1.codigo = 'COD-1';
      const c2 = new CodigoConvenioDTO();
      c2.codigo = 'COD-2';
      component.codigos = [c1, c2];

      component.removerCodigo(0);

      expect(component.codigos).toHaveLength(1);
      expect(component.codigos[0].codigo).toBe('COD-2');
    });

    it('deve limpar procedimento selecionado e código temp após adicionar', () => {
      component.procedimentoSelecionado = {
        id: 'proc-2',
        codigo: 'PROC-002',
        descricao: 'Fisioterapia',
      } as ProcedimentoDTO;
      component.codigoInputTemp = 'FIS-001';

      component.adicionarCodigo();

      expect(component.procedimentoSelecionado).toBeNull();
      expect(component.codigoInputTemp).toBe('');
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
        nome: 'Unimed',
        pessoaId: 'pessoa-1',
        ativo: true,
      });

      const mockError = {
        status: 400,
        error: { messages: ['Erro ao salvar convênio'] },
      };

      convenioService.save.mockImplementation(
        (_data: ConvenioDTO, callbacks: ExecutionCallbacks<ConvenioDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(convenioService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });
  });
});
