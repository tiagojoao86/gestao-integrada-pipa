package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.cadastro.modulo.entity.ModuloEntity;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilDTO;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilRepository;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilEntity;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilModuloEntity;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.UsuarioPerfilEntity;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioDTO;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioService;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.cadastro.usuario.entity.UsuarioEntity;
import br.com.grupopipa.gestaointegrada.config.security.dto.AuthorityDTO;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
public class UsuarioServiceImpl
    extends CrudServiceImpl<UsuarioDTO, UsuarioGridDTO, UsuarioEntity, UsuarioRepository>
    implements UsuarioService {

  private PasswordEncoder passwordEncoder;
  private final PerfilRepository perfilRepository;
  private final UnidadeNegocioRepository unidadeNegocioRepository;
  private final UnidadeNegocioService unidadeNegocioService;

  public UsuarioServiceImpl(
      PasswordEncoder passwordEncoder,
      UsuarioRepository repository,
      Specifications<UsuarioEntity> specifications,
      PerfilRepository perfilRepository,
      UnidadeNegocioRepository unidadeNegocioRepository,
      UnidadeNegocioService unidadeNegocioService) {
    super(repository, specifications);
    this.passwordEncoder = passwordEncoder;
    this.perfilRepository = perfilRepository;
    this.unidadeNegocioRepository = unidadeNegocioRepository;
    this.unidadeNegocioService = unidadeNegocioService;
  }

  @Override
  public UsuarioDTO findUsuarioDTOByLogin(String login) {
    UsuarioEntity entity =
        this.repository
            .findUsuarioByLoginValue(login)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(getEntityClass().getSimpleName(), "login", login));

    return buildDTOFromEntity(entity);
  }

  @Override
  public List<AuthorityDTO> findAuthoritiesByLogin(String login) {
    UsuarioEntity usuario =
        this.repository
            .findUsuarioByLoginValue(login)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(getEntityClass().getSimpleName(), "login", login));

    Map<String, List<String>> permissoesPorModulo = new HashMap<>();
    Map<String, ModuloEntity> modulos = new HashMap<>();

    for (PerfilEntity perfil : usuario.getPerfis().stream().map(up -> up.getPerfil()).toList()) {
      for (PerfilModuloEntity permissao : perfil.getPermissoes()) {
        String moduloChave = permissao.getModulo().getChave();
        modulos.putIfAbsent(moduloChave, permissao.getModulo());

        List<String> listaPermissoes =
            permissoesPorModulo.computeIfAbsent(moduloChave, k -> new ArrayList<>());

        if (permissao.isPodeListar() && !listaPermissoes.contains("LISTAR")) {
          listaPermissoes.add("LISTAR");
        }
        if (permissao.isPodeVisualizar() && !listaPermissoes.contains("VISUALIZAR")) {
          listaPermissoes.add("VISUALIZAR");
        }
        if (permissao.isPodeEditar() && !listaPermissoes.contains("EDITAR")) {
          listaPermissoes.add("EDITAR");
        }
        if (permissao.isPodeDeletar() && !listaPermissoes.contains("DELETAR")) {
          listaPermissoes.add("DELETAR");
        }
        if (permissao.isPodeAuditar() && !listaPermissoes.contains("AUDITAR")) {
          listaPermissoes.add("AUDITAR");
        }
      }
    }

    List<AuthorityDTO> authorities = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : permissoesPorModulo.entrySet()) {
      ModuloEntity modulo = modulos.get(entry.getKey());
      authorities.add(
          new AuthorityDTO(
              modulo.getChave(), modulo.getNome(), modulo.getGrupo().name(), entry.getValue()));
    }

    return authorities;
  }

  @Override
  protected UsuarioEntity mergeEntityAndDTO(UsuarioEntity entity, UsuarioDTO dto) {
    if (Objects.isNull(entity)) {
      entity =
          new UsuarioEntity.Builder()
              .nome(dto.getNome())
              .login(dto.getLogin())
              .senha(dto.getSenha())
              .build(this.passwordEncoder);

      // Add perfis for new user
      if (dto.getPerfis() != null) {
        for (var perfilDTO : dto.getPerfis()) {
          if (perfilDTO == null || perfilDTO.getId() == null) continue;
          PerfilEntity perfilEntity =
              perfilRepository
                  .findById(perfilDTO.getId())
                  .orElseThrow(
                      () ->
                          new EntityNotFoundException(
                              "Perfil", "id", perfilDTO.getId().toString()));
          UsuarioPerfilEntity up = new UsuarioPerfilEntity(entity, perfilEntity);
          entity.getPerfis().add(up);
        }
      }

      // Add unidades de negócio for new user
      if (dto.getUnidadesNegocio() != null && !dto.getUnidadesNegocio().isEmpty()) {
        for (UsuarioUnidadeNegocioDTO unDTO : dto.getUnidadesNegocio()) {
          UnidadeNegocio unidade =
              unidadeNegocioRepository
                  .findById(unDTO.getUnidadeNegocioId())
                  .orElseThrow(
                      () ->
                          new EntityNotFoundException(
                              "UnidadeNegocio", "id", unDTO.getUnidadeNegocioId().toString()));
          entity.addUnidadeNegocio(unidade, unDTO.getIsDefault());
        }
      }

      return entity;
    }

    entity.updateUsuarioFromDTO(dto, passwordEncoder);

    // Sync perfis if provided in DTO
    if (dto.getPerfis() != null) {
      var perfilIds = dto.getPerfis().stream().map(p -> p.getId()).toList();

      // remove perfis not present
      entity
          .getPerfis()
          .removeIf(up -> up.getPerfil() == null || !perfilIds.contains(up.getPerfil().getId()));

      // add new perfis
      var existingPerfilIds =
          entity.getPerfis().stream().map(up -> up.getPerfil().getId()).toList();
      for (var perfilDTO : dto.getPerfis()) {
        if (perfilDTO == null || perfilDTO.getId() == null) continue;
        if (!existingPerfilIds.contains(perfilDTO.getId())) {
          PerfilEntity perfilEntity =
              perfilRepository
                  .findById(perfilDTO.getId())
                  .orElseThrow(
                      () ->
                          new EntityNotFoundException(
                              "Perfil", "id", perfilDTO.getId().toString()));
          UsuarioPerfilEntity up = new UsuarioPerfilEntity(entity, perfilEntity);
          entity.getPerfis().add(up);
        }
      }
    }

    // Sync unidades de negócio if provided in DTO
    if (dto.getUnidadesNegocio() != null) {
      // Get current unidade IDs
      Set<UUID> currentIds =
          entity.getUnidadesNegocio().stream()
              .map(uun -> uun.getUnidadeNegocio().getId())
              .collect(Collectors.toSet());

      Set<UUID> newIds =
          dto.getUnidadesNegocio().stream()
              .map(UsuarioUnidadeNegocioDTO::getUnidadeNegocioId)
              .collect(Collectors.toSet());

      // Remove unidades not present in DTO
      entity
          .getUnidadesNegocio()
          .removeIf(uun -> !newIds.contains(uun.getUnidadeNegocio().getId()));

      // Add new unidades or update isDefault flag
      for (UsuarioUnidadeNegocioDTO unDTO : dto.getUnidadesNegocio()) {
        if (!currentIds.contains(unDTO.getUnidadeNegocioId())) {
          // New association
          UnidadeNegocio unidade =
              unidadeNegocioRepository
                  .findById(unDTO.getUnidadeNegocioId())
                  .orElseThrow(
                      () ->
                          new EntityNotFoundException(
                              "UnidadeNegocio", "id", unDTO.getUnidadeNegocioId().toString()));
          entity.addUnidadeNegocio(unidade, unDTO.getIsDefault());
        } else {
          // Update existing isDefault flag
          entity.getUnidadesNegocio().stream()
              .filter(uun -> uun.getUnidadeNegocio().getId().equals(unDTO.getUnidadeNegocioId()))
              .findFirst()
              .ifPresent(uun -> uun.setIsDefault(unDTO.getIsDefault()));
        }
      }
    }

    return entity;
  }

  @Override
  protected UsuarioDTO buildDTOFromEntity(UsuarioEntity entity) {
    var perfis =
        entity.getPerfis().stream()
            .map(up -> up.getPerfil())
            .filter(p -> p != null)
            .map(
                p ->
                    PerfilDTO.builder()
                        .id(p.getId())
                        .nome(p.getNome())
                        .createdAt(p.getCreatedAt())
                        .createdBy(p.getCreatedBy())
                        .updatedAt(p.getUpdatedAt())
                        .updatedBy(p.getUpdatedBy())
                        .build())
            .toList();

    List<UsuarioUnidadeNegocioDTO> unidadesNegocio =
        entity.getUnidadesNegocio().stream()
            .map(
                uun ->
                    UsuarioUnidadeNegocioDTO.builder()
                        .unidadeNegocioId(uun.getUnidadeNegocio().getId())
                        .unidadeNegocioCodigo(uun.getUnidadeNegocio().getCodigo())
                        .unidadeNegocioNome(uun.getUnidadeNegocio().getNome())
                        .isDefault(uun.getIsDefault())
                        .build())
            .toList();

    return UsuarioDTO.builder()
        .id(entity.getId())
        .nome(entity.getNome())
        .login(entity.getLogin())
        .updatedAt(entity.getUpdatedAt())
        .updatedBy(entity.getUpdatedBy())
        .createdAt(entity.getCreatedAt())
        .createdBy(entity.getCreatedBy())
        .perfis(perfis)
        .unidadesNegocio(unidadesNegocio)
        .build();
  }

  @Override
  protected UsuarioGridDTO buildGridDTOFromEntity(UsuarioEntity entity) {
    return UsuarioGridDTO.builder()
        .id(entity.getId())
        .login(entity.getLogin())
        .nome(entity.getNome())
        .createdAt(entity.getCreatedAt())
        .deleted(entity.getDeleted())
        .build();
  }

  @Override
  protected List<String> getPropertiesToFilter() {
    return List.of("login", "nome", "createdAt");
  }

  @Override
  protected Class<UsuarioEntity> getEntityClass() {
    return UsuarioEntity.class;
  }

  @Override
  public List<UnidadeNegocioDTO> listarUnidadesParaAssociacao() {
    return unidadeNegocioService.listarTodasDisponiveis();
  }
}
