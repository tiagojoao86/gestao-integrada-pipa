package br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.dto.CodigoConvenioDTO;
import br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.entity.CodigoConvenio;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoRepository;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;

@DisplayName("CodigoConvenioServiceImpl - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class CodigoConvenioServiceImplTest {

    @Mock
    private CodigoConvenioRepository repository;

    @Mock
    private ProcedimentoRepository procedimentoRepository;

    @InjectMocks
    private CodigoConvenioServiceImpl service;

    private UUID convenioId;
    private UUID procedimentoId;
    private UUID codigoId;

    private Convenio convenio;
    private Procedimento procedimento;
    private CodigoConvenio codigoConvenio;

    @BeforeEach
    void setup() {
        convenioId = UUID.randomUUID();
        procedimentoId = UUID.randomUUID();
        codigoId = UUID.randomUUID();

        convenio = mock(Convenio.class);
        lenient().when(convenio.getId()).thenReturn(convenioId);

        procedimento = mock(Procedimento.class);
        lenient().when(procedimento.getId()).thenReturn(procedimentoId);
        lenient().when(procedimento.getCodigo()).thenReturn("PROC-001");
        lenient().when(procedimento.getDescricao()).thenReturn("Terapia ABA");

        codigoConvenio = mock(CodigoConvenio.class);
        lenient().when(codigoConvenio.getId()).thenReturn(codigoId);
        lenient().when(codigoConvenio.getConvenio()).thenReturn(convenio);
        lenient().when(codigoConvenio.getProcedimento()).thenReturn(procedimento);
        lenient().when(codigoConvenio.getCodigo()).thenReturn("TRP-001");
    }

    // =========================================================================
    // findAllByConvenioId
    // =========================================================================

    @Test
    @DisplayName("Deve retornar lista vazia quando não há códigos para o convênio")
    void deveRetornarListaVaziaQuandoSemCodigos() {
        when(repository.findAllByConvenioId(convenioId)).thenReturn(List.of());

        List<CodigoConvenioDTO> resultado = service.findAllByConvenioId(convenioId);

        assertThat(resultado).isEmpty();
        verify(repository, times(1)).findAllByConvenioId(convenioId);
    }

    @Test
    @DisplayName("Deve retornar DTOs mapeados corretamente")
    void deveRetornarDTOsMapeadosCorretamente() {
        when(repository.findAllByConvenioId(convenioId)).thenReturn(List.of(codigoConvenio));

        List<CodigoConvenioDTO> resultado = service.findAllByConvenioId(convenioId);

        assertThat(resultado).hasSize(1);
        CodigoConvenioDTO dto = resultado.get(0);
        assertThat(dto.getId()).isEqualTo(codigoId);
        assertThat(dto.getConvenioId()).isEqualTo(convenioId);
        assertThat(dto.getProcedimentoId()).isEqualTo(procedimentoId);
        assertThat(dto.getProcedimentoCodigo()).isEqualTo("PROC-001");
        assertThat(dto.getProcedimentoDescricao()).isEqualTo("Terapia ABA");
        assertThat(dto.getCodigo()).isEqualTo("TRP-001");
    }

    @Test
    @DisplayName("Deve retornar múltiplos DTOs quando há vários códigos")
    void deveRetornarMultiplosDTOs() {
        UUID proc2Id = UUID.randomUUID();
        Procedimento proc2 = mock(Procedimento.class);
        when(proc2.getId()).thenReturn(proc2Id);
        when(proc2.getCodigo()).thenReturn("PROC-002");
        when(proc2.getDescricao()).thenReturn("Fonoaudiologia");

        CodigoConvenio codigo2 = mock(CodigoConvenio.class);
        when(codigo2.getId()).thenReturn(UUID.randomUUID());
        when(codigo2.getConvenio()).thenReturn(convenio);
        when(codigo2.getProcedimento()).thenReturn(proc2);
        when(codigo2.getCodigo()).thenReturn("FONO-002");

        when(repository.findAllByConvenioId(convenioId)).thenReturn(List.of(codigoConvenio, codigo2));

        List<CodigoConvenioDTO> resultado = service.findAllByConvenioId(convenioId);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getCodigo()).isEqualTo("TRP-001");
        assertThat(resultado.get(1).getCodigo()).isEqualTo("FONO-002");
    }

    @Test
    @DisplayName("Deve mapear null quando procedimento é null")
    void deveMapearNullQuandoProcedimentoENull() {
        CodigoConvenio semProcedimento = mock(CodigoConvenio.class);
        when(semProcedimento.getId()).thenReturn(codigoId);
        when(semProcedimento.getConvenio()).thenReturn(convenio);
        when(semProcedimento.getProcedimento()).thenReturn(null);
        when(semProcedimento.getCodigo()).thenReturn("TRP-X");

        when(repository.findAllByConvenioId(convenioId)).thenReturn(List.of(semProcedimento));

        List<CodigoConvenioDTO> resultado = service.findAllByConvenioId(convenioId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getProcedimentoId()).isNull();
        assertThat(resultado.get(0).getProcedimentoCodigo()).isNull();
        assertThat(resultado.get(0).getProcedimentoDescricao()).isNull();
    }

    // =========================================================================
    // syncForConvenio
    // =========================================================================

    @Test
    @DisplayName("Deve deletar todos os códigos quando lista é null")
    void deveDeletarTodosQuandoListaENull() {
        service.syncForConvenio(convenio, null);

        verify(repository, times(1)).deleteAllByConvenioId(convenioId);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar todos os códigos quando lista é vazia")
    void deveDeletarTodosQuandoListaEVazia() {
        service.syncForConvenio(convenio, List.of());

        verify(repository, times(1)).deleteAllByConvenioId(convenioId);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar novo código quando procedimento não existe ainda")
    void deveCriarNovoCodigo() {
        CodigoConvenioDTO dto = CodigoConvenioDTO.builder()
                .procedimentoId(procedimentoId)
                .codigo("TRP-NOVO")
                .build();

        when(repository.findAllByConvenioId(convenioId)).thenReturn(List.of());
        when(procedimentoRepository.findById(procedimentoId)).thenReturn(Optional.of(procedimento));
        when(repository.save(any(CodigoConvenio.class))).thenAnswer(inv -> inv.getArgument(0));

        service.syncForConvenio(convenio, List.of(dto));

        verify(procedimentoRepository, times(1)).findById(procedimentoId);
        verify(repository, times(1)).save(any(CodigoConvenio.class));
    }

    @Test
    @DisplayName("Deve atualizar código existente")
    void deveAtualizarCodigoExistente() {
        CodigoConvenioDTO dto = CodigoConvenioDTO.builder()
                .procedimentoId(procedimentoId)
                .codigo("TRP-ATUALIZADO")
                .build();

        when(repository.findAllByConvenioId(convenioId)).thenReturn(List.of(codigoConvenio));
        when(repository.save(any(CodigoConvenio.class))).thenReturn(codigoConvenio);

        service.syncForConvenio(convenio, List.of(dto));

        verify(codigoConvenio, times(1)).atualizar("TRP-ATUALIZADO");
        verify(repository, times(1)).save(codigoConvenio);
        verify(procedimentoRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve remover código quando procedimento não está na lista incoming")
    void deveRemoverCodigoAusente() {
        UUID outroProcedimentoId = UUID.randomUUID();
        CodigoConvenioDTO dto = CodigoConvenioDTO.builder()
                .procedimentoId(outroProcedimentoId)
                .codigo("OUTRO-001")
                .build();

        Procedimento outroProcedimento = mock(Procedimento.class);
        lenient().when(outroProcedimento.getId()).thenReturn(outroProcedimentoId);

        when(repository.findAllByConvenioId(convenioId)).thenReturn(List.of(codigoConvenio));
        when(procedimentoRepository.findById(outroProcedimentoId)).thenReturn(Optional.of(outroProcedimento));
        when(repository.save(any(CodigoConvenio.class))).thenAnswer(inv -> inv.getArgument(0));

        service.syncForConvenio(convenio, List.of(dto));

        // codigoConvenio (proc original) deve ser deletado pois não está na lista
        verify(repository, times(1)).deleteById(codigoId);
        // Novo código deve ser criado para o outro procedimento
        verify(repository, times(1)).save(any(CodigoConvenio.class));
    }

    @Test
    @DisplayName("Deve ignorar DTO com procedimentoId null")
    void deveIgnorarDTOSemProcedimentoId() {
        CodigoConvenioDTO dtoInvalido = CodigoConvenioDTO.builder()
                .procedimentoId(null)
                .codigo("TRP-X")
                .build();

        when(repository.findAllByConvenioId(convenioId)).thenReturn(List.of());

        service.syncForConvenio(convenio, List.of(dtoInvalido));

        verify(procedimentoRepository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve ignorar DTO com codigo null")
    void deveIgnorarDTOSemCodigo() {
        CodigoConvenioDTO dtoInvalido = CodigoConvenioDTO.builder()
                .procedimentoId(procedimentoId)
                .codigo(null)
                .build();

        when(repository.findAllByConvenioId(convenioId)).thenReturn(List.of());

        service.syncForConvenio(convenio, List.of(dtoInvalido));

        verify(procedimentoRepository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve ignorar DTO quando procedimento não encontrado no repositório")
    void deveIgnorarDTOQuandoProcedimentoNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();
        CodigoConvenioDTO dto = CodigoConvenioDTO.builder()
                .procedimentoId(idInexistente)
                .codigo("TRP-INEXISTENTE")
                .build();

        when(repository.findAllByConvenioId(convenioId)).thenReturn(List.of());
        when(procedimentoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        service.syncForConvenio(convenio, List.of(dto));

        verify(procedimentoRepository, times(1)).findById(idInexistente);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve processar múltiplos códigos: criar, atualizar e remover")
    void deveProcessarMultiplosCodigos() {
        UUID proc2Id = UUID.randomUUID();
        UUID proc3Id = UUID.randomUUID();

        Procedimento proc2 = mock(Procedimento.class);
        when(proc2.getId()).thenReturn(proc2Id);

        Procedimento proc3 = mock(Procedimento.class);
        lenient().when(proc3.getId()).thenReturn(proc3Id);

        CodigoConvenio codigoProc2 = mock(CodigoConvenio.class);
        when(codigoProc2.getId()).thenReturn(UUID.randomUUID());
        when(codigoProc2.getProcedimento()).thenReturn(proc2);

        // Existing: proc1 (to be updated), proc2 (to be deleted)
        when(repository.findAllByConvenioId(convenioId)).thenReturn(List.of(codigoConvenio, codigoProc2));

        // Incoming: proc1 (update), proc3 (create) - proc2 is removed
        CodigoConvenioDTO dtoAtualizar = CodigoConvenioDTO.builder()
                .procedimentoId(procedimentoId)
                .codigo("TRP-UPDATED")
                .build();
        CodigoConvenioDTO dtoCriar = CodigoConvenioDTO.builder()
                .procedimentoId(proc3Id)
                .codigo("NOVO-003")
                .build();

        when(procedimentoRepository.findById(proc3Id)).thenReturn(Optional.of(proc3));
        when(repository.save(any(CodigoConvenio.class))).thenAnswer(inv -> inv.getArgument(0));

        service.syncForConvenio(convenio, List.of(dtoAtualizar, dtoCriar));

        // proc2 should be deleted
        verify(repository, times(1)).deleteById(codigoProc2.getId());
        // proc1 should be updated
        verify(codigoConvenio, times(1)).atualizar("TRP-UPDATED");
        verify(repository, times(1)).save(codigoConvenio);
        // proc3 should be created
        verify(procedimentoRepository, times(1)).findById(proc3Id);
        // save called 2x: update proc1 + create proc3
        verify(repository, times(2)).save(any(CodigoConvenio.class));
    }
}
