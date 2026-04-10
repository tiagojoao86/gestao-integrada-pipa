package br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.dto.CodigoConvenioDTO;
import br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.entity.CodigoConvenio;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoRepository;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;

@Service
@Transactional(readOnly = true)
public class CodigoConvenioServiceImpl implements CodigoConvenioService {

    private final CodigoConvenioRepository repository;
    private final ProcedimentoRepository procedimentoRepository;

    public CodigoConvenioServiceImpl(
            CodigoConvenioRepository repository,
            ProcedimentoRepository procedimentoRepository) {
        this.repository = repository;
        this.procedimentoRepository = procedimentoRepository;
    }

    @Override
    public List<CodigoConvenioDTO> findAllByConvenioId(UUID convenioId) {
        return repository.findAllByConvenioId(convenioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void syncForConvenio(Convenio convenio, List<CodigoConvenioDTO> codigos) {
        if (codigos == null || codigos.isEmpty()) {
            repository.deleteAllByConvenioId(convenio.getId());
            return;
        }

        List<CodigoConvenio> existentes = repository.findAllByConvenioId(convenio.getId());
        Map<UUID, CodigoConvenio> existentesPorProcedimento = existentes.stream()
                .collect(Collectors.toMap(c -> c.getProcedimento().getId(), c -> c));

        List<UUID> procedimentoIdsIncoming = codigos.stream()
                .filter(dto -> dto.getProcedimentoId() != null)
                .map(CodigoConvenioDTO::getProcedimentoId)
                .collect(Collectors.toList());

        // Remove os que não vieram mais
        existentes.stream()
                .filter(e -> !procedimentoIdsIncoming.contains(e.getProcedimento().getId()))
                .forEach(e -> repository.deleteById(e.getId()));

        // Atualiza ou cria
        for (CodigoConvenioDTO dto : codigos) {
            if (dto.getProcedimentoId() == null || dto.getCodigo() == null) {
                continue;
            }
            CodigoConvenio existente = existentesPorProcedimento.get(dto.getProcedimentoId());
            if (existente != null) {
                existente.atualizar(dto.getCodigo());
                repository.save(existente);
            } else {
                Procedimento procedimento = procedimentoRepository
                        .findById(dto.getProcedimentoId()).orElse(null);
                if (procedimento == null) {
                    continue;
                }
                CodigoConvenio novo = new CodigoConvenio.Builder()
                        .convenio(convenio)
                        .procedimento(procedimento)
                        .codigo(dto.getCodigo())
                        .build();
                repository.save(novo);
            }
        }
    }

    private CodigoConvenioDTO toDTO(CodigoConvenio entity) {
        return CodigoConvenioDTO.builder()
                .id(entity.getId())
                .convenioId(entity.getConvenio() != null ? entity.getConvenio().getId() : null)
                .procedimentoId(entity.getProcedimento() != null ? entity.getProcedimento().getId() : null)
                .procedimentoCodigo(entity.getProcedimento() != null
                    ? entity.getProcedimento().getCodigo() : null)
                .procedimentoDescricao(entity.getProcedimento() != null
                    ? entity.getProcedimento().getDescricao() : null)
                .codigo(entity.getCodigo())
                .build();
    }
}
