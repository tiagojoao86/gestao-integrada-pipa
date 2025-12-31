package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;

@Repository
public interface TituloRepository
    extends JpaRepository<Titulo, UUID>, JpaSpecificationExecutor<Titulo>, TituloRepositoryCustom {}
