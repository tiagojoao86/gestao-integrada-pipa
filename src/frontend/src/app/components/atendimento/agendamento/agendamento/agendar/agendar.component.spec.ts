import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AgendarComponent } from './agendar.component';
import { AgendamentoService } from '../agendamento.service';
import { AgendaService } from '../../agenda/agenda.service';
import { PessoaService } from '../../../../cadastro/pessoa/pessoa.service';
import { ConvenioService } from '../../../convenio/convenio.service';
import { ProcedimentoService } from '../../../procedimento/procedimento.service';
import { MessageService } from '../../../../base/messages/messages.service';
import { AuthService } from '../../../../base/auth/auth-service';
import { EntitySearchService } from '../../../../base/entity-search/entity-search.service';
import { of } from 'rxjs';
import { AgendamentoDTO } from '../model/agendamento-dto';
import { SlotDTO } from '../model/slot-dto';
import { Response } from '../../../../base/model/response';
import { ExecutionCallbacks } from '../../../../base/base-service';

describe('AgendarComponent', () => {
  let component: AgendarComponent;
  let fixture: ComponentFixture<AgendarComponent>;
  let agendamentoService: jest.Mocked<AgendamentoService>;
  let authService: jest.Mocked<AuthService>;
  let messageService: jest.Mocked<MessageService>;

  const agendamentoServiceMock = {
    findById: jest.fn(),
    save: jest.fn(),
    listarSlots: jest.fn(),
    conflitoPaciente: jest.fn(),
  };

  const authServiceMock = {
    hasAuthorityEditarToModulo: jest.fn(),
  };

  const messageServiceMock = {
    sucesso: jest.fn(),
    erro: jest.fn(),
    alerta: jest.fn(),
  };

  const entitySearchServiceMock = {
    search: jest.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AgendarComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: EntitySearchService, useValue: entitySearchServiceMock },
      ],
    })
      .overrideComponent(AgendarComponent, {
        set: {
          providers: [
            { provide: AgendamentoService, useValue: agendamentoServiceMock },
            { provide: AgendaService, useValue: {} },
            { provide: PessoaService, useValue: {} },
            { provide: ConvenioService, useValue: {} },
            { provide: ProcedimentoService, useValue: {} },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(AgendarComponent);
    component = fixture.componentInstance;
    agendamentoService = fixture.debugElement.injector.get(
      AgendamentoService
    ) as jest.Mocked<AgendamentoService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;
    messageService = TestBed.inject(MessageService) as jest.Mocked<MessageService>;

    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
    agendamentoServiceMock.findById.mockReturnValue(
      of({ body: new AgendamentoDTO() } as Response<AgendamentoDTO>)
    );
    agendamentoServiceMock.conflitoPaciente.mockReturnValue(
      of({ body: [] } as Response<AgendamentoDTO[]>)
    );
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  // =========================================================================
  // Inicialização
  // =========================================================================

  describe('Inicialização', () => {
    it('deve criar o componente', () => {
      expect(component).toBeTruthy();
    });

    it('deve inicializar formulário com campos obrigatórios', () => {
      component.detailId = 'add';
      component.ngOnInit();

      expect(component.form.get('agendaId')).toBeTruthy();
      expect(component.form.get('pacienteId')).toBeTruthy();
      expect(component.form.get('convenioId')).toBeTruthy();
      expect(component.form.get('procedimentoId')).toBeTruthy();
      expect(component.form.get('observacao')).toBeTruthy();
    });

    it('deve configurar toolbar com cancelar, salvar e cadastrar paciente quando tem permissão', () => {
      authService.hasAuthorityEditarToModulo.mockReturnValue(true);

      component.detailId = 'add';
      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(3);
      expect(component.toolbarActions[0].icon).toBe('close');
      expect(component.toolbarActions[1].icon).toBe('save');
      expect(component.toolbarActions[2].icon).toBe('person_add');
    });

    it('deve configurar toolbar apenas com cancelar quando sem permissão', () => {
      authService.hasAuthorityEditarToModulo.mockReturnValue(false);

      component.detailId = 'add';
      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(1);
      expect(component.toolbarActions[0].icon).toBe('close');
    });

    it('deve configurar toolbar sem salvar mas com cadastrar paciente quando somenteLeitura', () => {
      authService.hasAuthorityEditarToModulo.mockReturnValue(true);

      component.detailId = 'add';
      component.somenteLeitura = true;
      component.ngOnInit();

      expect(component.toolbarActions.length).toBe(2);
      expect(component.toolbarActions[0].icon).toBe('close');
      expect(component.toolbarActions[1].icon).toBe('person_add');
    });

    it('deve preencher agenda e slot ao iniciar em modo calendário', () => {
      component.detailId = 'add';
      component.agendaIdPre = 'agenda-123';
      component.agendaNomePre = 'Agenda Dr. João';
      component.slotInicio = '2026-04-28T09:00:00';
      component.slotFim = '2026-04-28T10:00:00';
      component.ngOnInit();

      expect(component.agendaSelecionada?.id).toBe('agenda-123');
      expect(component.slotsSelecionados).toHaveLength(1);
      expect(component.slotsSelecionados[0].dataHoraInicio).toBe('2026-04-28T09:00:00');
    });
  });

  // =========================================================================
  // toggleSlot
  // =========================================================================

  describe('toggleSlot', () => {
    beforeEach(() => {
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve adicionar slot livre ao selecionar', () => {
      const slot: SlotDTO = { dataHoraInicio: '2026-04-28T09:00:00', dataHoraFim: '2026-04-28T10:00:00', livre: true };

      component.toggleSlot(slot);

      expect(component.slotsSelecionados).toContain(slot);
    });

    it('deve remover slot ao clicar novamente', () => {
      const slot: SlotDTO = { dataHoraInicio: '2026-04-28T09:00:00', dataHoraFim: '2026-04-28T10:00:00', livre: true };

      component.toggleSlot(slot);
      component.toggleSlot(slot);

      expect(component.slotsSelecionados).not.toContain(slot);
    });

    it('não deve adicionar slot ocupado que não está selecionado', () => {
      const slot: SlotDTO = { dataHoraInicio: '2026-04-28T09:00:00', livre: false };

      component.toggleSlot(slot);

      expect(component.slotsSelecionados).toHaveLength(0);
    });
  });

  // =========================================================================
  // Validação antes de salvar
  // =========================================================================

  describe('salvar', () => {
    beforeEach(() => {
      authService.hasAuthorityEditarToModulo.mockReturnValue(true);
      component.detailId = 'add';
      component.ngOnInit();
    });

    it('deve exibir erro quando formulário inválido', () => {
      component.salvar();

      expect(messageService.erro).toHaveBeenCalled();
      expect(agendamentoService.save).not.toHaveBeenCalled();
    });

    it('deve exibir erro quando nenhum slot selecionado', () => {
      component.form.patchValue({ agendaId: 'ag-1', pacienteId: 'pac-1' });
      component.slotsSelecionados = [];

      component.salvar();

      expect(messageService.erro).toHaveBeenCalled();
      expect(agendamentoService.save).not.toHaveBeenCalled();
    });

    it('deve chamar service.save com dados corretos quando formulário válido', () => {
      component.form.patchValue({ agendaId: 'ag-1', pacienteId: 'pac-1' });
      component.slotsSelecionados = [
        { dataHoraInicio: '2026-04-28T09:00:00', dataHoraFim: '2026-04-28T10:00:00', livre: true },
      ];
      agendamentoServiceMock.save.mockImplementation((_dto, callbacks: ExecutionCallbacks<AgendamentoDTO>) => {
        callbacks.onSuccess?.(new AgendamentoDTO());
      });

      component.salvar();

      expect(agendamentoService.save).toHaveBeenCalled();
      expect(messageService.sucesso).toHaveBeenCalled();
    });

    it('não deve salvar quando somenteLeitura', () => {
      component.somenteLeitura = true;
      component.form.patchValue({ agendaId: 'ag-1', pacienteId: 'pac-1' });
      component.slotsSelecionados = [
        { dataHoraInicio: '2026-04-28T09:00:00', livre: true },
      ];

      component.salvar();

      expect(agendamentoService.save).not.toHaveBeenCalled();
    });
  });

  // =========================================================================
  // getSlotClass
  // =========================================================================

  describe('getSlotClass', () => {
    it('deve retornar classe selecionado para slot selecionado', () => {
      const slot: SlotDTO = { dataHoraInicio: '2026-04-28T09:00:00', livre: true };
      component.slotsSelecionados = [slot];

      expect(component.getSlotClass(slot)).toBe('slot slot--selecionado');
    });

    it('deve retornar classe livre para slot livre não selecionado', () => {
      const slot: SlotDTO = { dataHoraInicio: '2026-04-28T09:00:00', livre: true };
      component.slotsSelecionados = [];

      expect(component.getSlotClass(slot)).toBe('slot slot--livre');
    });

    it('deve retornar classe ocupado para slot ocupado não selecionado', () => {
      const slot: SlotDTO = { dataHoraInicio: '2026-04-28T09:00:00', livre: false };
      component.slotsSelecionados = [];

      expect(component.getSlotClass(slot)).toBe('slot slot--ocupado');
    });
  });
});
