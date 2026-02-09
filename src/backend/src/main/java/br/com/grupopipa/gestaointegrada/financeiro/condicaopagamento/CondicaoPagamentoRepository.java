package br.com.grupopipa.gestaointegrada.financeiro.condicaopagamento;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.financeiro.entity.CondicaoPagamento;

public interface CondicaoPagamentoRepository
        extends JpaRepository<CondicaoPagamento, UUID>, JpaSpecificationExecutor<CondicaoPagamento> {
}
