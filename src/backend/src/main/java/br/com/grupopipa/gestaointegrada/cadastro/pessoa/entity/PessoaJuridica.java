package br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.valueobject.CNPJ;
import br.com.grupopipa.gestaointegrada.core.valueobject.Email;
import br.com.grupopipa.gestaointegrada.core.valueobject.PhoneNumber;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade para Pessoa Jurídica - herda de Pessoa
 */
@Entity
@Table(name = "pessoa_juridica", uniqueConstraints = {
    @UniqueConstraint(name = "uk_pessoa_juridica_cnpj", columnNames = "cnpj")
})
public class PessoaJuridica extends Pessoa {
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cnpj", nullable = false, length = 14))
    private CNPJ cnpj;
    
    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;
    
    @Column(name = "nome_fantasia", length = 200)
    private String nomeFantasia;
    
    @Column(name = "inscricao_estadual", length = 20)
    private String inscricaoEstadual;
    
    protected PessoaJuridica() {}
    
    public PessoaJuridica(String nome, Email email, PhoneNumber telefone, 
                          CNPJ cnpj, String razaoSocial) {
        super(nome, email, telefone);
        validarCamposObrigatorios(cnpj, razaoSocial);
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
    }
    
    public PessoaJuridica(String nome, Email email, PhoneNumber telefone, 
                          CNPJ cnpj, String razaoSocial, String nomeFantasia) {
        this(nome, email, telefone, cnpj, razaoSocial);
        this.nomeFantasia = nomeFantasia;
    }
    
    private void validarCamposObrigatorios(CNPJ cnpj, String razaoSocial) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        
        if (cnpj == null) {
            violations.add(new BeanValidationMessage("cnpj", "CNPJ é obrigatório para Pessoa Jurídica"));
        }
        if (razaoSocial == null || razaoSocial.isBlank()) {
            violations.add(new BeanValidationMessage("razaoSocial", "Razão Social é obrigatória"));
        } else if (razaoSocial.length() > 200) {
            violations.add(new BeanValidationMessage("razaoSocial", "Razão Social deve ter no máximo 200 caracteres"));
        }
        
        if (!violations.isEmpty()) {
            throw new BeanValidationException("pessoaJuridica", violations);
        }
    }
    
    public void atualizarDados(String razaoSocial, String nomeFantasia, String inscricaoEstadual) {
        if (razaoSocial != null && !razaoSocial.isBlank()) {
            validarCamposObrigatorios(this.cnpj, razaoSocial);
            this.razaoSocial = razaoSocial;
        }
        this.nomeFantasia = nomeFantasia;
        this.inscricaoEstadual = inscricaoEstadual;
    }
    
    // Getters
    public CNPJ getCnpj() {
        return cnpj;
    }
    
    public String getRazaoSocial() {
        return razaoSocial;
    }
    
    public String getNomeFantasia() {
        return nomeFantasia;
    }
    
    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PessoaJuridica)) return false;
        if (!super.equals(o)) return false;
        PessoaJuridica that = (PessoaJuridica) o;
        return Objects.equals(cnpj, that.cnpj);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cnpj);
    }
}
