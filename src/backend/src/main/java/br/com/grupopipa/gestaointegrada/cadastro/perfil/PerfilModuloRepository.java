package br.com.grupopipa.gestaointegrada.cadastro.perfil;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional; // Importar Transactional

import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilModuloEntity;

public interface PerfilModuloRepository extends JpaRepository<PerfilModuloEntity, UUID> {

    @Modifying // Indica que este método modifica o estado do banco de dados
    @Transactional // Garante que a operação de deleção seja transacional
    @Query("DELETE FROM perfil_modulo pme WHERE pme.perfil.id = :perfilId") 
    void deleteByPerfilId(UUID perfilId);

    List<PerfilModuloEntity> findByPerfilId(UUID perfilId);
}