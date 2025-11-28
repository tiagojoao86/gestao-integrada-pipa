package br.com.grupopipa.gestaointegrada.cadastro.modulo.entity;

import br.com.grupopipa.gestaointegrada.cadastro.modulo.GrupoModuloEnum;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.valueobject.Chave;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity(name = "modulo")
public class ModuloEntity extends BaseEntity {

    @Embedded
    private Chave chave;

    @Embedded
    private Nome nome;

    @Enumerated(EnumType.STRING)
    private GrupoModuloEnum grupo;

    protected ModuloEntity() {
    }

    public String getChave() {
        return this.chave != null ? this.chave.getValue() : null;
    }

    public String getNome() {
        return this.nome != null ? this.nome.getValue() : null;
    }

    public GrupoModuloEnum getGrupo() {
        return grupo;
    }

}
