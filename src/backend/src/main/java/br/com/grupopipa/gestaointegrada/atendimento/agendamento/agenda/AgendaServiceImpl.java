package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.dto.AgendaDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.dto.AgendaGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.entity.Agenda;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.ProfissionalRepository;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;
import br.com.grupopipa.gestaointegrada.cadastro.setor.SetorRepository;
import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
public class AgendaServiceImpl
        extends CrudServiceImpl<AgendaDTO, AgendaGridDTO, Agenda, AgendaRepository>
        implements AgendaService {

    private final ProfissionalRepository profissionalRepository;
    private final SetorRepository setorRepository;

    public AgendaServiceImpl(
            AgendaRepository repository,
            Specifications<Agenda> specifications,
            ProfissionalRepository profissionalRepository,
            SetorRepository setorRepository) {
        super(repository, specifications);
        this.profissionalRepository = profissionalRepository;
        this.setorRepository = setorRepository;
    }

    @Override
    protected Agenda mergeEntityAndDTO(Agenda entity, AgendaDTO dto) {
        if (Objects.isNull(entity)) {
            return criarNovaAgenda(dto);
        }
        return atualizarAgenda(entity, dto);
    }

    private Agenda criarNovaAgenda(AgendaDTO dto) {
        Profissional profissional = buscarProfissional(dto.getProfissionalId());
        Setor setor = buscarSetor(dto.getSetorId());
        return new Agenda.Builder()
                .nome(dto.getNome())
                .profissional(profissional)
                .setor(setor)
                .ativo(dto.getAtivo() != null ? dto.getAtivo() : true)
                .build();
    }

    private Agenda atualizarAgenda(Agenda entity, AgendaDTO dto) {
        Profissional profissional = buscarProfissional(dto.getProfissionalId());
        Setor setor = buscarSetor(dto.getSetorId());
        entity.atualizar(dto.getNome(), profissional, setor, dto.getAtivo());
        return entity;
    }

    private Profissional buscarProfissional(UUID profissionalId) {
        if (profissionalId == null) {
            return null;
        }
        return profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado."));
    }

    private Setor buscarSetor(UUID setorId) {
        if (setorId == null) {
            return null;
        }
        return setorRepository.findById(setorId)
                .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado."));
    }

    @Override
    protected AgendaDTO buildDTOFromEntity(Agenda entity) {
        return AgendaDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .profissionalId(entity.getProfissional() != null
                        ? entity.getProfissional().getId() : null)
                .profissionalNome(entity.getProfissional() != null
                        ? entity.getProfissional().getPessoa().getNome() : null)
                .setorId(entity.getSetor() != null ? entity.getSetor().getId() : null)
                .setorNome(entity.getSetor() != null ? entity.getSetor().getNome() : null)
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected AgendaGridDTO buildGridDTOFromEntity(Agenda entity) {
        return AgendaGridDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .profissionalNome(entity.getProfissional() != null
                        ? entity.getProfissional().getPessoa().getNome() : null)
                .setorNome(entity.getSetor() != null ? entity.getSetor().getNome() : null)
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "ativo");
    }

    @Override
    protected Class<Agenda> getEntityClass() {
        return Agenda.class;
    }
}
