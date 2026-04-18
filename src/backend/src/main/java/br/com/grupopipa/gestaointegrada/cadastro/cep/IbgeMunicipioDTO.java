package br.com.grupopipa.gestaointegrada.cadastro.cep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO interno para deserializar a resposta da API do IBGE (municipios).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IbgeMunicipioDTO {

    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
