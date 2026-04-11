package br.com.grupopipa.gestaointegrada.atendimento.tabela;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoRepository;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.dto.TabelaDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.dto.TabelaGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.dto.TabelaItemDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.Tabela;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.TabelaItem;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;

@DisplayName("TabelaService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class TabelaServiceTest {

    @Mock
    private TabelaRepository repository;

    @Mock
    private TabelaItemRepository tabelaItemRepository;

    @Mock
    private ProcedimentoRepository procedimentoRepository;

    @Mock
    private Specifications<Tabela> specifications;

    @InjectMocks
    private TabelaServiceImpl service;

    private UUID tabelaId;
    private UUID procedimentoId;
    private Tabela tabela;
    private Procedimento procedimento;
    private TabelaDTO dtoValido;

    @BeforeEach
    void setup() {
        tabelaId = UUID.randomUUID();
        procedimentoId = UUID.randomUUID();

        tabela = new Tabela.Builder()
                .nome("Tabela Particular")
                .tipo(TipoTabela.PARTICULAR)
                .ativo(true)
                .build();
        setId(tabela, tabelaId);

        procedimento = new Procedimento.Builder()
                .codigo("PROC-001")
                .descricao("Terapia ABA")
                .ativo(true)
                .build();
        setId(procedimento, procedimentoId);

        dtoValido = TabelaDTO.builder()
                .nome("Tabela Particular")
                .tipo(TipoTabela.PARTICULAR)
                .ativo(true)
                .itens(List.of())
                .build();
    }

    // =========================================================================
    // Criação e atualização
    // =========================================================================

    @Test
    @DisplayName("Deve criar nova tabela sem itens")
    void deveCriarNovaTabelaSemItens() {
        when(repository.save(any(Tabela.class))).thenAnswer(inv -> {
            Tabela t = inv.getArgument(0);
            setId(t, tabelaId);
            return t;
        });
        when(repository.findById(tabelaId)).thenReturn(Optional.of(tabela));
        when(tabelaItemRepository.findAllByTabelaId(tabelaId)).thenReturn(List.of());

        TabelaDTO resultado = service.save(dtoValido);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Tabela Particular");
        assertThat(resultado.getTipo()).isEqualTo(TipoTabela.PARTICULAR);
        assertThat(resultado.getAtivo()).isTrue();
        assertThat(resultado.getItens()).isEmpty();
        verify(repository, times(1)).save(any(Tabela.class));
    }

    @Test
    @DisplayName("Deve atualizar tabela existente")
    void deveAtualizarTabelaExistente() {
        dtoValido = TabelaDTO.builder()
                .id(tabelaId)
                .nome("Tabela Convênio")
                .tipo(TipoTabela.CONVENIO)
                .ativo(true)
                .itens(List.of())
                .build();

        // findById é chamado 2x: CrudServiceImpl.save() + syncItens(findEntityById)
        when(repository.findById(tabelaId)).thenReturn(Optional.of(tabela));
        when(repository.save(any(Tabela.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tabelaItemRepository.findAllByTabelaId(tabelaId)).thenReturn(List.of());

        TabelaDTO resultado = service.save(dtoValido);

        assertThat(resultado.getNome()).isEqualTo("Tabela Convênio");
        assertThat(resultado.getTipo()).isEqualTo(TipoTabela.CONVENIO);
        verify(repository, times(2)).findById(tabelaId);
        verify(repository, times(1)).save(any(Tabela.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar tabela sem nome")
    void deveLancarExcecaoSemNome() {
        dtoValido = TabelaDTO.builder()
                .nome(null)
                .tipo(TipoTabela.PARTICULAR)
                .ativo(true)
                .itens(List.of())
                .build();

        assertThatThrownBy(() -> service.save(dtoValido))
                .isInstanceOf(BeanValidationException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar tabela sem tipo")
    void deveLancarExcecaoSemTipo() {
        dtoValido = TabelaDTO.builder()
                .nome("Tabela")
                .tipo(null)
                .ativo(true)
                .itens(List.of())
                .build();

        assertThatThrownBy(() -> service.save(dtoValido))
                .isInstanceOf(BeanValidationException.class);
        verify(repository, never()).save(any());
    }

    // =========================================================================
    // Gestão de itens
    // =========================================================================

    @Test
    @DisplayName("Deve adicionar item à tabela")
    void deveAdicionarItemATabela() {
        TabelaItemDTO itemDTO = TabelaItemDTO.builder()
                .procedimentoId(procedimentoId)
                .valor(new BigDecimal("150.00"))
                .vigenciaInicio(LocalDate.of(2026, 1, 1))
                .build();

        dtoValido = TabelaDTO.builder()
                .nome("Tabela Particular")
                .tipo(TipoTabela.PARTICULAR)
                .ativo(true)
                .itens(List.of(itemDTO))
                .build();

        when(repository.save(any(Tabela.class))).thenAnswer(inv -> {
            Tabela t = inv.getArgument(0);
            setId(t, tabelaId);
            return t;
        });
        when(repository.findById(tabelaId)).thenReturn(Optional.of(tabela));
        when(procedimentoRepository.findById(procedimentoId)).thenReturn(Optional.of(procedimento));
        when(tabelaItemRepository.findAllByTabelaId(tabelaId)).thenReturn(List.of());
        when(tabelaItemRepository.findItensAtivosConflitantes(
                any(), any(), any(), any())).thenReturn(List.of());
        when(tabelaItemRepository.save(any(TabelaItem.class))).thenAnswer(inv -> inv.getArgument(0));

        service.save(dtoValido);

        verify(tabelaItemRepository, times(1)).save(any(TabelaItem.class));
    }

    @Test
    @DisplayName("Deve rejeitar item com vigência conflitante")
    void deveRejeitarItemComVigenciaConflitante() {
        TabelaItem itemExistente = new TabelaItem.Builder()
                .tabela(tabela)
                .procedimento(procedimento)
                .valor(new BigDecimal("100.00"))
                .vigenciaInicio(LocalDate.of(2026, 1, 1))
                .build();

        TabelaItemDTO itemDTO = TabelaItemDTO.builder()
                .procedimentoId(procedimentoId)
                .valor(new BigDecimal("150.00"))
                .vigenciaInicio(LocalDate.of(2026, 1, 1))
                .build();

        dtoValido = TabelaDTO.builder()
                .nome("Tabela Particular")
                .tipo(TipoTabela.PARTICULAR)
                .ativo(true)
                .itens(List.of(itemDTO))
                .build();

        when(repository.save(any(Tabela.class))).thenAnswer(inv -> {
            Tabela t = inv.getArgument(0);
            setId(t, tabelaId);
            return t;
        });
        when(repository.findById(tabelaId)).thenReturn(Optional.of(tabela));
        when(procedimentoRepository.findById(procedimentoId)).thenReturn(Optional.of(procedimento));
        when(tabelaItemRepository.findAllByTabelaId(tabelaId)).thenReturn(List.of());
        when(tabelaItemRepository.findItensAtivosConflitantes(any(), any(), any(), any()))
                .thenReturn(List.of(itemExistente));

        assertThatThrownBy(() -> service.save(dtoValido))
                .isInstanceOf(BeanValidationException.class);

        verify(tabelaItemRepository, never()).save(any(TabelaItem.class));
    }

    @Test
    @DisplayName("Deve remover item ao não incluir na lista de itens")
    void deveRemoverItemAusenteDaLista() {
        UUID itemId = UUID.randomUUID();
        TabelaItem itemExistente = new TabelaItem.Builder()
                .tabela(tabela)
                .procedimento(procedimento)
                .valor(new BigDecimal("100.00"))
                .vigenciaInicio(LocalDate.of(2026, 1, 1))
                .build();
        setId(itemExistente, itemId);

        // DTO enviado sem o item existente → deve deletar
        dtoValido = TabelaDTO.builder()
                .id(tabelaId)
                .nome("Tabela Particular")
                .tipo(TipoTabela.PARTICULAR)
                .ativo(true)
                .itens(List.of())
                .build();

        when(repository.findById(tabelaId)).thenReturn(Optional.of(tabela));
        when(repository.save(any(Tabela.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tabelaItemRepository.findAllByTabelaId(tabelaId)).thenReturn(List.of(itemExistente));

        service.save(dtoValido);

        verify(tabelaItemRepository, times(1)).deleteById(itemId);
    }

    // =========================================================================
    // buildDTOFromEntity / buildGridDTOFromEntity
    // =========================================================================

    @Test
    @DisplayName("Deve construir DTO corretamente da entidade")
    void deveConstruirDTODaEntidade() {
        when(tabelaItemRepository.findAllByTabelaId(tabelaId)).thenReturn(List.of());

        TabelaDTO dto = service.buildDTOFromEntity(tabela);

        assertThat(dto).isNotNull();
        assertThat(dto.getNome()).isEqualTo("Tabela Particular");
        assertThat(dto.getTipo()).isEqualTo(TipoTabela.PARTICULAR);
        assertThat(dto.getAtivo()).isTrue();
        assertThat(dto.getItens()).isEmpty();
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente da entidade")
    void deveConstruirGridDTODaEntidade() {
        TabelaGridDTO grid = service.buildGridDTOFromEntity(tabela);

        assertThat(grid).isNotNull();
        assertThat(grid.getNome()).isEqualTo("Tabela Particular");
        assertThat(grid.getTipo()).isEqualTo(TipoTabela.PARTICULAR);
        assertThat(grid.getAtivo()).isTrue();
    }

    // =========================================================================
    // Soft delete
    // =========================================================================

    @Test
    @DisplayName("Deve realizar soft delete da tabela")
    void deveRealizarSoftDelete() {
        when(repository.findById(tabelaId)).thenReturn(Optional.of(tabela));
        when(repository.save(any(Tabela.class))).thenReturn(tabela);

        UUID resultado = service.delete(tabelaId);

        assertThat(resultado).isEqualTo(tabelaId);
        assertThat(tabela.getDeleted()).isTrue();
        assertThat(tabela.getDeletedAt()).isNotNull();
        verify(repository, never()).deleteById(any());
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private static void setId(Object entity, UUID id) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
