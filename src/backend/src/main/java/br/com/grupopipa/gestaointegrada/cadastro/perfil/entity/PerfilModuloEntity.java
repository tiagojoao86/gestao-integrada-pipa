package br.com.grupopipa.gestaointegrada.cadastro.perfil.entity;

import br.com.grupopipa.gestaointegrada.cadastro.modulo.entity.ModuloEntity;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode; // Adicionado import
import lombok.Getter;

@Entity(name = "perfil_modulo")
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_perfil_modulo", columnNames = { "perfil_id", "modulo_id" })
})
@Getter
@EqualsAndHashCode(of = {"perfil", "modulo"}, callSuper = false)
public class PerfilModuloEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    private PerfilEntity perfil;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "modulo_id", nullable = false)
    private ModuloEntity modulo;

    @Column(name = "pode_listar", nullable = false)
    private boolean podeListar = false;

    @Column(name = "pode_visualizar", nullable = false)
    private boolean podeVisualizar = false;

    @Column(name = "pode_editar", nullable = false)
    private boolean podeEditar = false;

    @Column(name = "pode_deletar", nullable = false)
    private boolean podeDeletar = false;
    
    protected PerfilModuloEntity() {
    }

    private PerfilModuloEntity(PerfilEntity perfil, ModuloEntity modulo, boolean podeListar, boolean podeVisualizar,
            boolean podeEditar, boolean podeDeletar) {
        this.perfil = perfil;
        this.modulo = modulo;
        this.podeListar = podeListar;
        this.podeVisualizar = podeVisualizar;
        this.podeEditar = podeEditar;
        this.podeDeletar = podeDeletar;
    }

    public static class Builder {
        private PerfilEntity perfil;
        private ModuloEntity modulo;
        private boolean podeListar;
        private boolean podeVisualizar;
        private boolean podeEditar;
        private boolean podeDeletar;

        public Builder perfil(PerfilEntity perfil) {
            this.perfil = perfil;
            return this;
        }

        public Builder modulo(ModuloEntity modulo) {
            this.modulo = modulo;
            return this;
        }

        public Builder podeListar(boolean podeListar) {
            this.podeListar = podeListar;
            return this;
        }

        public Builder podeVisualizar(boolean podeVisualizar) {
            this.podeVisualizar = podeVisualizar;
            return this;
        }

        public Builder podeEditar(boolean podeEditar) {
            this.podeEditar = podeEditar;
            return this;
        }

        public Builder podeDeletar(boolean podeDeletar) {
            this.podeDeletar = podeDeletar;
            return this;
        }

        public PerfilModuloEntity build() {
            return new PerfilModuloEntity(perfil, modulo, podeListar, podeVisualizar, podeEditar, podeDeletar);
        }
    }

    /* package-private setter to keep both sides consistent when managed by PerfilEntity */
    void setPerfil(PerfilEntity perfil) {
        this.perfil = perfil;
    }
}
