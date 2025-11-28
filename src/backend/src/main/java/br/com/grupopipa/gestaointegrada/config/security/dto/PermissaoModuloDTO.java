package br.com.grupopipa.gestaointegrada.config.security.dto;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.modulo.entity.ModuloEntity;
import lombok.Getter;

@Getter
public class PermissaoModuloDTO {
    private String chave;
    private String nome;
    private String grupo;
    private Set<String> permissoes = new HashSet<>();

    public PermissaoModuloDTO(ModuloEntity modulo) {
        this.chave = modulo.getChave();
        this.nome = modulo.getNome();
        this.grupo = modulo.getGrupo().name();
    }

    public void addPermissao(String permissao) {
        if (permissao != null && !permissao.isBlank()) {
            this.permissoes.add(permissao);
        }
    }
}