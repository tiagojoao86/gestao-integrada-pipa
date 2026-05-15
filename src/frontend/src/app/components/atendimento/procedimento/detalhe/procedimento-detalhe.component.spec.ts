import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ProcedimentoDetalheComponent } from './procedimento-detalhe.component';
import { ProcedimentoService } from '../procedimento.service';
import { TituloCategoriaService } from '../../../financeiro/titulo-categoria/titulo-categoria.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { of } from 'rxjs';
import { ProcedimentoDTO } from '../model/procedimento-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { Response } from '../../../base/model/response';

describe('ProcedimentoDetalheComponent', () => {
  let component: ProcedimentoDetalheComponent;
  let fixture: ComponentFixture<ProcedimentoDetalheComponent>;
  let procedimentoService: jest.Mocked<ProcedimentoService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;

  beforeEach(async () => {
    const procedimentoServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
    };

    const tituloCategoriaServiceMock = {
      listAll: jest.fn().mockReturnValue(of({ body: [] })),
    };

    const messageServiceMock = {
      sucesso: jest.fn(),
      erro: jest.fn(),
    };

    const authServiceMock = {
      hasAuthorityEditarToModulo: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [ProcedimentoDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    })
      .overrideComponent(ProcedimentoDetalheComponent, {
        set: {
          providers: [
            { provide: ProcedimentoService, useValue: procedimentoServiceMock },
            { provide: TituloCategoriaService, useValue: tituloCategoriaServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ProcedimentoDetalheComponent);
    component = fixture.componentInstance;
    procedimentoService = fixture.debugElement.injector.get(
      ProcedimentoService
    ) as jest.Mocked<ProcedimentoService>;
    messageService = TestBed.inject(MessageService) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
    procedimentoServiceMock.findById.mockReturnValue(
      of({ body: new ProcedimentoDTO() } as Response<ProcedimentoDTO>)
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

      expect(component.form.get('codigo')).toBeTruthy();
      expect(component.form.get('descricao')).toBeTruthy();
      expect(component.form.get('codigoTiss')).toBeTruthy();
      expect(component.form.get('codigoTuss')).toBeTruthy();
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
    it('deve carregar dados do procedimento ao editar', () => {
      const mockProcedimento = new ProcedimentoDTO();
      mockProcedimento.id = 'proc-1';
      mockProcedimento.codigo = 'PROC-001';
      mockProcedimento.descricao = 'Terapia ABA';
      mockProcedimento.codigoTiss = 'TISS-001';
      mockProcedimento.codigoTuss = 'TUSS-001';
      mockProcedimento.ativo = true;

      procedimentoService.findById.mockReturnValue(
        of({ body: mockProcedimento } as Response<ProcedimentoDTO>)
      );

      component.detailId = 'proc-1';
      component.ngOnInit();

      expect(procedimentoService.findById).toHaveBeenCalledWith('proc-1');
      expect(component.procedimento.codigo).toBe('PROC-001');
      expect(component.editMode).toBe(true);
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(procedimentoService.findById).not.toHaveBeenCalled();
      expect(component.editMode).toBe(false);
    });

    it('deve preencher form com dados carregados', () => {
      const mockProcedimento = new ProcedimentoDTO();
      mockProcedimento.codigo = 'PROC-002';
      mockProcedimento.descricao = 'Fonoaudiologia';
      mockProcedimento.codigoTiss = 'TISS-002';
      mockProcedimento.codigoTuss = 'TUSS-002';
      mockProcedimento.ativo = false;

      procedimentoService.findById.mockReturnValue(
        of({ body: mockProcedimento } as Response<ProcedimentoDTO>)
      );

      component.detailId = 'proc-2';
      component.ngOnInit();

      expect(component.form.get('codigo')?.value).toBe('PROC-002');
      expect(component.form.get('descricao')?.value).toBe('Fonoaudiologia');
      expect(component.form.get('codigoTiss')?.value).toBe('TISS-002');
      expect(component.form.get('codigoTuss')?.value).toBe('TUSS-002');
      expect(component.form.get('ativo')?.value).toBe(false);
    });
  });

  describe('Validações ao Salvar', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve exibir erro e não salvar quando form é inválido', () => {
      component.form.patchValue({ codigo: '', descricao: '' });

      component.save();

      expect(messageService.erro).toHaveBeenCalled();
      expect(procedimentoService.save).not.toHaveBeenCalled();
    });

    it('deve marcar todos os campos como tocados quando form é inválido', () => {
      component.form.patchValue({ codigo: '', descricao: '' });
      const markAllAsTouchedSpy = jest.spyOn(component.form, 'markAllAsTouched');

      component.save();

      expect(markAllAsTouchedSpy).toHaveBeenCalled();
    });

    it('deve isControlInvalid retornar false quando controle não foi tocado', () => {
      expect(component.isControlInvalid('codigo')).toBe(false);
    });

    it('deve isControlInvalid retornar true quando controle é inválido e tocado', () => {
      const control = component.form.get('codigo');
      control?.markAsTouched();
      control?.setValue('');

      expect(component.isControlInvalid('codigo')).toBe(true);
    });

    it('deve isControlInvalid retornar false quando controle é válido', () => {
      component.form.get('codigo')?.setValue('PROC-001');
      component.form.get('codigo')?.markAsTouched();

      expect(component.isControlInvalid('codigo')).toBe(false);
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve salvar procedimento com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        codigo: 'PROC-001',
        descricao: 'Terapia ABA',
        codigoTiss: 'TISS-001',
        codigoTuss: 'TUSS-001',
        ativo: true,
      });

      procedimentoService.save.mockImplementation(
        (_data: ProcedimentoDTO, callbacks: ExecutionCallbacks<ProcedimentoDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'proc-new' } as ProcedimentoDTO);
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(procedimentoService.save).toHaveBeenCalled();
      const callArgs = procedimentoService.save.mock.calls[0][0];
      expect(callArgs.codigo).toBe('PROC-001');
      expect(callArgs.descricao).toBe('Terapia ABA');
      expect(callArgs.codigoTiss).toBe('TISS-001');
      expect(callArgs.codigoTuss).toBe('TUSS-001');
      expect(callArgs.ativo).toBe(true);
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('deve salvar codigoTiss e codigoTuss como undefined quando vazios', () => {
      component.form.patchValue({
        codigo: 'PROC-003',
        descricao: 'Psicologia',
        codigoTiss: '',
        codigoTuss: '',
        ativo: true,
      });

      procedimentoService.save.mockImplementation(
        (_data: ProcedimentoDTO, callbacks: ExecutionCallbacks<ProcedimentoDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'proc-opt' } as ProcedimentoDTO);
        }
      );

      component.save();

      const callArgs = procedimentoService.save.mock.calls[0][0];
      expect(callArgs.codigoTiss).toBeUndefined();
      expect(callArgs.codigoTuss).toBeUndefined();
    });

    it('deve incluir id ao salvar quando está editando', () => {
      component.procedimento.id = 'proc-edit';
      component.form.patchValue({
        codigo: 'PROC-EDIT',
        descricao: 'Procedimento Editado',
        ativo: true,
      });

      procedimentoService.save.mockImplementation(
        (_data: ProcedimentoDTO, callbacks: ExecutionCallbacks<ProcedimentoDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'proc-edit' } as ProcedimentoDTO);
        }
      );

      component.save();

      const callArgs = procedimentoService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('proc-edit');
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
        codigo: 'PROC-ERR',
        descricao: 'Procedimento Erro',
        ativo: true,
      });

      const mockError = {
        status: 400,
        error: { messages: ['Código já cadastrado'] },
      };

      procedimentoService.save.mockImplementation(
        (_data: ProcedimentoDTO, callbacks: ExecutionCallbacks<ProcedimentoDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(procedimentoService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });
  });
});
