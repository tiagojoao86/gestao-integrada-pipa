package br.com.grupopipa.gestaointegrada.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public DashboardServiceImpl(TituloRepository tituloRepository) {
        this.tituloRepository = tituloRepository;
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
}
