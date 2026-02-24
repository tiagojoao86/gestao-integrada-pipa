package br.com.grupopipa.gestaointegrada.cadastro.usuario.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.UsuarioPerfilEntity;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.cadastro.usuario.UsuarioDTO;
import br.com.grupopipa.gestaointegrada.cadastro.usuario.UsuarioValidator;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.valueobject.Login;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import br.com.grupopipa.gestaointegrada.core.valueobject.Senha;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

@Entity(name = "usuario")
public class UsuarioEntity extends BaseEntity {

    @Embedded
    private Nome nome;

    @Embedded
    private Login login;

    @Embedded
    private Senha senha;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioPerfilEntity> perfis = new HashSet<>();

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioUnidadeNegocioEntity> unidadesNegocio = new HashSet<>();

    private UsuarioEntity(Nome nome, Login login, Senha senha) {
        this.nome = nome;
        this.login = login;
        this.senha = senha;
    }

    private UsuarioEntity() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {

        private String login;
        private String nome;
        private String senha;

        public Builder login(String login) {
            this.login = login;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder senha(String senha) {
            this.senha = senha;
            return this;
        }

        public UsuarioEntity build(PasswordEncoder passwordEncoder) {
            UsuarioValidator.ValidatedData data = UsuarioValidator.validate(
                    this.nome, this.login, this.senha, passwordEncoder, true);
            return new UsuarioEntity(data.nome, data.login, data.senha);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void updateUsuarioFromDTO(UsuarioDTO dto, PasswordEncoder passwordEncoder) {
        UsuarioValidator.ValidatedData data = UsuarioValidator.validate(
                dto.getNome(), dto.getLogin(), dto.getSenha(), passwordEncoder, false);
        this.nome = data.nome;
        this.login = data.login;
        if (Objects.nonNull(data.senha)) {
            this.senha = data.senha;
        }
    }

    public void addUnidadeNegocio(UnidadeNegocio unidadeNegocio, Boolean isDefault) {
        if (unidadeNegocio != null) {
            UsuarioUnidadeNegocioEntity association = new UsuarioUnidadeNegocioEntity(this, unidadeNegocio, isDefault);
            this.unidadesNegocio.add(association);
        }
    }

    public void removeUnidadeNegocio(UUID unidadeNegocioId) {
        this.unidadesNegocio.removeIf(uun -> uun.getUnidadeNegocio().getId().equals(unidadeNegocioId));
    }

    public String getNome() {
        return this.nome != null ? this.nome.getValue() : null;
    }

    public String getLogin() {
        return this.login != null ? this.login.getValue() : null;
    }

    public String getSenha() {
        return this.senha != null ? this.senha.getValue() : null;
    }

    public Set<UsuarioPerfilEntity> getPerfis() {
        return perfis;
    }

    public Set<UsuarioUnidadeNegocioEntity> getUnidadesNegocio() {
        return unidadesNegocio;
    }

    public void setUnidadesNegocio(Set<UsuarioUnidadeNegocioEntity> unidadesNegocio) {
        this.unidadesNegocio = unidadesNegocio != null ? unidadesNegocio : new HashSet<>();
    }
}
