package br.com.grupopipa.gestaointegrada.atendimento.tabela;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.Tabela;

public interface TabelaRepository extends JpaRepository<Tabela, UUID>, JpaSpecificationExecutor<Tabela> {

    List<Tabela> findAllByAtivoTrueAndDeletedFalseOrderByNomeAsc();
}
