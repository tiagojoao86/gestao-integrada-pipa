package br.com.grupopipa.gestaointegrada.atendimento.procedimento;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.dto.ProcedimentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.dto.ProcedimentoGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaRepository;

@Service
public class ProcedimentoServiceImpl
        extends CrudServiceImpl<ProcedimentoDTO, ProcedimentoGridDTO, Procedimento, ProcedimentoRepository>
        implements ProcedimentoService {

    private final TituloCategoriaRepository tituloCategoriaRepository;

    public ProcedimentoServiceImpl(
            ProcedimentoRepository repository,
            Specifications<Procedimento> specifications,
            TituloCategoriaRepository tituloCategoriaRepository) {
        super(repository, specifications);
        this.tituloCategoriaRepository = tituloCategoriaRepository;
    }

    @Override
    protected Procedimento mergeEntityAndDTO(Procedimento entity, ProcedimentoDTO dto) {
        if (Objects.isNull(entity)) {
            return criarProcedimento(dto);
        }
        return atualizarProcedimento(entity, dto);
    }

    private Procedimento criarProcedimento(ProcedimentoDTO dto) {
        return new Procedimento.Builder()
                .codigo(dto.getCodigo())
                .codigoTiss(dto.getCodigoTiss())
                .codigoTuss(dto.getCodigoTuss())
                .descricao(dto.getDescricao())
                .ativo(dto.getAtivo())
                .tituloCategoriaId(dto.getTituloCategoriaId())
                .build();
    }

    private Procedimento atualizarProcedimento(Procedimento entity, ProcedimentoDTO dto) {
        entity.atualizar(
            dto.getCodigo(), dto.getCodigoTiss(), dto.getCodigoTuss(),
            dto.getDescricao(), dto.getAtivo(), dto.getTituloCategoriaId());
        return entity;
    }

    private String resolverNomeCategoria(UUID tituloCategoriaId) {
        if (tituloCategoriaId == null) return null;
        return tituloCategoriaRepository.findById(tituloCategoriaId)
            .map(c -> c.getNome().getValue())
            .orElse(null);
    }

    @Override
    protected ProcedimentoDTO buildDTOFromEntity(Procedimento entity) {
        return ProcedimentoDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .codigoTiss(entity.getCodigoTiss())
                .codigoTuss(entity.getCodigoTuss())
                .descricao(entity.getDescricao())
                .ativo(entity.getAtivo())
                .tituloCategoriaId(entity.getTituloCategoriaId())
                .tituloCategoriaNome(resolverNomeCategoria(entity.getTituloCategoriaId()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected ProcedimentoGridDTO buildGridDTOFromEntity(Procedimento entity) {
        return ProcedimentoGridDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .codigoTiss(entity.getCodigoTiss())
                .codigoTuss(entity.getCodigoTuss())
                .descricao(entity.getDescricao())
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("codigo", "codigoTiss", "codigoTuss", "descricao", "ativo", "createdAt");
    }

    @Override
    protected Class<Procedimento> getEntityClass() {
        return Procedimento.class;
    }
}
