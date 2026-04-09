package br.com.grupopipa.gestaointegrada.atendimento.profissional;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;

public interface ProfissionalRepository
        extends JpaRepository<Profissional, UUID>, JpaSpecificationExecutor<Profissional> {
}
