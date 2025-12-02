package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.cadastro.modulo.entity.ModuloEntity;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilDTO;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilRepository;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilEntity;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilModuloEntity;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.UsuarioPerfilEntity;
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

    public UsuarioServiceImpl(PasswordEncoder passwordEncoder, UsuarioRepository repository,
            Specifications<UsuarioEntity> specifications, PerfilRepository perfilRepository) {
        super(repository, specifications);
        this.passwordEncoder = passwordEncoder;
        this.perfilRepository = perfilRepository;
    }

    @Override
    public UsuarioDTO findUsuarioDTOByLogin(String login) {
        UsuarioEntity entity = this.repository.findUsuarioByLoginValue(login)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass().getSimpleName(), "login", login));

        return buildDTOFromEntity(entity);
    }

    @Override
    public List<AuthorityDTO> findAuthoritiesByLogin(String login) {
        UsuarioEntity usuario = this.repository.findUsuarioByLoginValue(login)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass().getSimpleName(), "login", login));

        Map<String, List<String>> permissoesPorModulo = new HashMap<>();
        Map<String, ModuloEntity> modulos = new HashMap<>();

        for (PerfilEntity perfil : usuario.getPerfis().stream().map(up -> up.getPerfil()).toList()) {
            for (PerfilModuloEntity permissao : perfil.getPermissoes()) {
                String moduloChave = permissao.getModulo().getChave();
                modulos.putIfAbsent(moduloChave, permissao.getModulo());

                List<String> listaPermissoes = permissoesPorModulo.computeIfAbsent(moduloChave, k -> new ArrayList<>());

                if (permissao.isPodeListar() && !listaPermissoes.contains("LISTAR")) listaPermissoes.add("LISTAR");
                if (permissao.isPodeVisualizar() && !listaPermissoes.contains("VISUALIZAR")) listaPermissoes.add("VISUALIZAR");
                if (permissao.isPodeEditar() && !listaPermissoes.contains("EDITAR")) listaPermissoes.add("EDITAR");
                if (permissao.isPodeDeletar() && !listaPermissoes.contains("DELETAR")) listaPermissoes.add("DELETAR");
            }
        }

        List<AuthorityDTO> authorities = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : permissoesPorModulo.entrySet()) {
            ModuloEntity modulo = modulos.get(entry.getKey());
            authorities.add(new AuthorityDTO(
                modulo.getChave(),
                modulo.getNome(),
                modulo.getGrupo().name(),
                entry.getValue()
            ));
        }

        return authorities;
    }

    @Override
    protected UsuarioEntity mergeEntityAndDTO(UsuarioEntity entity, UsuarioDTO dto) {
        if (Objects.isNull(entity)) {
            entity = new UsuarioEntity.Builder()
                    .nome(dto.getNome())
                    .login(dto.getLogin())
                    .senha(dto.getSenha())
                    .build(this.passwordEncoder);
            
            // Add perfis for new user
            if (dto.getPerfis() != null) {
                for (var perfilDTO : dto.getPerfis()) {
                    if (perfilDTO == null || perfilDTO.getId() == null) continue;
                    PerfilEntity perfilEntity = perfilRepository.findById(perfilDTO.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Perfil", "id", perfilDTO.getId().toString()));
                    UsuarioPerfilEntity up = new UsuarioPerfilEntity(entity, perfilEntity);
                    entity.getPerfis().add(up);
                }
            }
            
            return entity;
        }

        entity.updateUsuarioFromDTO(dto, passwordEncoder);

        // Sync perfis if provided in DTO
        if (dto.getPerfis() != null) {
            var perfilIds = dto.getPerfis().stream().map(p -> p.getId()).toList();

            // remove perfis not present
            entity.getPerfis().removeIf(up -> up.getPerfil() == null || !perfilIds.contains(up.getPerfil().getId()));

            // add new perfis
            var existingPerfilIds = entity.getPerfis().stream().map(up -> up.getPerfil().getId()).toList();
            for (var perfilDTO : dto.getPerfis()) {
                if (perfilDTO == null || perfilDTO.getId() == null) continue;
                if (!existingPerfilIds.contains(perfilDTO.getId())) {
                    PerfilEntity perfilEntity = perfilRepository.findById(perfilDTO.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Perfil", "id", perfilDTO.getId().toString()));
                    UsuarioPerfilEntity up = new UsuarioPerfilEntity(entity, perfilEntity);
                    entity.getPerfis().add(up);
                }
            }
        }

        return entity;
    }

    @Override
    protected UsuarioDTO buildDTOFromEntity(UsuarioEntity entity) {
        var perfis = entity.getPerfis().stream()
                .map(up -> up.getPerfil())
                .filter(p -> p != null)
                .map(p -> PerfilDTO
                        .builder()
                        .id(p.getId())
                        .nome(p.getNome())
                        .createdAt(p.getCreatedAt())
                        .createdBy(p.getCreatedBy())
                        .updatedAt(p.getUpdatedAt())
                        .updatedBy(p.getUpdatedBy())
                        .build())
                .toList();

        return UsuarioDTO
                .builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .login(entity.getLogin())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .perfis(perfis)
                .build();
    }

    @Override
    protected UsuarioGridDTO buildGridDTOFromEntity(UsuarioEntity entity) {
        return UsuarioGridDTO
                .builder()
                .id(entity.getId())
                .login(entity.getLogin())
                .nome(entity.getNome())
                .createdAt(entity.getCreatedAt())
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
}
