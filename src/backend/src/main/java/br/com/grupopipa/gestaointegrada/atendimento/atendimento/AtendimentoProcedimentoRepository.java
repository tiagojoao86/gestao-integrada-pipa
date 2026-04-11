package br.com.grupopipa.gestaointegrada.atendimento.atendimento;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity.AtendimentoProcedimento;

public interface AtendimentoProcedimentoRepository extends JpaRepository<AtendimentoProcedimento, UUID> {
}
