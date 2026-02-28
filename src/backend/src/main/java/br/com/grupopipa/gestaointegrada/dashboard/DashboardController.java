package br.com.grupopipa.gestaointegrada.dashboard;

import static br.com.grupopipa.gestaointegrada.dashboard.DashboardConstants.R_DASHBOARD;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller de dashboards.
 * Cada quadro adicionado ao sistema gera um novo endpoint GET neste controller,
 * protegido com @PreAuthorize("hasAuthority('DASHBOARD_<GRUPO>_<QUADRO>_LISTAR')").
 */
@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_DASHBOARD)
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/financeiro/fluxo-caixa")
    @PreAuthorize("hasAuthority('DASHBOARD_FINANCEIRO_FLUXO_CAIXA_LISTAR')")
    public ResponseEntity<List<DFCItemDTO>> getFluxoCaixa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "COMPETENCIA") RegimeDFC regime) {
        return ResponseEntity.ok(service.getFluxoCaixa(dataInicio, dataFim, regime));
    }

    @GetMapping("/financeiro/fluxo-caixa-detalhe")
    @PreAuthorize("hasAuthority('DASHBOARD_FINANCEIRO_FLUXO_CAIXA_LISTAR')")
    public ResponseEntity<List<DFCDetalheItemDTO>> getFluxoCaixaDetalhe(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "COMPETENCIA") RegimeDFC regime) {
        return ResponseEntity.ok(service.getFluxoCaixaDetalhe(dataInicio, dataFim, regime));
    }
}
