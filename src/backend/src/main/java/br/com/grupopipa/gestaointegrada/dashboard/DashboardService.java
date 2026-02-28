package br.com.grupopipa.gestaointegrada.dashboard;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato do serviço de dashboards.
 * Cada quadro adicionado ao sistema gera um novo método nesta interface.
 */
public interface DashboardService {

    List<DFCItemDTO> getFluxoCaixa(LocalDate dataInicio, LocalDate dataFim, RegimeDFC regime);

    List<DFCDetalheItemDTO> getFluxoCaixaDetalhe(LocalDate dataInicio, LocalDate dataFim, RegimeDFC regime);
}
