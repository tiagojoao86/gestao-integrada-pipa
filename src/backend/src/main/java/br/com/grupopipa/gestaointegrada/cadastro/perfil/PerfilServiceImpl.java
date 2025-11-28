package br.com.grupopipa.gestaointegrada.cadastro.perfil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.cadastro.modulo.ModuloRepository;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilEntity;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilModuloEntity;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import jakarta.transaction.Transactional; // Adicionado para @Transactional

@Service
public class PerfilServiceImpl
        extends CrudServiceImpl<PerfilDTO, PerfilGridDTO, PerfilEntity, PerfilRepository>
        implements PerfilService {

    private final ModuloRepository moduloRepository;    

    public PerfilServiceImpl(PerfilRepository repository,
                             Specifications<PerfilEntity> specifications,
                             ModuloRepository moduloRepository,
                             PerfilModuloRepository perfilModuloRepository) { // <--- INJETAR NO CONSTRUTOR
        super(repository, specifications);
        this.moduloRepository = moduloRepository;        
    }

    @Override
    @Transactional  
    protected PerfilEntity mergeEntityAndDTO(PerfilEntity entity, PerfilDTO dto) {
        if (Objects.isNull(entity)) {
            PerfilEntity newEntity = new PerfilEntity.Builder()
                    .nome(dto.getNome())
                    .build();
            
            addPermissoesToEntity(newEntity, dto);
            return newEntity;
        }

        entity.updatePerfilFromDTO(dto);

        if (entity.getId() != null) {
            entity.getPermissoes().clear();
            entity = this.repository.save(entity);            
        }
        
        addPermissoesToEntity(entity, dto);
        return entity;
    }
    
    private void addPermissoesToEntity(PerfilEntity entity, PerfilDTO dto) {
        if (Objects.nonNull(dto.getPermissoes())) {
            for (PerfilModuloDTO permissaoDTO : dto.getPermissoes()) {
                PerfilModuloEntity permissaoEntity = buildPerfilModuloEntity(entity, permissaoDTO);
                entity.addPermissao(permissaoEntity);
            }
        }
    }

    @Override
    protected PerfilDTO buildDTOFromEntity(PerfilEntity entity) {
        List<PerfilModuloDTO> permissoesDTO = new ArrayList<>();

        for (PerfilModuloEntity pme : entity.getPermissoes()) {
            PerfilModuloDTO dto = new PerfilModuloDTO();
            dto.setId(pme.getId());
            dto.setPerfilId(entity.getId());
            dto.setModuloId(pme.getModulo().getId());
            dto.setModuloNome(pme.getModulo().getNome());
            dto.setPodeListar(pme.isPodeListar());
            dto.setPodeVisualizar(pme.isPodeVisualizar());
            dto.setPodeEditar(pme.isPodeEditar());
            dto.setPodeDeletar(pme.isPodeDeletar());
            permissoesDTO.add(dto);
        }

        return PerfilDTO
                .builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .permissoes(permissoesDTO)
                .build();
    }

    @Override
    protected PerfilGridDTO buildGridDTOFromEntity(PerfilEntity entity) {
        return PerfilGridDTO
                .builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "createdAt");
    }

    @Override
    protected Class<PerfilEntity> getEntityClass() {
        return PerfilEntity.class;
    }

    private PerfilModuloEntity buildPerfilModuloEntity(PerfilEntity perfil, PerfilModuloDTO dto) {
        var modulo = moduloRepository.findById(dto.getModuloId())
                .orElseThrow(() -> new EntityNotFoundException("Modulo", "id", dto.getModuloId().toString()));

        return new PerfilModuloEntity.Builder()
                .perfil(perfil)
                .modulo(modulo)
                .podeListar(dto.isPodeListar())
                .podeVisualizar(dto.isPodeVisualizar())
                .podeEditar(dto.isPodeEditar())
                .podeDeletar(dto.isPodeDeletar())
                .build();
    }
}
