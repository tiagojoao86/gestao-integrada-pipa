package br.com.grupopipa.gestaointegrada.core.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Endereço embutido reutilizável.
 * Todos os campos são opcionais — o endereço completo pode estar ausente.
 */
@Embeddable
public class Endereco {

    @Column(name = "endereco_cep", length = 8)
    private String cep;

    @Column(name = "endereco_logradouro", length = 200)
    private String logradouro;

    @Column(name = "endereco_numero", length = 20)
    private String numero;

    @Column(name = "endereco_complemento", length = 100)
    private String complemento;

    @Column(name = "endereco_bairro", length = 100)
    private String bairro;

    @Column(name = "endereco_cidade", length = 100)
    private String cidade;

    @Column(name = "endereco_uf", length = 2)
    private String uf;

    protected Endereco() {
    }

    public Endereco(
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf) {
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
    }

    public String getCep() {
        return cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }

    public boolean isEmpty() {
        return (cep == null || cep.isBlank())
                && (logradouro == null || logradouro.isBlank())
                && (cidade == null || cidade.isBlank());
    }
}
