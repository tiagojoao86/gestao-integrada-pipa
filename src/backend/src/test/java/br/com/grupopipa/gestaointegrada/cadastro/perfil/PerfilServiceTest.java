package br.com.grupopipa.gestaointegrada.cadastro.perfil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import br.com.grupopipa.gestaointegrada.cadastro.modulo.ModuloRepository;
import br.com.grupopipa.gestaointegrada.cadastro.modulo.entity.ModuloEntity;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilEntity;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.DeletedEntityException;

/**
 * Testes unitários para PerfilService. Usa Mockito para simular dependências
 * (repository).
 */
@DisplayName("PerfilService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    private PerfilRepository repository;

    @Mock
    private Specifications<PerfilEntity> specifications;

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private PerfilModuloRepository perfilModuloRepository;

    @InjectMocks
    private PerfilServiceImpl service;

    private PerfilDTO dtoValido;
    private PerfilEntity entidadeValida;
    private ModuloEntity modulo;
    private UUID moduloId;

    @BeforeEach
    void setup() {
        moduloId = UUID.randomUUID();

        modulo = mock(ModuloEntity.class);
        lenient().when(modulo.getId()).thenReturn(moduloId);
        lenient().when(modulo.getNome()).thenReturn("Cadastro de Pessoas");

        PerfilModuloDTO permissao = new PerfilModuloDTO();
        permissao.setModuloId(moduloId);
        permissao.setPodeListar(true);
        permissao.setPodeVisualizar(true);
        permissao.setPodeEditar(true);
        permissao.setPodeDeletar(false);

        dtoValido = PerfilDTO.builder().nome("Administrador").permissoes(List.of(permissao)).build();

        entidadeValida = new PerfilEntity.Builder().nome("Administrador").build();
    }

    @Test
    @DisplayName("Deve criar novo perfil com permissões")
    void deveCriarNovoPerfilComPermissoes() {
        // Given
        when(moduloRepository.findById(moduloId)).thenReturn(Optional.of(modulo));
        when(repository.save(any(PerfilEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PerfilDTO resultado = service.save(dtoValido);

        // Then
        assertNotNull(resultado);
        assertEquals("Administrador", resultado.getNome());
        assertNotNull(resultado.getPermissoes());
        assertEquals(1, resultado.getPermissoes().size());

        verify(repository, times(1)).save(any(PerfilEntity.class));
        verify(moduloRepository, times(1)).findById(moduloId);
    }

    @Test
    @DisplayName("Deve atualizar perfil existente")
    void deveAtualizarPerfilExistente() {
        // Given
        UUID id = UUID.randomUUID();
        dtoValido.setId(id);
        dtoValido.setNome("Administrador Atualizado");

        when(repository.findById(id)).thenReturn(Optional.of(entidadeValida));
        when(moduloRepository.findById(moduloId)).thenReturn(Optional.of(modulo));
        when(repository.save(any(PerfilEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PerfilDTO resultado = service.save(dtoValido);

        // Then
        assertNotNull(resultado);
        assertEquals("Administrador Atualizado", resultado.getNome());

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(PerfilEntity.class));
    }

    @Test
    @DisplayName("Deve buscar perfil por ID")
    void deveBuscarPerfilPorId() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entidadeValida));

        // When
        PerfilDTO resultado = service.findById(id);

        // Then
        assertNotNull(resultado);
        assertEquals("Administrador", resultado.getNome());

        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve deletar perfil (deprecated - usar soft delete)")
    void deveDeletarPerfil() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(PerfilEntity.class))).thenReturn(entidadeValida);

        // When
        UUID resultadoId = service.delete(id);

        // Then
        assertEquals(id, resultadoId);
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(PerfilEntity.class));
    }

    @Test
    @DisplayName("Deve construir DTO corretamente da entidade")
    void deveConstruirDTOCorretamenteDaEntidade() {
        // When
        PerfilDTO dto = service.buildDTOFromEntity(entidadeValida);

        // Then
        assertNotNull(dto);
        assertEquals("Administrador", dto.getNome());
        assertNotNull(dto.getPermissoes());
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente da entidade")
    void deveConstruirGridDTOCorretamenteDaEntidade() {
        // When
        PerfilGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);

        // Then
        assertNotNull(gridDTO);
        assertEquals("Administrador", gridDTO.getNome());
    }

    @Test
    @DisplayName("Deve criar perfil sem permissões")
    void deveCriarPerfilSemPermissoes() {
        // Given
        PerfilDTO dtoSemPermissoes = PerfilDTO.builder().nome("Perfil Básico").permissoes(null).build();

        when(repository.save(any(PerfilEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PerfilDTO resultado = service.save(dtoSemPermissoes);

        // Then
        assertNotNull(resultado);
        assertEquals("Perfil Básico", resultado.getNome());

        verify(repository, times(1)).save(any(PerfilEntity.class));
        verify(moduloRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve fazer merge de entidade nova com DTO")
    void deveFazerMergeDeEntidadeNovaComDTO() {
        // Given
        when(moduloRepository.findById(moduloId)).thenReturn(Optional.of(modulo));

        // When
        PerfilEntity resultado = service.mergeEntityAndDTO(null, dtoValido);

        // Then
        assertNotNull(resultado);
        assertEquals("Administrador", resultado.getNome());
        assertEquals(1, resultado.getPermissoes().size());
    }

    @Test
    @DisplayName("Deve realizar soft delete do perfil")
    void deveRealizarSoftDeleteDoPerfil() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(PerfilEntity.class))).thenReturn(entidadeValida);

        // When
        UUID resultado = service.delete(id);

        // Then
        assertEquals(id, resultado);
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(PerfilEntity.class));
        // Verifica que markAsDeleted foi chamado na entidade
        assertTrue(entidadeValida.getDeleted());
        assertNotNull(entidadeValida.getDeletedAt());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar editar perfil excluído")
    void deveLancarExcecaoAoTentarEditarPerfilExcluido() {
        // Given
        UUID id = UUID.randomUUID();
        PerfilEntity perfilExcluido = new PerfilEntity.Builder().nome("Perfil Excluído").build();

        // Marcar como excluído
        perfilExcluido.markAsDeleted("admin");

        // Usar reflexão para setar o ID na entidade
        try {
            java.lang.reflect.Field idField = PerfilEntity.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(perfilExcluido, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(repository.findById(id)).thenReturn(Optional.of(perfilExcluido));

        PerfilDTO dtoAtualizado = PerfilDTO.builder().id(id).nome("Tentando Atualizar").build();

        // When/Then
        assertThrows(DeletedEntityException.class, () -> service.save(dtoAtualizado));
        verify(repository, times(1)).findById(id);
        verify(repository, never()).save(any(PerfilEntity.class));
    }

    @Test
    @DisplayName("Deve incluir campo deleted no GridDTO")
    void deveIncluirCampoDeletedNoGridDTO() {
        // Entidade não excluída
        PerfilGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);
        assertNotNull(gridDTO);
        assertFalse(gridDTO.getDeleted());

        // Entidade excluída
        entidadeValida.markAsDeleted("admin");
        PerfilGridDTO gridDTOExcluido = service.buildGridDTOFromEntity(entidadeValida);
        assertNotNull(gridDTOExcluido);
        assertTrue(gridDTOExcluido.getDeleted());
    }
}
