import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ProfissionalDetalheComponent } from './profissional-detalhe.component';
import { ProfissionalService } from '../profissional.service';
import { PessoaService } from '../../../cadastro/pessoa/pessoa.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { EntitySearchService } from '../../../base/entity-search/entity-search.service';
import { of } from 'rxjs';
import { ProfissionalDTO } from '../model/profissional-dto';
import { TipoRemuneracao } from '../model/tipo-remuneracao.enum';
import { Conselho } from '../model/conselho.enum';
import { PessoaDTO } from '../../../cadastro/pessoa/model/pessoa-dto';
import { ExecutionCallbacks } from '../../../base/base-service';
import { Response } from '../../../base/model/response';

describe('ProfissionalDetalheComponent', () => {
  let component: ProfissionalDetalheComponent;
  let fixture: ComponentFixture<ProfissionalDetalheComponent>;
  let profissionalService: jest.Mocked<ProfissionalService>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;
  let _entitySearchService: jest.Mocked<EntitySearchService>;

  beforeEach(async () => {
    const profissionalServiceMock = {
      findById: jest.fn(),
      save: jest.fn(),
    };

    const pessoaServiceMock = {
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
      imports: [ProfissionalDetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: EntitySearchService, useValue: entitySearchServiceMock },
      ],
    })
      .overrideComponent(ProfissionalDetalheComponent, {
        set: {
          providers: [
            { provide: ProfissionalService, useValue: profissionalServiceMock },
            { provide: PessoaService, useValue: pessoaServiceMock },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ProfissionalDetalheComponent);
    component = fixture.componentInstance;
    profissionalService = fixture.debugElement.injector.get(
      ProfissionalService
    ) as jest.Mocked<ProfissionalService>;
    messageService = TestBed.inject(MessageService) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;
    _entitySearchService = TestBed.inject(EntitySearchService) as jest.Mocked<EntitySearchService>;

    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
    profissionalServiceMock.findById.mockReturnValue(
      of({ body: new ProfissionalDTO() } as Response<ProfissionalDTO>)
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

      expect(component.form.get('pessoaId')).toBeTruthy();
      expect(component.form.get('conselho')).toBeTruthy();
      expect(component.form.get('codigoConselho')).toBeTruthy();
      expect(component.form.get('tipoRemuneracao')).toBeTruthy();
      expect(component.form.get('banco')).toBeTruthy();
      expect(component.form.get('conta')).toBeTruthy();
      expect(component.form.get('chavePix')).toBeTruthy();
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

    it('deve inicializar lista de tipos de remuneração', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.tiposRemuneracao.length).toBeGreaterThan(0);
      expect(component.tiposRemuneracao).toContain(TipoRemuneracao.CLT);
      expect(component.tiposRemuneracao).toContain(TipoRemuneracao.PJ);
    });

    it('deve inicializar lista de conselhos', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.conselhos.length).toBeGreaterThan(0);
      expect(component.conselhos).toContain(Conselho.CRM);
      expect(component.conselhos).toContain(Conselho.CRP);
    });
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados do profissional ao editar', () => {
      const mockProfissional = new ProfissionalDTO();
      mockProfissional.id = 'prof-1';
      mockProfissional.pessoaId = 'pessoa-1';
      mockProfissional.pessoaNome = 'Dr. João';
      mockProfissional.conselho = 'CRM';
      mockProfissional.codigoConselho = 'CRM-12345';
      mockProfissional.tipoRemuneracao = TipoRemuneracao.CLT;
      mockProfissional.ativo = true;

      profissionalService.findById.mockReturnValue(
        of({ body: mockProfissional } as Response<ProfissionalDTO>)
      );

      component.detailId = 'prof-1';
      component.ngOnInit();

      expect(profissionalService.findById).toHaveBeenCalledWith('prof-1');
      expect(component.profissional.pessoaNome).toBe('Dr. João');
      expect(component.editMode).toBe(true);
    });

    it('NÃO deve carregar dados quando detailId é "add"', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(profissionalService.findById).not.toHaveBeenCalled();
      expect(component.editMode).toBe(false);
    });

    it('deve preencher form com dados carregados', () => {
      const mockProfissional = new ProfissionalDTO();
      mockProfissional.id = 'prof-2';
      mockProfissional.pessoaId = 'pessoa-2';
      mockProfissional.pessoaNome = 'Dra. Maria';
      mockProfissional.conselho = 'CRP';
      mockProfissional.codigoConselho = 'CRP-98765';
      mockProfissional.tipoRemuneracao = TipoRemuneracao.PJ;
      mockProfissional.banco = 'Banco do Brasil';
      mockProfissional.conta = '12345-6';
      mockProfissional.ativo = true;

      profissionalService.findById.mockReturnValue(
        of({ body: mockProfissional } as Response<ProfissionalDTO>)
      );

      component.detailId = 'prof-2';
      component.ngOnInit();

      expect(component.form.get('codigoConselho')?.value).toBe('CRP-98765');
      expect(component.form.get('banco')?.value).toBe('Banco do Brasil');
      expect(component.form.get('conta')?.value).toBe('12345-6');
      expect(component.form.get('ativo')?.value).toBe(true);
    });

    it('deve desabilitar campo pessoaId no modo edição', () => {
      const mockProfissional = new ProfissionalDTO();
      mockProfissional.id = 'prof-3';
      mockProfissional.pessoaId = 'pessoa-3';
      mockProfissional.pessoaNome = 'Dr. Carlos';

      profissionalService.findById.mockReturnValue(
        of({ body: mockProfissional } as Response<ProfissionalDTO>)
      );

      component.detailId = 'prof-3';
      component.ngOnInit();

      expect(component.form.get('pessoaId')?.disabled).toBe(true);
    });

    it('deve definir pessoaSelecionada ao carregar profissional com pessoa', () => {
      const mockProfissional = new ProfissionalDTO();
      mockProfissional.pessoaId = 'pessoa-4';
      mockProfissional.pessoaNome = 'Dr. Pedro';

      profissionalService.findById.mockReturnValue(
        of({ body: mockProfissional } as Response<ProfissionalDTO>)
      );

      component.detailId = 'prof-4';
      component.ngOnInit();

      expect(component.pessoaSelecionada).not.toBeNull();
      expect(component.pessoaSelecionada?.id).toBe('pessoa-4');
      expect(component.pessoaSelecionada?.nome).toBe('Dr. Pedro');
    });
  });

  describe('Validações ao Salvar', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve exibir erro e não salvar quando form é inválido', () => {
      component.form.patchValue({
        pessoaId: '',
        conselho: null,
        codigoConselho: '',
        tipoRemuneracao: null,
      });

      component.save();

      expect(messageService.erro).toHaveBeenCalled();
      expect(profissionalService.save).not.toHaveBeenCalled();
    });

    it('deve marcar todos os campos como tocados quando form é inválido', () => {
      component.form.patchValue({
        pessoaId: '',
        conselho: null,
        codigoConselho: '',
        tipoRemuneracao: null,
      });

      const markAllAsTouchedSpy = jest.spyOn(component.form, 'markAllAsTouched');
      component.save();

      expect(markAllAsTouchedSpy).toHaveBeenCalled();
    });

    it('deve chamar save quando form é válido', () => {
      component.form.patchValue({
        pessoaId: 'pessoa-1',
        conselho: Conselho.CRM,
        codigoConselho: 'CRM-12345',
        tipoRemuneracao: TipoRemuneracao.CLT,
        ativo: true,
      });

      profissionalService.save.mockImplementation(
        (_data: ProfissionalDTO, callbacks: ExecutionCallbacks<ProfissionalDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'prof-new' } as ProfissionalDTO);
        }
      );

      component.save();

      expect(profissionalService.save).toHaveBeenCalled();
    });

    it('deve isControlInvalid retornar false quando controle não foi tocado', () => {
      expect(component.isControlInvalid('pessoaId')).toBe(false);
    });

    it('deve isControlInvalid retornar true quando controle é inválido e tocado', () => {
      const control = component.form.get('pessoaId');
      control?.markAsTouched();
      control?.setValue('');

      expect(component.isControlInvalid('pessoaId')).toBe(true);
    });

    it('deve isControlInvalid retornar false quando controle é válido', () => {
      component.form.get('pessoaId')?.setValue('pessoa-1');
      component.form.get('pessoaId')?.markAsTouched();

      expect(component.isControlInvalid('pessoaId')).toBe(false);
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve salvar profissional com sucesso e fechar detalhe', () => {
      component.form.patchValue({
        pessoaId: 'pessoa-1',
        conselho: Conselho.CRM,
        codigoConselho: 'CRM-12345',
        tipoRemuneracao: TipoRemuneracao.CLT,
        ativo: true,
      });

      profissionalService.save.mockImplementation(
        (_data: ProfissionalDTO, callbacks: ExecutionCallbacks<ProfissionalDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'prof-new' } as ProfissionalDTO);
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(profissionalService.save).toHaveBeenCalled();
      const callArgs = profissionalService.save.mock.calls[0][0];
      expect(callArgs.pessoaId).toBe('pessoa-1');
      expect(callArgs.conselho).toBe('CRM');
      expect(callArgs.codigoConselho).toBe('CRM-12345');
      expect(callArgs.ativo).toBe(true);
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(closeDetailSpy).toHaveBeenCalled();
    });

    it('deve serializar enum conselho para string ao salvar', () => {
      component.form.patchValue({
        pessoaId: 'pessoa-1',
        conselho: Conselho.CRP,
        codigoConselho: 'CRP-00001',
        tipoRemuneracao: TipoRemuneracao.PJ,
        ativo: true,
      });

      profissionalService.save.mockImplementation(
        (_data: ProfissionalDTO, callbacks: ExecutionCallbacks<ProfissionalDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'prof-crp' } as ProfissionalDTO);
        }
      );

      component.save();

      const callArgs = profissionalService.save.mock.calls[0][0];
      expect(callArgs.conselho).toBe('CRP');
    });

    it('deve incluir id ao salvar quando está editando', () => {
      component.profissional.id = 'prof-edit';
      component.form.patchValue({
        pessoaId: 'pessoa-1',
        conselho: Conselho.CRM,
        codigoConselho: 'CRM-12345',
        tipoRemuneracao: TipoRemuneracao.CLT,
        ativo: true,
      });

      profissionalService.save.mockImplementation(
        (_data: ProfissionalDTO, callbacks: ExecutionCallbacks<ProfissionalDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'prof-edit' } as ProfissionalDTO);
        }
      );

      component.save();

      const callArgs = profissionalService.save.mock.calls[0][0];
      expect(callArgs.id).toBe('prof-edit');
    });

    it('deve salvar campos opcionais como undefined quando vazios', () => {
      component.form.patchValue({
        pessoaId: 'pessoa-1',
        conselho: Conselho.CRM,
        codigoConselho: 'CRM-99999',
        tipoRemuneracao: TipoRemuneracao.HORA,
        banco: '',
        conta: '',
        chavePix: '',
        ativo: false,
      });

      profissionalService.save.mockImplementation(
        (_data: ProfissionalDTO, callbacks: ExecutionCallbacks<ProfissionalDTO>) => {
          if (callbacks.onSuccess) callbacks.onSuccess({ id: 'prof-opt' } as ProfissionalDTO);
        }
      );

      component.save();

      const callArgs = profissionalService.save.mock.calls[0][0];
      expect(callArgs.banco).toBeUndefined();
      expect(callArgs.conta).toBeUndefined();
      expect(callArgs.chavePix).toBeUndefined();
      expect(callArgs.ativo).toBe(false);
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

  describe('Gestão de Pessoa', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve limpar pessoaSelecionada e pessoaId ao chamar limparPessoa', () => {
      component.pessoaSelecionada = { id: 'pessoa-x', nome: 'Alguém' } as unknown as PessoaDTO;
      component.form.get('pessoaId')?.setValue('pessoa-x');

      component.limparPessoa();

      expect(component.pessoaSelecionada).toBeNull();
      expect(component.form.get('pessoaId')?.value).toBe('');
    });
  });

  describe('Mensagens de Erro do Backend', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('NÃO deve fechar detalhe quando há erro do backend', () => {
      component.form.patchValue({
        pessoaId: 'pessoa-1',
        conselho: Conselho.CRM,
        codigoConselho: 'CRM-12345',
        tipoRemuneracao: TipoRemuneracao.CLT,
        ativo: true,
      });

      const mockError = {
        status: 400,
        error: { messages: ['Erro ao salvar profissional'] },
      };

      profissionalService.save.mockImplementation(
        (_data: ProfissionalDTO, callbacks: ExecutionCallbacks<ProfissionalDTO>) => {
          if (callbacks.onError) {
            callbacks.onError(mockError as unknown as HttpErrorResponse);
          }
        }
      );

      const closeDetailSpy = jest.fn();
      component.closeDetail.subscribe(closeDetailSpy);

      component.save();

      expect(profissionalService.save).toHaveBeenCalled();
      expect(closeDetailSpy).not.toHaveBeenCalled();
    });
  });
});
