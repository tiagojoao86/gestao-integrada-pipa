import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { CategoriaTituloDetalheComponent } from './titulo-categoria-detalhe.component';
import { TituloCategoriaService } from '../titulo-categoria.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { of } from 'rxjs';
import { TituloCategoriaDTO } from '../model/titulo-categoria.dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { TituloCategoriaTipoEnum } from '../model/titulo-categoria-tipo.enum';
import { Response, ResponseListNoPagination } from '../../../base/model/response';
import { TituloCategoriaGridDTO } from '../model/titulo-categoria-grid.dto';

describe('CategoriaTituloDetalheComponent', () => {
  let component: CategoriaTituloDetalheComponent;
  let fixture: ComponentFixture<CategoriaTituloDetalheComponent>;
  let tituloCategoriaService: jest.Mocked<TituloCategoriaService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;

  beforeEach(async () => {
    const tituloCategoriaServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
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
      imports: [CategoriaTituloDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    })
      .overrideComponent(CategoriaTituloDetalheComponent, {
        set: {
          providers: [
            { provide: TituloCategoriaService, useValue: tituloCategoriaServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(CategoriaTituloDetalheComponent);
    component = fixture.componentInstance;
    tituloCategoriaService = fixture.debugElement.injector.get(
      TituloCategoriaService
    ) as jest.Mocked<TituloCategoriaService>;
    messageService = TestBed.inject(
      MessageService
    ) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    // Mock padrão para listAll de agrupadores
    tituloCategoriaServiceMock.listAll.mockReturnValue(
      of({ body: [] })
    );

    // Mock padrão para findById
    tituloCategoriaServiceMock.findById.mockReturnValue(
      of({ body: new TituloCategoriaDTO('', '', TituloCategoriaTipoEnum.DESPESA) } as Response<TituloCategoriaDTO>)
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
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.form.get('codigo')).toBeTruthy();
      expect(component.form.get('nome')).toBeTruthy();
      expect(component.form.get('descricao')).toBeTruthy();
      expect(component.form.get('tipo')).toBeTruthy();
      expect(component.form.get('agrupadorId')).toBeTruthy();
    });

    it('deve inicializar form com tipo DESPESA como padrão', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.form.get('tipo')?.value).toBe(TituloCategoriaTipoEnum.DESPESA);
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

    it('deve inicializar tipos de categoria com RECEITA e DESPESA', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.tiposCategoria.length).toBe(2);
      expect(component.tiposCategoria).toContain(TituloCategoriaTipoEnum.RECEITA);
      expect(component.tiposCategoria).toContain(TituloCategoriaTipoEnum.DESPESA);
    });

    it('deve carregar agrupadores ao inicializar', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(tituloCategoriaService.listAll).toHaveBeenCalled();
    });
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar agrupadores e preencher lista de opções', () => {
      const mockAgrupadores: TituloCategoriaDTO[] = [
        new TituloCategoriaDTO('AG-1', 'Agrupador 1', TituloCategoriaTipoEnum.DESPESA, undefined, undefined, undefined, 'ag-1'),
        new TituloCategoriaDTO('AG-2', 'Agrupador 2', TituloCategoriaTipoEnum.RECEITA, undefined, undefined, undefined, 'ag-2'),
      ];

      tituloCategoriaService.listAll.mockReturnValue(
        of({ body: mockAgrupadores } as unknown as ResponseListNoPagination<TituloCategoriaGridDTO>)
      );

      component.detailId = 'add';
      component.ngOnInit();

      expect(tituloCategoriaService.listAll).toHaveBeenCalled();
      expect(component.agrupadores.length).toBe(2);
      expect(component.agrupadores[0].nome).toBe('Agrupador 1');
    });

    it('deve carregar dados da categoria ao editar', () => {
      const mockCategoria: TituloCategoriaDTO = new TituloCategoriaDTO(
        'CAT-001',
        'Categoria Teste',
        TituloCategoriaTipoEnum.DESPESA,
        'Descrição teste',
        'ag-1',
        'Agrupador 1',
        'cat-1'
      );

      tituloCategoriaService.findById.mockReturnValue(
        of({ body: mockCategoria } as Response<TituloCategoriaDTO>)
      );

      component.detailId = 'cat-1';
      component.ngOnInit();

      expect(tituloCategoriaService.findById).toHaveBeenCalledWith('cat-1');
      expect(component.categoria.codigo).toBe('CAT-001');
      expect(component.categoria.nome).toBe('Categoria Teste');
      expect(component.editMode).toBe(true);
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(tituloCategoriaService.findById).not.toHaveBeenCalled();
      expect(component.editMode).toBe(false);
    });

    it('deve preencher form com dados carregados usando fillForm', () => {
      const mockCategoria: TituloCategoriaDTO = new TituloCategoriaDTO(
        'CAT-002',
        'Categoria Teste 2',
        TituloCategoriaTipoEnum.RECEITA,
        'Descrição teste 2',
        'ag-2',
        'Agrupador 2',
        'cat-2'
      );

      tituloCategoriaService.findById.mockReturnValue(
        of({ body: mockCategoria } as Response<TituloCategoriaDTO>)
      );

      component.detailId = 'cat-2';
      component.ngOnInit();

      expect(component.form.get('codigo')?.value).toBe('CAT-002');
      expect(component.form.get('nome')?.value).toBe('Categoria Teste 2');
      expect(component.form.get('descricao')?.value).toBe('Descrição teste 2');
      expect(component.form.get('tipo')?.value).toBe(TituloCategoriaTipoEnum.RECEITA);
      expect(component.form.get('agrupadorId')?.value).toBe('ag-2');
    });
  });

  describe('Validações ao Salvar', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve permitir salvar com todos os campos preenchidos', () => {
      component.form.patchValue({
        codigo: 'CAT-001',
        nome: 'Categoria Teste',
        tipo: TituloCategoriaTipoEnum.DESPESA,
      });

      tituloCategoriaService.save.mockImplementation(
        (_data: TituloCategoriaDTO, callbacks: ExecutionCallbacks<TituloCategoriaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cat-123' } as TituloCategoriaDTO);
          }
        }
      );

      component.salvar();

      expect(tituloCategoriaService.save).toHaveBeenCalled();
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve salvar categoria com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        codigo: 'CAT-001',
        nome: 'Categoria Nova',
        descricao: 'Descrição',
        tipo: TituloCategoriaTipoEnum.DESPESA,
        agrupadorId: 'ag-1',
      });

      tituloCategoriaService.save.mockImplementation(
        (_data: TituloCategoriaDTO, callbacks: ExecutionCallbacks<TituloCategoriaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess(
              new TituloCategoriaDTO('CAT-001', 'Categoria Nova', TituloCategoriaTipoEnum.DESPESA, 'Descrição', 'ag-1', undefined, 'cat-123')
            );
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.salvar();

      expect(tituloCategoriaService.save).toHaveBeenCalled();
      const callArgs = tituloCategoriaService.save.mock.calls[0][0];
      expect(callArgs.codigo).toBe('CAT-001');
      expect(callArgs.nome).toBe('Categoria Nova');
      expect(callArgs.descricao).toBe('Descrição');
      expect(callArgs.tipo).toBe(TituloCategoriaTipoEnum.DESPESA);
      expect(callArgs.agrupadorId).toBe('ag-1');
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('deve incluir id ao salvar quando está editando', () => {
      component.detailId = 'cat-456';
      component.categoria.id = 'cat-456';
      component.form.patchValue({
        codigo: 'CAT-002',
        nome: 'Categoria Editada',
        tipo: TituloCategoriaTipoEnum.RECEITA,
      });

      tituloCategoriaService.save.mockImplementation(
        (_data: TituloCategoriaDTO, callbacks: ExecutionCallbacks<TituloCategoriaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cat-456' } as TituloCategoriaDTO);
          }
        }
      );

      component.salvar();

      const callArgs = tituloCategoriaService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('cat-456');
      expect(callArgs.codigo).toBe('CAT-002');
      expect(callArgs.nome).toBe('Categoria Editada');
    });

    it('NÃO deve incluir id ao salvar quando é novo (add)', () => {
      component.detailId = 'add';
      component.categoria = new TituloCategoriaDTO('', '', TituloCategoriaTipoEnum.DESPESA);
      component.form.patchValue({
        codigo: 'CAT-NEW',
        nome: 'Nova Categoria',
        tipo: TituloCategoriaTipoEnum.DESPESA,
      });

      tituloCategoriaService.save.mockImplementation(
        (_data: TituloCategoriaDTO, callbacks: ExecutionCallbacks<TituloCategoriaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cat-new' } as TituloCategoriaDTO);
          }
        }
      );

      component.salvar();

      const callArgs = tituloCategoriaService.save.mock.calls[0][0];
      expect(callArgs.id).toBeUndefined();
      expect(callArgs.codigo).toBe('CAT-NEW');
      expect(callArgs.nome).toBe('Nova Categoria');
    });

    it('deve salvar categoria sem descricao (campo opcional)', () => {
      component.form.patchValue({
        codigo: 'CAT-003',
        nome: 'Categoria Sem Descricao',
        descricao: '',
        tipo: TituloCategoriaTipoEnum.RECEITA,
      });

      tituloCategoriaService.save.mockImplementation(
        (_data: TituloCategoriaDTO, callbacks: ExecutionCallbacks<TituloCategoriaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cat-789' } as TituloCategoriaDTO);
          }
        }
      );

      component.salvar();

      expect(tituloCategoriaService.save).toHaveBeenCalled();
      const callArgs = tituloCategoriaService.save.mock.calls[0][0];
      expect(callArgs.codigo).toBe('CAT-003');
      expect(callArgs.nome).toBe('Categoria Sem Descricao');
      expect(callArgs.descricao).toBe('');
    });

    it('deve salvar categoria sem agrupadorId (campo opcional)', () => {
      component.form.patchValue({
        codigo: 'CAT-004',
        nome: 'Categoria Sem Agrupador',
        tipo: TituloCategoriaTipoEnum.DESPESA,
        agrupadorId: '',
      });

      tituloCategoriaService.save.mockImplementation(
        (_data: TituloCategoriaDTO, callbacks: ExecutionCallbacks<TituloCategoriaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cat-999' } as TituloCategoriaDTO);
          }
        }
      );

      component.salvar();

      expect(tituloCategoriaService.save).toHaveBeenCalled();
      const callArgs = tituloCategoriaService.save.mock.calls[0][0];
      expect(callArgs.codigo).toBe('CAT-004');
      expect(callArgs.agrupadorId).toBe('');
    });

    it('deve salvar categoria do tipo RECEITA', () => {
      component.form.patchValue({
        codigo: 'REC-001',
        nome: 'Categoria Receita',
        tipo: TituloCategoriaTipoEnum.RECEITA,
      });

      tituloCategoriaService.save.mockImplementation(
        (_data: TituloCategoriaDTO, callbacks: ExecutionCallbacks<TituloCategoriaDTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'cat-rec' } as TituloCategoriaDTO);
          }
        }
      );

      component.salvar();

      const callArgs = tituloCategoriaService.save.mock.calls[0][0];
      expect(callArgs.tipo).toBe(TituloCategoriaTipoEnum.RECEITA);
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
      component.form.get('nome')?.setValue('Categoria Válida');

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
        codigo: 'CAT-DUP',
        nome: 'Categoria Duplicada',
        tipo: TituloCategoriaTipoEnum.DESPESA,
      });

      const mockError = {
        status: 400,
        error: { message: 'titulo-categoria.codigo.unique' },
      };

      tituloCategoriaService.save.mockImplementation(
        (_data: TituloCategoriaDTO, callbacks: ExecutionCallbacks<TituloCategoriaDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.salvar();

      expect(tituloCategoriaService.save).toHaveBeenCalled();
      // BaseService deve processar o erro e exibir mensagem via MessageService
    });

    it('NÃO deve fechar detalhe quando há erro do backend', () => {
      component.form.patchValue({
        codigo: 'CAT-ERR',
        nome: 'Categoria Erro',
        tipo: TituloCategoriaTipoEnum.DESPESA,
      });

      const mockError = {
        status: 500,
        error: { message: 'Erro interno do servidor' },
      };

      tituloCategoriaService.save.mockImplementation(
        (_data: TituloCategoriaDTO, callbacks: ExecutionCallbacks<TituloCategoriaDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.salvar();

      expect(tituloCategoriaService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });

    it('deve permitir BaseService tratar erro de constraint unique', () => {
      component.form.patchValue({
        codigo: 'CAT-EXIST',
        nome: 'Categoria Existente',
        tipo: TituloCategoriaTipoEnum.RECEITA,
      });

      const mockError = {
        status: 409,
        error: {
          message: 'titulo-categoria.codigo.unique',
          constraintName: 'UK_TITULO_CATEGORIA_CODIGO'
        },
      };

      tituloCategoriaService.save.mockImplementation(
        (_data: TituloCategoriaDTO, callbacks: ExecutionCallbacks<TituloCategoriaDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.salvar();

      expect(tituloCategoriaService.save).toHaveBeenCalled();
      // BaseService deve processar constraint e exibir mensagem amigável
    });

    it('deve permitir BaseService tratar erro de foreign key', () => {
      component.form.patchValue({
        codigo: 'CAT-FK',
        nome: 'Categoria FK',
        tipo: TituloCategoriaTipoEnum.DESPESA,
        agrupadorId: 'ag-inexistente',
      });

      const mockError = {
        status: 400,
        error: {
          message: 'titulo-categoria.agrupador.foreignKey',
          constraintName: 'FK_TITULO_CATEGORIA_AGRUPADOR'
        },
      };

      tituloCategoriaService.save.mockImplementation(
        (_data: TituloCategoriaDTO, callbacks: ExecutionCallbacks<TituloCategoriaDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      component.salvar();

      expect(tituloCategoriaService.save).toHaveBeenCalled();
      // BaseService deve processar FK e exibir mensagem amigável
    });
  });
});
