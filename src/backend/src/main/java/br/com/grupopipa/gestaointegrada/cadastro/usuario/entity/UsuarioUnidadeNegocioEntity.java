package br.com.grupopipa.gestaointegrada.cadastro.usuario.entity;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "usuario_unidade_negocio")
public class UsuarioUnidadeNegocioEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unidade_negocio_id", nullable = false)
    private UnidadeNegocio unidadeNegocio;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    public UsuarioUnidadeNegocioEntity(UsuarioEntity usuario, UnidadeNegocio unidadeNegocio, Boolean isDefault) {
        this.usuario = usuario;
        this.unidadeNegocio = unidadeNegocio;
        this.isDefault = isDefault != null ? isDefault : false;
    }

    protected UsuarioUnidadeNegocioEntity() {
    }

    public UsuarioEntity getUsuario() {
        return usuario;
    }

    public UnidadeNegocio getUnidadeNegocio() {
        return unidadeNegocio;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault != null ? isDefault : false;
    }
}
