package br.com.grupopipa.gestaointegrada.cadastro.perfil.entity;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilDTO;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilValidator;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

@Entity(name = "perfil")
public class PerfilEntity extends BaseEntity {

    @Embedded
    private Nome nome;

    @OneToMany(mappedBy = "perfil", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PerfilModuloEntity> permissoes = new HashSet<>();

    protected PerfilEntity() {
    }

    private PerfilEntity(Nome nome) {
        this.nome = nome;
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {

        private String nome;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public PerfilEntity build() {
            PerfilValidator.ValidatedData data = PerfilValidator.validate(this.nome);
            return new PerfilEntity(data.nome);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void updatePerfilFromDTO(PerfilDTO dto) {
        PerfilValidator.ValidatedData data = PerfilValidator.validate(dto.getNome());
        this.nome = data.nome;
    }

    public void addPermissao(PerfilModuloEntity permissao) {
        if (permissao == null) {
            return;
        }

        permissao.setPerfil(this);
        this.permissoes.add(permissao);
    }

    public void removePermissao(PerfilModuloEntity permissao) {
        if (permissao == null) {
            return;
        }
        this.permissoes.remove(permissao);
        try {
            permissao.setPerfil(null);
        } catch (Exception e) {
            // ignore
        }
    }

    public String getNome() {
        return this.nome != null ? this.nome.getValue() : null;
    }

    public Set<PerfilModuloEntity> getPermissoes() {
        return this.permissoes;
    }
}
