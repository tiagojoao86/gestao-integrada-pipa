package br.com.grupopipa.gestaointegrada.atendimento.profissional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.atendimento.profissional.dto.ProfissionalDTO;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.dto.ProfissionalGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
public class ProfissionalServiceImpl
        extends CrudServiceImpl<ProfissionalDTO, ProfissionalGridDTO, Profissional, ProfissionalRepository>
        implements ProfissionalService {

    private final PessoaRepository pessoaRepository;

    public ProfissionalServiceImpl(
            ProfissionalRepository repository,
            Specifications<Profissional> specifications,
            PessoaRepository pessoaRepository) {
        super(repository, specifications);
        this.pessoaRepository = pessoaRepository;
    }

    @Override
    protected Profissional mergeEntityAndDTO(Profissional entity, ProfissionalDTO dto) {
        Pessoa pessoa = resolverPessoa(dto.getPessoaId());
        TipoRemuneracao tipoRemuneracao = resolverTipoRemuneracao(dto.getTipoRemuneracao());

        if (Objects.isNull(entity)) {
            return new Profissional.Builder()
                    .pessoa(pessoa)
                    .conselho(dto.getConselho())
                    .codigoConselho(dto.getCodigoConselho())
                    .tipoRemuneracao(tipoRemuneracao)
                    .banco(dto.getBanco())
                    .conta(dto.getConta())
                    .chavePix(dto.getChavePix())
                    .ativo(dto.getAtivo())
                    .build();
        }

        entity.atualizar(
                pessoa,
                dto.getConselho(),
                dto.getCodigoConselho(),
                tipoRemuneracao,
                dto.getBanco(),
                dto.getConta(),
                dto.getChavePix(),
                dto.getAtivo());

        return entity;
    }

    private Pessoa resolverPessoa(UUID pessoaId) {
        if (pessoaId == null) {
            return null;
        }
        return pessoaRepository.findById(pessoaId).orElse(null);
    }

    private TipoRemuneracao resolverTipoRemuneracao(String tipoRemuneracao) {
        if (tipoRemuneracao == null) {
            return null;
        }
        return TipoRemuneracao.valueOf(tipoRemuneracao);
    }

    @Override
    protected ProfissionalDTO buildDTOFromEntity(Profissional entity) {
        return ProfissionalDTO.builder()
                .id(entity.getId())
                .pessoaId(entity.getPessoa() != null ? entity.getPessoa().getId() : null)
                .pessoaNome(entity.getPessoa() != null ? entity.getPessoa().getNome() : null)
                .conselho(entity.getConselho())
                .codigoConselho(entity.getCodigoConselho())
                .tipoRemuneracao(
                    entity.getTipoRemuneracao() != null ? entity.getTipoRemuneracao().name() : null
                )
                .banco(entity.getBanco())
                .conta(entity.getConta())
                .chavePix(entity.getChavePix())
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected ProfissionalGridDTO buildGridDTOFromEntity(Profissional entity) {
        return ProfissionalGridDTO.builder()
                .id(entity.getId())
                .pessoaNome(entity.getPessoa() != null ? entity.getPessoa().getNome() : null)
                .conselho(entity.getConselho())
                .codigoConselho(entity.getCodigoConselho())
                .tipoRemuneracao(
                    entity.getTipoRemuneracao() != null ? entity.getTipoRemuneracao().name() : null
                )
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("conselho", "codigoConselho", "ativo", "createdAt");
    }

    @Override
    protected Class<Profissional> getEntityClass() {
        return Profissional.class;
    }
}
