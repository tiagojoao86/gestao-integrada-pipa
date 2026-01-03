package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioDTO;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioService;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;

@Service
public class PlanoContasServiceImpl
        extends CrudServiceImpl<PlanoContasDTO, PlanoContasGridDTO, PlanoContas, PlanoContasRepository>
        implements PlanoContasService {

    private final UnidadeNegocioRepository unidadeNegocioRepository;
    private final UnidadeNegocioService unidadeNegocioService;

    public PlanoContasServiceImpl(
            PlanoContasRepository repository,
            Specifications<PlanoContas> specifications,
            UnidadeNegocioRepository unidadeNegocioRepository,
            UnidadeNegocioService unidadeNegocioService) {
        super(repository, specifications);
        this.unidadeNegocioRepository = unidadeNegocioRepository;
        this.unidadeNegocioService = unidadeNegocioService;
    }

    @Override
    protected PlanoContas mergeEntityAndDTO(PlanoContas entity, PlanoContasDTO dto) {
        if (Objects.isNull(entity)) {
            TipoPlanoContas tipo = TipoPlanoContas.valueOf(dto.getTipo());
            PlanoContas planoPai = null;

            if (dto.getPlanoPaiId() != null) {
                planoPai = repository
                        .findById(dto.getPlanoPaiId())
                        .orElseThrow(() -> new IllegalArgumentException("Plano pai não encontrado"));
            }

            UnidadeNegocio unidadeNegocio = unidadeNegocioRepository
                    .findById(dto.getUnidadeNegocioId())
                    .orElseThrow(() -> new IllegalArgumentException("Unidade de negócio não encontrada"));

            entity = new PlanoContas.Builder()
                    .codigo(dto.getCodigo())
                    .descricao(dto.getDescricao())
                    .tipo(tipo)
                    .planoPai(planoPai)
                    .unidadeNegocio(unidadeNegocio)
                    .build();

            return entity;
        }

        entity.atualizar(dto.getDescricao());

        // Atualizar unidade de negócio se fornecida
        if (dto.getUnidadeNegocioId() != null) {
            UnidadeNegocio unidadeNegocio = unidadeNegocioRepository
                    .findById(dto.getUnidadeNegocioId())
                    .orElseThrow(() -> new IllegalArgumentException("Unidade de negócio não encontrada"));
            entity.atualizarUnidadeNegocio(unidadeNegocio);
        }

        if (dto.getAtivo() != null) {
            if (dto.getAtivo()) {
                entity.ativar();
            } else {
                entity.inativar();
            }
        }

        return entity;
    }

    @Override
    protected PlanoContasDTO buildDTOFromEntity(PlanoContas entity) {
        boolean analitico = repository.isAnalitico(entity.getId());

        return PlanoContasDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .descricao(entity.getDescricao())
                .tipo(entity.getTipo().name())
                .planoPaiId(entity.getPlanoPai() != null ? entity.getPlanoPai().getId() : null)
                .planoPaiDescricao(
                        entity.getPlanoPai() != null ? entity.getPlanoPai().getDescricao() : null)
                .unidadeNegocioId(entity.getUnidadeNegocio().getId())
                .unidadeNegocioNome(entity.getUnidadeNegocio().getNome())
                .ativo(entity.getAtivo())
                .analitico(analitico)
                .nivel(entity.getNivel())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected PlanoContasGridDTO buildGridDTOFromEntity(PlanoContas entity) {
        boolean analitico = repository.isAnalitico(entity.getId());

        return PlanoContasGridDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .descricao(entity.getDescricao())
                .tipo(entity.getTipo().name())
                .planoPaiCodigo(entity.getPlanoPai() != null ? entity.getPlanoPai().getCodigo() : null)
                .planoPaiDescricao(
                        entity.getPlanoPai() != null ? entity.getPlanoPai().getDescricao() : null)
                .unidadeNegocioCodigo(entity.getUnidadeNegocio().getCodigo())
                .ativo(entity.getAtivo())
                .analitico(analitico)
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("codigo", "descricao", "tipo", "ativo");
    }

    @Override
    protected Class<PlanoContas> getEntityClass() {
        return PlanoContas.class;
    }

    @Override
    public List<UnidadeNegocioDTO> listarUnidadesDisponiveis() {
        return unidadeNegocioService.listarDisponiveisParaUsuario();
    }

    @Override
    public List<PlanoContasDTO> listarPlanosParaVinculo(java.util.UUID unidadeNegocioId) {
        return repository.findByAtivoTrueAndUnidadeNegocioId(unidadeNegocioId).stream()
                .map(
                        plano -> PlanoContasDTO.builder()
                                .id(plano.getId())
                                .codigo(plano.getCodigo())
                                .descricao(plano.getDescricao())
                                .tipo(plano.getTipo().name())
                                .build())
                .toList();
    }
}
