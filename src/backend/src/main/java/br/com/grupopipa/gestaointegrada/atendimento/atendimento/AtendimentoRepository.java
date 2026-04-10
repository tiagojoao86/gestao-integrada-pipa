package br.com.grupopipa.gestaointegrada.atendimento.atendimento;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity.Atendimento;

public interface AtendimentoRepository
        extends JpaRepository<Atendimento, UUID>, JpaSpecificationExecutor<Atendimento> {
}
