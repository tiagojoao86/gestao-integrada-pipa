package br.com.grupopipa.gestaointegrada.atendimento.procedimento;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;

public interface ProcedimentoRepository
        extends JpaRepository<Procedimento, UUID>, JpaSpecificationExecutor<Procedimento> {
}
