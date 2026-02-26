package br.com.grupopipa.gestaointegrada.dashboard;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação dos serviços de dashboard.
 * Todas as operações são somente leitura (@Transactional readOnly = true).
 * Cada quadro adicionado ao sistema gera um novo método nesta classe.
 */
@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
}
