package br.com.grupopipa.gestaointegrada.atendimento.convenio;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.dto.ConvenioDTO;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.dto.ConvenioGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
public class ConvenioServiceImpl
        extends CrudServiceImpl<ConvenioDTO, ConvenioGridDTO, Convenio, ConvenioRepository>
        implements ConvenioService {

    private final PessoaRepository pessoaRepository;

    public ConvenioServiceImpl(
            ConvenioRepository repository,
            Specifications<Convenio> specifications,
            PessoaRepository pessoaRepository) {
        super(repository, specifications);
        this.pessoaRepository = pessoaRepository;
    }

    @Override
    protected Convenio mergeEntityAndDTO(Convenio entity, ConvenioDTO dto) {
        Pessoa pessoa = resolverPessoa(dto.getPessoaId());

        if (Objects.isNull(entity)) {
            return new Convenio.Builder()
                    .nome(dto.getNome())
                    .pessoa(pessoa)
                    .registroAns(dto.getRegistroAns())
                    .ativo(dto.getAtivo())
                    .build();
        }

        entity.atualizar(dto.getNome(), pessoa, dto.getRegistroAns(), dto.getAtivo());
        return entity;
    }

    private Pessoa resolverPessoa(UUID pessoaId) {
        if (pessoaId == null) {
            return null;
        }
        return pessoaRepository.findById(pessoaId).orElse(null);
    }

    @Override
    protected ConvenioDTO buildDTOFromEntity(Convenio entity) {
        return ConvenioDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .pessoaId(entity.getPessoa() != null ? entity.getPessoa().getId() : null)
                .pessoaNome(entity.getPessoa() != null ? entity.getPessoa().getNome() : null)
                .registroAns(entity.getRegistroAns())
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected ConvenioGridDTO buildGridDTOFromEntity(Convenio entity) {
        return ConvenioGridDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .pessoaNome(entity.getPessoa() != null ? entity.getPessoa().getNome() : null)
                .registroAns(entity.getRegistroAns())
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "registroAns", "ativo", "createdAt");
    }

    @Override
    protected Class<Convenio> getEntityClass() {
        return Convenio.class;
    }
}
