package br.com.grupopipa.gestaointegrada.cadastro.perfil.entity;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilDTO;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

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

    public String getNome() {
        return this.nome != null ? this.nome.getValue() : null;
    }

    public Set<PerfilModuloEntity> getPermissoes() {
        return this.permissoes;
    }

    public void addPermissao(PerfilModuloEntity permissao) {
        if (permissao == null) return;
        
        permissao.setPerfil(this);
        this.permissoes.add(permissao);
    }

    public void removePermissao(PerfilModuloEntity permissao) {
        if (permissao == null) return;
        this.permissoes.remove(permissao);
        try {
            permissao.setPerfil(null);
        } catch (Exception e) {
            // ignore
        }
    }

    private static class ValidatedData {
        final Nome nome;

        ValidatedData(Nome nome) {
            this.nome = nome;
        }
    }

    private static ValidatedData validate(String nomeStr) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("perfil", violations);
        }
        return new ValidatedData(nome);
    }

    public void updatePerfilFromDTO(PerfilDTO dto) {
        ValidatedData data = validate(dto.getNome());
        this.nome = data.nome;
    }

    public static class Builder {

        private String nome;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public PerfilEntity build() {
            ValidatedData data = validate(this.nome);
            return new PerfilEntity(data.nome);
        }
    }
}
