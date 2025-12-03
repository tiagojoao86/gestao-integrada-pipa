package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, UUID>, JpaSpecificationExecutor<ContaBancaria> {
}
