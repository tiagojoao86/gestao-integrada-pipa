package br.com.grupopipa.gestaointegrada.dashboard;

import static br.com.grupopipa.gestaointegrada.dashboard.DashboardConstants.R_DASHBOARD;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
