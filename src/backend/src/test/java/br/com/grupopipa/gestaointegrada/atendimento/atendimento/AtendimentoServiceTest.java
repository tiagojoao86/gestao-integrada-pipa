package br.com.grupopipa.gestaointegrada.atendimento.atendimento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto.AtendimentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto.AtendimentoGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto.AtendimentoProcedimentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity.Atendimento;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioRepository;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.ConvenioCategoriaRepository;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoRepository;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.ProfissionalRepository;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.TabelaItemRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.setor.SetorRepository;
import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;

@DisplayName("AtendimentoService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class AtendimentoServiceTest {

    @Mock private AtendimentoRepository repository;
    @Mock private PessoaRepository pessoaRepository;
    @Mock private SetorRepository setorRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private ConvenioRepository convenioRepository;
    @Mock private ConvenioCategoriaRepository convenioCategoriaRepository;
    @Mock private ProcedimentoRepository procedimentoRepository;
    @Mock private TabelaItemRepository tabelaItemRepository;
    @Mock private Specifications<Atendimento> specifications;

    @InjectMocks
    private AtendimentoServiceImpl service;

    private UUID setorId;
    private UUID pacienteId;
    private UUID profAtendimentoId;
    private UUID profResponsavelId;
    private UUID procedimentoId;

    private Setor setor;
    private Pessoa paciente;
    private Profissional profAtendimento;
    private Profissional profResponsavel;
    private Procedimento procedimento;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    @BeforeEach
    void setup() {
        setorId = UUID.randomUUID();
        pacienteId = UUID.randomUUID();
        profAtendimentoId = UUID.randomUUID();
        profResponsavelId = UUID.randomUUID();
        procedimentoId = UUID.randomUUID();

        dataInicio = LocalDateTime.of(2026, 4, 11, 9, 0);
        dataFim = LocalDateTime.of(2026, 4, 11, 23, 59, 59);

        // Usar mocks para entidades com construtores de validação complexa
        setor = mock(Setor.class);
        lenient().when(setor.getId()).thenReturn(setorId);
        lenient().when(setor.getNome()).thenReturn("Clínica");

        paciente = mock(Pessoa.class);
        lenient().when(paciente.getId()).thenReturn(pacienteId);
        lenient().when(paciente.getNome()).thenReturn("Maria da Silva");
        lenient().when(paciente.getResponsavel()).thenReturn(null);

        Pessoa pessoaProf = mock(Pessoa.class);
        lenient().when(pessoaProf.getNome()).thenReturn("Dr. João");

        profAtendimento = mock(Profissional.class);
        lenient().when(profAtendimento.getId()).thenReturn(profAtendimentoId);
        lenient().when(profAtendimento.getPessoa()).thenReturn(pessoaProf);

        profResponsavel = mock(Profissional.class);
        lenient().when(profResponsavel.getId()).thenReturn(profResponsavelId);
        lenient().when(profResponsavel.getPessoa()).thenReturn(pessoaProf);

        procedimento = mock(Procedimento.class);
        lenient().when(procedimento.getId()).thenReturn(procedimentoId);
        lenient().when(procedimento.getCodigo()).thenReturn("PROC-001");
        lenient().when(procedimento.getDescricao()).thenReturn("Terapia ABA");
    }

    private AtendimentoDTO dtoPadrao() {
        return AtendimentoDTO.builder()
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .setorId(setorId)
                .pacienteId(pacienteId)
                .profissionalAtendimentoId(profAtendimentoId)
                .profissionalResponsavelId(profResponsavelId)
                .procedimentos(List.of(
                    AtendimentoProcedimentoDTO.builder()
                        .procedimentoId(procedimentoId)
                        .dataInicio(dataInicio)
                        .dataFim(dataFim)
                        .build()
                ))
                .build();
    }

    private void mockDependencias() {
        when(setorRepository.findById(setorId)).thenReturn(Optional.of(setor));
        when(pessoaRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(profAtendimentoId)).thenReturn(Optional.of(profAtendimento));
        when(profissionalRepository.findById(profResponsavelId)).thenReturn(Optional.of(profResponsavel));
        when(procedimentoRepository.findById(procedimentoId)).thenReturn(Optional.of(procedimento));
        when(tabelaItemRepository.findItemVigenteParaProcedimento(any(), any(), anyBoolean()))
                .thenReturn(Optional.empty());
        when(repository.save(any(Atendimento.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // =========================================================================
    // Criação
    // =========================================================================

    @Test
    @DisplayName("Deve criar novo atendimento com dados válidos")
    void deveCriarNovoAtendimento() {
        mockDependencias();

        AtendimentoDTO resultado = service.save(dtoPadrao());

        assertThat(resultado).isNotNull();
        assertThat(resultado.getDataInicio()).isEqualTo(dataInicio);
        assertThat(resultado.getDataFim()).isEqualTo(dataFim);
        assertThat(resultado.getSetorId()).isEqualTo(setorId);
        assertThat(resultado.getPacienteId()).isEqualTo(pacienteId);
        assertThat(resultado.getProcedimentos()).hasSize(1);
        verify(repository, times(1)).save(any(Atendimento.class));
    }

    @Test
    @DisplayName("Deve calcular dataFim automática como fim do dia quando não informada")
    void deveCalcularDataFimAutomatica() {
        mockDependencias();
        AtendimentoDTO dto = dtoPadrao();
        dto.setDataFim(null);

        AtendimentoDTO resultado = service.save(dto);

        LocalDateTime fimEsperado = dataInicio.toLocalDate().atTime(LocalTime.of(23, 59, 59));
        assertThat(resultado.getDataFim()).isEqualTo(fimEsperado);
    }

    @Test
    @DisplayName("Deve lançar exceção quando não há procedimentos")
    void deveLancarExcecaoSemProcedimentos() {
        when(setorRepository.findById(setorId)).thenReturn(Optional.of(setor));
        when(pessoaRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(profAtendimentoId)).thenReturn(Optional.of(profAtendimento));
        when(profissionalRepository.findById(profResponsavelId)).thenReturn(Optional.of(profResponsavel));

        AtendimentoDTO dto = dtoPadrao();
        dto.setProcedimentos(List.of());

        assertThatThrownBy(() -> service.save(dto))
                .isInstanceOf(BeanValidationException.class);
        verify(repository, never()).save(any());
    }

    // =========================================================================
    // Responsável — fallback para responsável do paciente
    // =========================================================================

    @Test
    @DisplayName("Deve herdar responsável do paciente quando não informado no atendimento")
    void deveHerdarResponsavelDoPaciente() {
        Pessoa responsavel = mock(Pessoa.class);
        UUID responsavelId = UUID.randomUUID();
        when(responsavel.getId()).thenReturn(responsavelId);
        when(responsavel.getNome()).thenReturn("Ana Responsável");

        when(paciente.getResponsavel()).thenReturn(responsavel);

        mockDependencias();
        AtendimentoDTO dto = dtoPadrao();
        dto.setResponsavelId(null);

        AtendimentoDTO resultado = service.save(dto);

        assertThat(resultado.getResponsavelNome()).isEqualTo("Ana Responsável");
    }

    @Test
    @DisplayName("Deve usar responsável explícito em vez do do paciente")
    void deveUsarResponsavelExplicito() {
        UUID responsavelExplicitoId = UUID.randomUUID();
        Pessoa responsavelExplicito = mock(Pessoa.class);
        when(responsavelExplicito.getId()).thenReturn(responsavelExplicitoId);
        when(responsavelExplicito.getNome()).thenReturn("Carlos Responsável");

        mockDependencias();
        when(pessoaRepository.findById(responsavelExplicitoId))
                .thenReturn(Optional.of(responsavelExplicito));

        AtendimentoDTO dto = dtoPadrao();
        dto.setResponsavelId(responsavelExplicitoId);

        AtendimentoDTO resultado = service.save(dto);

        assertThat(resultado.getResponsavelId()).isEqualTo(responsavelExplicitoId);
        assertThat(resultado.getResponsavelNome()).isEqualTo("Carlos Responsável");
    }

    // =========================================================================
    // Validação de categoria de convênio
    // =========================================================================

    @Test
    @DisplayName("Deve rejeitar categoria que não pertence ao convênio informado")
    void deveRejeitarCategoriaDeOutroConvenio() {
        UUID convenioId = UUID.randomUUID();
        UUID outroConvenioId = UUID.randomUUID();
        UUID categoriaId = UUID.randomUUID();

        Convenio convenio = mock(Convenio.class);
        when(convenio.getId()).thenReturn(convenioId);

        Convenio outroConvenio = mock(Convenio.class);
        when(outroConvenio.getId()).thenReturn(outroConvenioId);

        ConvenioCategoria categoria = mock(ConvenioCategoria.class);
        when(categoria.getConvenio()).thenReturn(outroConvenio);

        when(setorRepository.findById(setorId)).thenReturn(Optional.of(setor));
        when(pessoaRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(convenioRepository.findById(convenioId)).thenReturn(Optional.of(convenio));
        when(convenioCategoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));

        AtendimentoDTO dto = dtoPadrao();
        dto.setConvenioId(convenioId);
        dto.setConvenioCategoriaId(categoriaId);

        assertThatThrownBy(() -> service.save(dto))
                .isInstanceOf(BeanValidationException.class);
        verify(repository, never()).save(any());
    }

    // =========================================================================
    // buildGridDTOFromEntity
    // =========================================================================

    @Test
    @DisplayName("Deve construir GridDTO corretamente da entidade")
    void deveConstruirGridDTODaEntidade() {
        Atendimento atendimento = mock(Atendimento.class);
        when(atendimento.getDataInicio()).thenReturn(dataInicio);
        when(atendimento.getPaciente()).thenReturn(paciente);
        when(atendimento.getProfissionalAtendimento()).thenReturn(profAtendimento);
        when(atendimento.getProcedimentos()).thenReturn(List.of());
        when(atendimento.getConvenio()).thenReturn(null);
        when(atendimento.getCreatedAt()).thenReturn(null);
        when(atendimento.getDeleted()).thenReturn(false);

        AtendimentoGridDTO grid = service.buildGridDTOFromEntity(atendimento);

        assertThat(grid).isNotNull();
        assertThat(grid.getDataInicio()).isEqualTo(dataInicio);
        assertThat(grid.getPacienteNome()).isEqualTo("Maria da Silva");
        assertThat(grid.getProcedimentosCount()).isZero();
    }

    // =========================================================================
    // Soft delete
    // =========================================================================

    @Test
    @DisplayName("Deve realizar soft delete do atendimento")
    void deveRealizarSoftDelete() {
        Atendimento atendimento = mock(Atendimento.class);
        UUID atendimentoId = UUID.randomUUID();

        when(repository.findById(atendimentoId)).thenReturn(Optional.of(atendimento));
        when(repository.save(any(Atendimento.class))).thenReturn(atendimento);

        UUID resultado = service.delete(atendimentoId);

        assertThat(resultado).isEqualTo(atendimentoId);
        verify(repository, times(1)).save(atendimento);
        verify(repository, never()).deleteById(any());
    }
}
