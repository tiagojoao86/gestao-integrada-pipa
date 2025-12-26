package br.com.grupopipa.gestaointegrada.cadastro.setor;

import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.DeletedEntityException;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.financeiro.centrocusto.CentroCustoRepository;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CentroCusto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("SetorService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class SetorServiceTest {

    @Mock
    private SetorRepository repository;

    @Mock
    private CentroCustoRepository centroCustoRepository;

    @Mock
    private Specifications<Setor> specifications;

    @InjectMocks
    private SetorServiceImpl service;

    private SetorDTO dtoValido;
    private Setor entidadeValida;
    private CentroCusto centroCusto;
    private UnidadeNegocio unidadeNegocio;
    private UUID setorId;
    private UUID centroCustoId;
    private UUID unidadeId;

    @BeforeEach
    void setup() {
        setorId = UUID.randomUUID();
        centroCustoId = UUID.randomUUID();
        unidadeId = UUID.randomUUID();

        // Criar UnidadeNegocio mock
        unidadeNegocio = new UnidadeNegocio.Builder()
                .codigo("UN_TEST")
                .nome("Unidade Teste")
                .cnpj("11222333000181")
                .build();
        // Usar reflexão para setar o ID na entidade
        try {
            java.lang.reflect.Field idField = UnidadeNegocio.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(unidadeNegocio, unidadeId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Criar CentroCusto mock
        centroCusto = new CentroCusto.Builder()
                .nome("Centro Custo Teste")
                .centroResultado(Boolean.FALSE)
                .unidadeNegocio(unidadeNegocio)
                .build();
        // Usar reflexão para setar o ID na entidade
        try {
            java.lang.reflect.Field idField = CentroCusto.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(centroCusto, centroCustoId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // DTO válido
        dtoValido = SetorDTO.builder()
                .nome("Setor Teste")
                .descricao("Descrição do setor teste")
                .centroCustoId(centroCustoId)
                .build();

        // Entidade válida
        entidadeValida = new Setor.Builder()
                .nome("Setor Teste")
                .descricao("Descrição do setor teste")
                .centroCusto(centroCusto)
                .build();
    }

    @Test
    @DisplayName("Deve criar novo setor")
    void deveCriarNovoSetor() {
        when(centroCustoRepository.findById(centroCustoId)).thenReturn(Optional.of(centroCusto));
        when(repository.save(any(Setor.class))).thenReturn(entidadeValida);

        SetorDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        assertEquals("Setor Teste", resultado.getNome());
        assertEquals("Descrição do setor teste", resultado.getDescricao());
        verify(repository, times(1)).save(any(Setor.class));
        verify(centroCustoRepository, times(1)).findById(centroCustoId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando CentroCusto não existe")
    void deveLancarExcecaoQuandoCentroCustoNaoExiste() {
        UUID centroCustoInexistente = UUID.randomUUID();
        when(centroCustoRepository.findById(centroCustoInexistente)).thenReturn(Optional.empty());

        SetorDTO dtoInvalido = SetorDTO.builder()
                .nome("Setor Teste")
                .centroCustoId(centroCustoInexistente)
                .build();

        assertThrows(EntityNotFoundException.class, () -> service.save(dtoInvalido));
        verify(centroCustoRepository, times(1)).findById(centroCustoInexistente);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar setor por id")
    void deveBuscarSetorPorId() {
        when(repository.findById(setorId)).thenReturn(Optional.of(entidadeValida));

        SetorDTO resultado = service.findById(setorId);

        assertNotNull(resultado);
        assertEquals("Setor Teste", resultado.getNome());
        assertEquals("Descrição do setor teste", resultado.getDescricao());
        assertEquals(centroCustoId, resultado.getCentroCustoId());
        assertEquals("Centro Custo Teste", resultado.getCentroCustoNome());
        verify(repository, times(1)).findById(setorId);
    }

    @Test
    @DisplayName("Deve deletar setor (deprecated - usar soft delete)")
    void deveDeletarSetor() {
        when(repository.findById(setorId)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(Setor.class))).thenReturn(entidadeValida);

        UUID resultado = service.delete(setorId);

        assertEquals(setorId, resultado);
        verify(repository, times(1)).findById(setorId);
        verify(repository, times(1)).save(any(Setor.class));
    }

    @Test
    @DisplayName("Deve construir DTO corretamente da entidade")
    void deveConstruirDTOCorretamenteDaEntidade() {
        SetorDTO dto = service.buildDTOFromEntity(entidadeValida);

        assertNotNull(dto);
        assertEquals("Setor Teste", dto.getNome());
        assertEquals("Descrição do setor teste", dto.getDescricao());
        assertEquals("Centro Custo Teste", dto.getCentroCustoNome());
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente da entidade")
    void deveConstruirGridDTOCorretamenteDaEntidade() {
        SetorGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);

        assertNotNull(gridDTO);
        assertEquals("Setor Teste", gridDTO.getNome());
        assertEquals("Descrição do setor teste", gridDTO.getDescricao());
        assertEquals("Centro Custo Teste", gridDTO.getCentroCustoNome());
    }

    @Test
    @DisplayName("Deve atualizar setor existente")
    void deveAtualizarSetorExistente() {
        Setor entidadeExistente = new Setor.Builder()
                .nome("Nome Antigo")
                .descricao("Descrição antiga")
                .centroCusto(centroCusto)
                .build();

        when(centroCustoRepository.findById(centroCustoId)).thenReturn(Optional.of(centroCusto));
        when(repository.findById(setorId)).thenReturn(Optional.of(entidadeExistente));
        when(repository.save(any(Setor.class))).thenReturn(entidadeExistente);

        SetorDTO dtoAtualizado = SetorDTO.builder()
                .id(setorId)
                .nome("Nome Novo")
                .descricao("Descrição nova")
                .centroCustoId(centroCustoId)
                .build();

        SetorDTO resultado = service.save(dtoAtualizado);

        assertNotNull(resultado);
        verify(repository, times(1)).findById(setorId);
        verify(repository, times(1)).save(any(Setor.class));
        verify(centroCustoRepository, times(1)).findById(centroCustoId);
    }

    @Test
    @DisplayName("Deve realizar soft delete do setor")
    void deveRealizarSoftDeleteDoSetor() {
        when(repository.findById(setorId)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(Setor.class))).thenReturn(entidadeValida);

        UUID resultado = service.delete(setorId);

        assertEquals(setorId, resultado);
        verify(repository, times(1)).findById(setorId);
        verify(repository, times(1)).save(any(Setor.class));
        // Verifica que markAsDeleted foi chamado na entidade
        assertTrue(entidadeValida.getDeleted());
        assertNotNull(entidadeValida.getDeletedAt());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar editar setor excluído")
    void deveLancarExcecaoAoTentarEditarSetorExcluido() {
        Setor setorExcluido = new Setor.Builder()
                .nome("Setor Excluído")
                .descricao("Descrição")
                .centroCusto(centroCusto)
                .build();

        // Marcar como excluído
        setorExcluido.markAsDeleted("admin");

        // Usar reflexão para setar o ID na entidade
        try {
            java.lang.reflect.Field idField = Setor.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(setorExcluido, setorId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(repository.findById(setorId)).thenReturn(Optional.of(setorExcluido));

        SetorDTO dtoAtualizado = SetorDTO.builder()
                .id(setorId)
                .nome("Tentando Atualizar")
                .centroCustoId(centroCustoId)
                .build();

        assertThrows(DeletedEntityException.class, () -> service.save(dtoAtualizado));
        verify(repository, times(1)).findById(setorId);
        verify(repository, never()).save(any(Setor.class));
    }

    @Test
    @DisplayName("Deve incluir campo deleted no GridDTO")
    void deveIncluirCampoDeletedNoGridDTO() {
        // Entidade não excluída
        SetorGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);
        assertNotNull(gridDTO);
        assertFalse(gridDTO.getDeleted());

        // Entidade excluída
        entidadeValida.markAsDeleted("admin");
        SetorGridDTO gridDTOExcluido = service.buildGridDTOFromEntity(entidadeValida);
        assertNotNull(gridDTOExcluido);
        assertTrue(gridDTOExcluido.getDeleted());
    }
}
