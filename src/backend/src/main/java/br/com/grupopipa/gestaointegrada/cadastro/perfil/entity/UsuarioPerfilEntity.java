package br.com.grupopipa.gestaointegrada.cadastro.perfil.entity;

import br.com.grupopipa.gestaointegrada.cadastro.usuario.entity.UsuarioEntity;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity(name = "usuario_perfil")
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_perfil", columnNames = { "usuario_id", "perfil_id" })
})
@Getter
public class UsuarioPerfilEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "perfil_id", nullable = false)
    private PerfilEntity perfil;
    
    protected UsuarioPerfilEntity() {
    }

    public UsuarioPerfilEntity(UsuarioEntity usuario, PerfilEntity perfil) {
        this.usuario = usuario;
        this.perfil = perfil;
    }

    public void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    public void setPerfil(PerfilEntity perfil) {
        this.perfil = perfil;
    }
}
