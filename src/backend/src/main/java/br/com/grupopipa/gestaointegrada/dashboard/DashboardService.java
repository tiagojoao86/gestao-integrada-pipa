package br.com.grupopipa.gestaointegrada.dashboard;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Contrato do serviço de dashboards.
 * Cada quadro adicionado ao sistema gera um novo método nesta interface.
 */
public interface DashboardService {

    List<DFCItemDTO> getFluxoCaixa(LocalDate dataInicio, LocalDate dataFim, RegimeDFC regime);

    List<DFCDetalheItemDTO> getFluxoCaixaDetalhe(LocalDate dataInicio, LocalDate dataFim, RegimeDFC regime);

    List<SetorLookupItemDTO> getSetoresByUnidades(List<UUID> unidadeIds);

    List<AtendimentoMesItemDTO> getAtendimentosPorMes(
            LocalDate dataInicio, LocalDate dataFim, List<UUID> unidadeIds, List<UUID> setorIds);
}
