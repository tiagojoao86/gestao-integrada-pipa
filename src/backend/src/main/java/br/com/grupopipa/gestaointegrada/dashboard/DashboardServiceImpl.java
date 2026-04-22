package br.com.grupopipa.gestaointegrada.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.atendimento.AtendimentoRepository;
import br.com.grupopipa.gestaointegrada.cadastro.setor.SetorRepository;
import br.com.grupopipa.gestaointegrada.core.Session;
import br.com.grupopipa.gestaointegrada.financeiro.titulo.TituloRepository;

/**
 * Implementação dos serviços de dashboard.
 * Todas as operações são somente leitura (@Transactional readOnly = true).
 * Cada quadro adicionado ao sistema gera um novo método nesta classe.
 */
@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final TituloRepository tituloRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final SetorRepository setorRepository;

    public DashboardServiceImpl(
            TituloRepository tituloRepository,
            AtendimentoRepository atendimentoRepository,
            SetorRepository setorRepository) {
        this.tituloRepository = tituloRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.setorRepository = setorRepository;
    }

    @Override
    public List<DFCItemDTO> getFluxoCaixa(LocalDate dataInicio, LocalDate dataFim, RegimeDFC regime) {
        Set<UUID> unidadeIds = Session.getUnidadeNegocioIds();
        List<DFCProjection> proj = regime == RegimeDFC.CAIXA
                ? tituloRepository.findFluxoCaixaCaixa(dataInicio, dataFim, unidadeIds)
                : tituloRepository.findFluxoCaixaCompetencia(dataInicio, dataFim, unidadeIds);
        return proj.stream()
                .map(p -> DFCItemDTO.builder()
                        .mes(p.getMes())
                        .entradas(p.getEntradas() != null ? p.getEntradas() : BigDecimal.ZERO)
                        .saidas(p.getSaidas() != null ? p.getSaidas() : BigDecimal.ZERO)
                        .build())
                .toList();
    }

    @Override
    public List<DFCDetalheItemDTO> getFluxoCaixaDetalhe(
            LocalDate dataInicio, LocalDate dataFim, RegimeDFC regime) {
        Set<UUID> unidadeIds = Session.getUnidadeNegocioIds();
        List<DFCDetalheProjection> proj = regime == RegimeDFC.CAIXA
                ? tituloRepository.findFluxoCaixaDetalheCaixa(dataInicio, dataFim, unidadeIds)
                : tituloRepository.findFluxoCaixaDetalheCompetencia(dataInicio, dataFim, unidadeIds);
        return proj.stream()
                .map(p -> DFCDetalheItemDTO.builder()
                        .mes(p.getMes())
                        .tipo(p.getTipo())
                        .agrupadorId(p.getAgrupadorId())
                        .agrupadorNome(p.getAgrupadorNome())
                        .agrupadorCodigo(p.getAgrupadorCodigo())
                        .categoriaId(p.getCategoriaId())
                        .categoriaNome(p.getCategoriaNome())
                        .categoriaCodigo(p.getCategoriaCodigo())
                        .temAgrupador(Boolean.TRUE.equals(p.getTemAgrupador()))
                        .total(p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                        .build())
                .toList();
    }

    @Override
    public List<SetorLookupItemDTO> getSetoresByUnidades(List<UUID> unidadeIds) {
        Set<UUID> allowed = Session.getUnidadeNegocioIds();
        Set<UUID> filteredUnidades = resolveUnidadeIds(unidadeIds, allowed);
        if (filteredUnidades.isEmpty()) {
            return List.of();
        }
        return setorRepository.findByUnidadeIds(filteredUnidades).stream()
                .map(p -> SetorLookupItemDTO.builder().id(p.getId()).nome(p.getNome()).build())
                .toList();
    }

    @Override
    public List<AtendimentoMesItemDTO> getAtendimentosPorMes(
            LocalDate dataInicio, LocalDate dataFim, List<UUID> unidadeIds, List<UUID> setorIds) {
        Set<UUID> allowed = Session.getUnidadeNegocioIds();
        Set<UUID> filteredUnidades = resolveUnidadeIds(unidadeIds, allowed);
        Set<UUID> filteredSetores = resolveSetorIds(setorIds, filteredUnidades);
        if (filteredSetores.isEmpty()) {
            return List.of();
        }
        return atendimentoRepository
                .findAtendimentosPorMes(dataInicio, dataFim, filteredSetores)
                .stream()
                .map(p -> AtendimentoMesItemDTO.builder()
                        .mes(p.getMes())
                        .total(p.getTotal() != null ? p.getTotal() : 0L)
                        .build())
                .toList();
    }

    private Set<UUID> resolveUnidadeIds(List<UUID> requested, Set<UUID> allowed) {
        if (requested == null || requested.isEmpty()) {
            return allowed;
        }
        if (allowed.isEmpty()) {
            return Set.copyOf(requested);
        }
        Set<UUID> intersection = requested.stream()
                .filter(allowed::contains)
                .collect(Collectors.toSet());
        return intersection.isEmpty() ? allowed : intersection;
    }

    private Set<UUID> resolveSetorIds(List<UUID> requested, Set<UUID> filteredUnidades) {
        if (filteredUnidades.isEmpty()) {
            return Set.of();
        }
        Set<UUID> allSetores = setorRepository.findByUnidadeIds(filteredUnidades).stream()
                .map(SetorRepository.SetorLookupProjection::getId)
                .collect(Collectors.toSet());
        if (allSetores.isEmpty()) {
            return Set.of();
        }
        if (requested == null || requested.isEmpty()) {
            return allSetores;
        }
        Set<UUID> intersection = requested.stream()
                .filter(allSetores::contains)
                .collect(Collectors.toSet());
        return intersection.isEmpty() ? allSetores : intersection;
    }
}
