package br.com.grupopipa.gestaointegrada.cadastro.cep;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO de resposta para busca de logradouros via ViaCEP.
 * O campo cidade mapeia o campo "localidade" retornado pela ViaCEP.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogradouroDTO {

    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;

    @JsonAlias("localidade")
    private String cidade;

    private String uf;

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }
}
