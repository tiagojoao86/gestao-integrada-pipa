import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ConvenioCategoriaDetalheComponent } from './convenio-categoria-detalhe.component';
import { ConvenioCategoriaService } from '../convenio-categoria.service';
import { ConvenioService } from '../../convenio/convenio.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { EntitySearchService } from '../../../base/entity-search/entity-search.service';
import { of } from 'rxjs';
import { ConvenioCategoriaDTO } from '../model/convenio-categoria-dto';
import { ConvenioDTO } from '../../convenio/model/convenio-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { Response } from '../../../base/model/response';

describe('ConvenioCategoriaDetalheComponent', () => {
  let component: ConvenioCategoriaDetalheComponent;
  let fixture: ComponentFixture<ConvenioCategoriaDetalheComponent>;
  let convenioCategoriaService: jest.Mocked<ConvenioCategoriaService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;

  beforeEach(async () => {
    const convenioCategoriaServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
    };

    const convenioServiceMock = {
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
      imports: [ConvenioCategoriaDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: EntitySearchService, useValue: entitySearchServiceMock },
      ],
    })
      .overrideComponent(ConvenioCategoriaDetalheComponent, {
        set: {
          providers: [
            { provide: ConvenioCategoriaService, useValue: convenioCategoriaServiceMock },
            { provide: ConvenioService, useValue: convenioServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ConvenioCategoriaDetalheComponent);
    component = fixture.componentInstance;
    convenioCategoriaService = fixture.debugElement.injector.get(
      ConvenioCategoriaService
    ) as jest.Mocked<ConvenioCategoriaService>;
    messageService = TestBed.inject(MessageService) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
    convenioCategoriaServiceMock.findById.mockReturnValue(
      of({ body: new ConvenioCategoriaDTO() } as Response<ConvenioCategoriaDTO>)
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

      expect(component.form.get('convenioId')).toBeTruthy();
      expect(component.form.get('nome')).toBeTruthy();
      expect(component.form.get('codigoAnsPlano')).toBeTruthy();
      expect(component.form.get('ativo')).toBeTruthy();
    });

    it('deve inicializar form com ativo = true como padrão', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.form.get('ativo')?.value).toBe(true);
    });

    it('deve configurar toolbar com ações de cancelar e salvar quando tem permissão', () => {
      authService.hasAuthorityEditarToModulo.mockReturnValue(true);

      component.detailId = 'cat-1';
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
    it('deve carregar dados da categoria ao editar', () => {
      const mockCategoria = new ConvenioCategoriaDTO();
      mockCategoria.id = 'cat-1';
      mockCategoria.convenioId = 'conv-1';
      mockCategoria.convenioNome = 'Unimed';
      mockCategoria.nome = 'Plano Básico';
      mockCategoria.codigoAnsPlano = 'ANS-001';
      mockCategoria.ativo = true;

      convenioCategoriaService.findById.mockReturnValue(
        of({ body: mockCategoria } as Response<ConvenioCategoriaDTO>)
      );

      component.detailId = 'cat-1';
      component.ngOnInit();

      expect(convenioCategoriaService.findById).toHaveBeenCalledWith('cat-1');
      expect(component.convenioCategoria.nome).toBe('Plano Básico');
      expect(component.editMode).toBe(true);
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(convenioCategoriaService.findById).not.toHaveBeenCalled();
      expect(component.editMode).toBe(false);
    });

    it('deve preencher form com dados carregados', () => {
      const mockCategoria = new ConvenioCategoriaDTO();
      mockCategoria.convenioId = 'conv-2';
      mockCategoria.convenioNome = 'Bradesco';
      mockCategoria.nome = 'Plano Premium';
      mockCategoria.codigoAnsPlano = 'ANS-002';
      mockCategoria.ativo = false;

      convenioCategoriaService.findById.mockReturnValue(
        of({ body: mockCategoria } as Response<ConvenioCategoriaDTO>)
      );

      component.detailId = 'cat-2';
      component.ngOnInit();

      expect(component.form.get('nome')?.value).toBe('Plano Premium');
      expect(component.form.get('codigoAnsPlano')?.value).toBe('ANS-002');
      expect(component.form.get('ativo')?.value).toBe(false);
    });

    it('deve definir convenioSelecionado ao carregar categoria com convênio', () => {
      const mockCategoria = new ConvenioCategoriaDTO();
      mockCategoria.convenioId = 'conv-3';
      mockCategoria.convenioNome = 'Amil';
      mockCategoria.nome = 'Categoria Amil';

      convenioCategoriaService.findById.mockReturnValue(
        of({ body: mockCategoria } as Response<ConvenioCategoriaDTO>)
      );

      component.detailId = 'cat-3';
      component.ngOnInit();

      expect(component.convenioSelecionado).not.toBeNull();
      expect(component.convenioSelecionado?.id).toBe('conv-3');
      expect(component.convenioSelecionado?.nome).toBe('Amil');
    });
  });

  describe('Validações ao Salvar', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve exibir erro e não salvar quando form é inválido', () => {
      component.form.patchValue({ convenioId: '', nome: '' });

      component.save();

      expect(messageService.erro).toHaveBeenCalled();
      expect(convenioCategoriaService.save).not.toHaveBeenCalled();
    });

    it('deve marcar todos os campos como tocados quando form é inválido', () => {
      component.form.patchValue({ convenioId: '', nome: '' });
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

    it('deve salvar categoria com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        convenioId: 'conv-1',
        nome: 'Plano Básico',
        codigoAnsPlano: 'ANS-001',
        ativo: true,
      });

      convenioCategoriaService.save.mockImplementation(
        (_data: ConvenioCategoriaDTO, callbacks: ExecutionCallbacks<ConvenioCategoriaDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'cat-new' } as ConvenioCategoriaDTO);
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(convenioCategoriaService.save).toHaveBeenCalled();
      const callArgs = convenioCategoriaService.save.mock.calls[0][0];
      expect(callArgs.convenioId).toBe('conv-1');
      expect(callArgs.nome).toBe('Plano Básico');
      expect(callArgs.codigoAnsPlano).toBe('ANS-001');
      expect(callArgs.ativo).toBe(true);
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('deve salvar codigoAnsPlano como undefined quando vazio', () => {
      component.form.patchValue({
        convenioId: 'conv-1',
        nome: 'Plano Teste',
        codigoAnsPlano: '',
        ativo: true,
      });

      convenioCategoriaService.save.mockImplementation(
        (_data: ConvenioCategoriaDTO, callbacks: ExecutionCallbacks<ConvenioCategoriaDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'cat-ans' } as ConvenioCategoriaDTO);
        }
      );

      component.save();

      const callArgs = convenioCategoriaService.save.mock.calls[0][0];
      expect(callArgs.codigoAnsPlano).toBeUndefined();
    });

    it('deve incluir id ao salvar quando está editando', () => {
      component.convenioCategoria.id = 'cat-edit';
      component.form.patchValue({
        convenioId: 'conv-1',
        nome: 'Plano Editado',
        ativo: true,
      });

      convenioCategoriaService.save.mockImplementation(
        (_data: ConvenioCategoriaDTO, callbacks: ExecutionCallbacks<ConvenioCategoriaDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'cat-edit' } as ConvenioCategoriaDTO);
        }
      );

      component.save();

      const callArgs = convenioCategoriaService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('cat-edit');
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

  describe('Gestão de Convênio', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve limpar convenioSelecionado e convenioId ao chamar limparConvenio', () => {
      component.convenioSelecionado = { id: 'conv-x', nome: 'Convênio X' } as unknown as ConvenioDTO;
      component.form.get('convenioId')?.setValue('conv-x');

      component.limparConvenio();

      expect(component.convenioSelecionado).toBeNull();
      expect(component.form.get('convenioId')?.value).toBe('');
    });
  });

  describe('Mensagens de Erro do Backend', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('NÃO deve fechar detalhe quando há erro do backend', () => {
      component.form.patchValue({
        convenioId: 'conv-1',
        nome: 'Plano Erro',
        ativo: true,
      });

      const mockError = {
        status: 400,
        error: { messages: ['Erro ao salvar categoria'] },
      };

      convenioCategoriaService.save.mockImplementation(
        (_data: ConvenioCategoriaDTO, callbacks: ExecutionCallbacks<ConvenioCategoriaDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(convenioCategoriaService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });
  });
});
