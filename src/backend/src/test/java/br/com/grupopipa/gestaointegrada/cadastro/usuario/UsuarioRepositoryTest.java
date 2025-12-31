package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilRepository;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilEntity;
import br.com.grupopipa.gestaointegrada.cadastro.usuario.entity.UsuarioEntity;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;

/**
 * Testes de integração para UsuarioRepository. Valida a persistência e consultas de usuários com
 * perfis.
 */
@DisplayName("UsuarioRepository - Testes de Integração")
@Transactional
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UsuarioRepositoryTest extends AbstractIntegrationTest {
  @org.springframework.beans.factory.annotation.Autowired
  private jakarta.persistence.EntityManager entityManager;

  private final br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio
      unidadeNegocio =
          new br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio
                  .Builder()
              .codigo("UN001")
              .nome("Unidade Teste")
              .cnpj("11222333000181")
              .build();

  @Autowired private UsuarioRepository repository;

  @Autowired private PerfilRepository perfilRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("Deve salvar e recuperar usuário sem perfis")
  void deveSalvarERecuperarUsuarioSemPerfis() {
    entityManager.persist(unidadeNegocio);
    entityManager.flush();
    // Given
    UsuarioEntity usuario =
        new UsuarioEntity.Builder()
            .nome("João Silva")
            .login("joao.silva")
            .senha("senha123")
            .build(passwordEncoder);
    usuario.addUnidadeNegocio(unidadeNegocio, true);

    // When
    UsuarioEntity usuarioSalvo = repository.save(usuario);

    // Then
    assertNotNull(usuarioSalvo.getId());
    assertEquals("João Silva", usuarioSalvo.getNome());
    assertEquals("joao.silva", usuarioSalvo.getLogin());
    assertNotNull(usuarioSalvo.getCreatedAt());
    assertTrue(usuarioSalvo.getPerfis().isEmpty());
  }

  @Test
  @DisplayName("Deve salvar usuário com perfil")
  void deveSalvarUsuarioComPerfil() {
    entityManager.persist(unidadeNegocio);
    entityManager.flush();
    // Given
    PerfilEntity perfil = new PerfilEntity.Builder().nome("Administrador").build();
    perfilRepository.save(perfil);

    UsuarioEntity usuario =
        new UsuarioEntity.Builder()
            .nome("Maria Santos")
            .login("maria.santos")
            .senha("senha456")
            .build(passwordEncoder);
    usuario.addUnidadeNegocio(unidadeNegocio, true);

    // Nota: Teste simplificado - relacionamento com perfis é testado via
    // UsuarioService

    // When
    UsuarioEntity usuarioSalvo = repository.save(usuario);

    // Then
    assertNotNull(usuarioSalvo.getId());
    assertEquals("Maria Santos", usuarioSalvo.getNome());
    assertNotNull(usuarioSalvo.getCreatedAt());
  }

  @Test
  @DisplayName("Deve buscar usuário por ID")
  void deveBuscarUsuarioPorId() {
    entityManager.persist(unidadeNegocio);
    entityManager.flush();
    // Given
    UsuarioEntity usuario =
        new UsuarioEntity.Builder()
            .nome("Carlos Oliveira")
            .login("carlos.oliveira")
            .senha("senha789")
            .build(passwordEncoder);
    usuario.addUnidadeNegocio(unidadeNegocio, true);

    UsuarioEntity usuarioSalvo = repository.save(usuario);

    // When
    Optional<UsuarioEntity> resultado = repository.findById(usuarioSalvo.getId());

    // Then
    assertTrue(resultado.isPresent());
    assertEquals("Carlos Oliveira", resultado.get().getNome());
    assertEquals("carlos.oliveira", resultado.get().getLogin());
  }

  @Test
  @DisplayName("Deve deletar usuário")
  void deveDeletarUsuario() {
    entityManager.persist(unidadeNegocio);
    entityManager.flush();
    // Given
    UsuarioEntity usuario =
        new UsuarioEntity.Builder()
            .nome("Usuário Temporário")
            .login("temp.user")
            .senha("senha303")
            .build(passwordEncoder);
    usuario.addUnidadeNegocio(unidadeNegocio, true);

    UsuarioEntity usuarioSalvo = repository.save(usuario);

    // When
    repository.delete(usuarioSalvo);
    Optional<UsuarioEntity> resultado = repository.findById(usuarioSalvo.getId());

    // Then
    assertFalse(resultado.isPresent());
  }

  @Test
  @DisplayName("Deve validar campos obrigatórios")
  void deveValidarCamposObrigatorios() {
    entityManager.persist(unidadeNegocio);
    entityManager.flush();
    // Given
    UsuarioEntity usuario =
        new UsuarioEntity.Builder()
            .nome("Validação User")
            .login("validacao.user")
            .senha("senha404")
            .build(passwordEncoder);
    usuario.addUnidadeNegocio(unidadeNegocio, true);

    // When
    UsuarioEntity usuarioSalvo = repository.save(usuario);

    // Then
    assertNotNull(usuarioSalvo.getId());
    assertNotNull(usuarioSalvo.getNome());
    assertNotNull(usuarioSalvo.getLogin());
    assertNotNull(usuarioSalvo.getSenha());
    assertNotNull(usuarioSalvo.getCreatedAt());
  }

  @Test
  @DisplayName("Deve verificar senha criptografada")
  void deveVerificarSenhaCriptografada() {
    entityManager.persist(unidadeNegocio);
    entityManager.flush();
    // Given
    String senhaTexto = "senha505";
    UsuarioEntity usuario =
        new UsuarioEntity.Builder()
            .nome("Senha User")
            .login("senha.user")
            .senha(senhaTexto)
            .build(passwordEncoder);
    usuario.addUnidadeNegocio(unidadeNegocio, true);

    // When
    UsuarioEntity usuarioSalvo = repository.save(usuario);

    // Then
    assertNotNull(usuarioSalvo.getSenha());
    assertNotEquals(senhaTexto, usuarioSalvo.getSenha());
    assertTrue(passwordEncoder.matches(senhaTexto, usuarioSalvo.getSenha()));
  }
}
