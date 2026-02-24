package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilDTO;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilRepository;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilEntity;
import br.com.grupopipa.gestaointegrada.cadastro.usuario.entity.UsuarioEntity;
import br.com.grupopipa.gestaointegrada.config.security.dto.AuthorityDTO;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;

/**
 * Testes unitários para UsuarioService. Usa Mockito para simular dependências
 * (repository, password
 * encoder).
 */
@DisplayName("UsuarioService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private Specifications<UsuarioEntity> specifications;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PerfilRepository perfilRepository;

    @InjectMocks
    private UsuarioServiceImpl service;

    private UsuarioDTO dtoValido;
    private UsuarioEntity entidadeValida;
    private PerfilDTO perfilDTO;
    private PerfilEntity perfilEntity;

    @BeforeEach
    void setup() {
        UUID perfilId = UUID.randomUUID();

        perfilDTO = PerfilDTO.builder().id(perfilId).nome("Administrador").build();

        perfilEntity = new PerfilEntity.Builder().nome("Administrador").build();

        dtoValido = UsuarioDTO.builder()
                .nome("João Silva")
                .login("joao.silva")
                .senha("senha123")
                .perfis(List.of(perfilDTO))
                .build();

        entidadeValida = new UsuarioEntity.Builder()
                .nome("João Silva")
                .login("joao.silva")
                .senha("senha123")
                .build(passwordEncoder);

        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
    }

    @Test
    @DisplayName("Deve criar novo usuário com perfis")
    void deveCriarNovoUsuarioComPerfis() {
        // Given
        when(perfilRepository.findById(any(UUID.class))).thenReturn(Optional.of(perfilEntity));
        when(repository.save(any(UsuarioEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UsuarioDTO resultado = service.save(dtoValido);

        // Then
        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals("joao.silva", resultado.getLogin());

        verify(repository, times(1)).save(any(UsuarioEntity.class));
        verify(perfilRepository, times(1)).findById(any(UUID.class));
        // passwordEncoder.encode() é chamado 2 vezes: uma no Builder e outra no
        // construtor de Senha
        verify(passwordEncoder, times(2)).encode("senha123");
    }

    @Test
    @DisplayName("Deve atualizar usuário existente")
    void deveAtualizarUsuarioExistente() {
        // Given
        UUID id = UUID.randomUUID();
        dtoValido.setId(id);
        dtoValido.setNome("João Silva Atualizado");

        when(repository.findById(id)).thenReturn(Optional.of(entidadeValida));
        when(perfilRepository.findById(any(UUID.class))).thenReturn(Optional.of(perfilEntity));
        when(repository.save(any(UsuarioEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UsuarioDTO resultado = service.save(dtoValido);

        // Then
        assertNotNull(resultado);
        assertEquals("João Silva Atualizado", resultado.getNome());

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void deveBuscarUsuarioPorId() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entidadeValida));

        // When
        UsuarioDTO resultado = service.findById(id);

        // Then
        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals("joao.silva", resultado.getLogin());

        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve buscar usuário por login")
    void deveBuscarUsuarioPorLogin() {
        // Given
        when(repository.findUsuarioByLoginValue("joao.silva")).thenReturn(Optional.of(entidadeValida));

        // When
        UsuarioDTO resultado = service.findUsuarioDTOByLogin("joao.silva");

        // Then
        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals("joao.silva", resultado.getLogin());

        verify(repository, times(1)).findUsuarioByLoginValue("joao.silva");
    }

    @Test
    @DisplayName("Deve buscar authorities por login")
    void deveBuscarAuthoritiesPorLogin() {
        // Given
        when(repository.findUsuarioByLoginValue("joao.silva")).thenReturn(Optional.of(entidadeValida));

        // When
        List<AuthorityDTO> authorities = service.findAuthoritiesByLogin("joao.silva");

        // Then
        assertNotNull(authorities);

        verify(repository, times(1)).findUsuarioByLoginValue("joao.silva");
    }

    @Test
    @DisplayName("Deve deletar usuário")
    void deveDeletarUsuario() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(UsuarioEntity.class))).thenReturn(entidadeValida);

        // When
        UUID resultadoId = service.delete(id);

        // Then
        assertEquals(id, resultadoId);
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("Deve construir DTO corretamente da entidade")
    void deveConstruirDTOCorretamenteDaEntidade() {
        // When
        UsuarioDTO dto = service.buildDTOFromEntity(entidadeValida);

        // Then
        assertNotNull(dto);
        assertEquals("João Silva", dto.getNome());
        assertEquals("joao.silva", dto.getLogin());
        assertNotNull(dto.getPerfis());
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente da entidade")
    void deveConstruirGridDTOCorretamenteDaEntidade() {
        // When
        UsuarioGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);

        // Then
        assertNotNull(gridDTO);
        assertEquals("João Silva", gridDTO.getNome());
        assertEquals("joao.silva", gridDTO.getLogin());
    }

    @Test
    @DisplayName("Deve criar usuário sem perfis")
    void deveCriarUsuarioSemPerfis() {
        // Given
        UsuarioDTO dtoSemPerfis = UsuarioDTO.builder()
                .nome("Maria Santos")
                .login("maria.santos")
                .senha("senha456")
                .perfis(null)
                .build();

        when(repository.save(any(UsuarioEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UsuarioDTO resultado = service.save(dtoSemPerfis);

        // Then
        assertNotNull(resultado);
        assertEquals("Maria Santos", resultado.getNome());

        verify(repository, times(1)).save(any(UsuarioEntity.class));
        verify(perfilRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve fazer merge de entidade nova com DTO")
    void deveFazerMergeDeEntidadeNovaComDTO() {
        // Given
        when(perfilRepository.findById(any(UUID.class))).thenReturn(Optional.of(perfilEntity));

        // When
        UsuarioEntity resultado = service.mergeEntityAndDTO(null, dtoValido);

        // Then
        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals("joao.silva", resultado.getLogin());
        assertFalse(resultado.getPerfis().isEmpty());
    }
}
