package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;

@Repository
public interface PessoaRepository
        extends JpaRepository<Pessoa, UUID>, JpaSpecificationExecutor<Pessoa> {
    List<Pessoa> findByAtivaTrue();
}
