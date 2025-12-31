package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;

@Repository
public interface ContaBancariaRepository
    extends JpaRepository<ContaBancaria, UUID>, JpaSpecificationExecutor<ContaBancaria> {}
