package br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.valueobject.CPF;
import br.com.grupopipa.gestaointegrada.core.valueobject.Email;
import br.com.grupopipa.gestaointegrada.core.valueobject.PhoneNumber;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade para Pessoa Física - herda de Pessoa
 */
@Entity
@Table(name = "pessoa_fisica", uniqueConstraints = {
    @UniqueConstraint(name = "uk_pessoa_fisica_cpf", columnNames = "cpf")
})
public class PessoaFisica extends Pessoa {
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cpf", nullable = false, length = 11))
    private CPF cpf;
    
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;
    
    protected PessoaFisica() {}
    
    public PessoaFisica(String nome, Email email, PhoneNumber telefone, CPF cpf) {
        super(nome, email, telefone);
        Set<BeanValidationMessage> violations = new HashSet<>();
        
        if (cpf == null) {
            violations.add(new BeanValidationMessage("cpf", "CPF é obrigatório para Pessoa Física"));
        }
        
        if (!violations.isEmpty()) {
            throw new BeanValidationException("pessoaFisica", violations);
        }
        
        this.cpf = cpf;
    }
    
    public PessoaFisica(String nome, Email email, PhoneNumber telefone, CPF cpf, LocalDate dataNascimento) {
        this(nome, email, telefone, cpf);
        this.dataNascimento = dataNascimento;
    }
    
    public void definirDataNascimento(LocalDate dataNascimento) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        
        if (dataNascimento != null && dataNascimento.isAfter(LocalDate.now())) {
            violations.add(new BeanValidationMessage("dataNascimento", 
                "Data de nascimento não pode ser futura"));
        }
        
        if (!violations.isEmpty()) {
            throw new BeanValidationException("pessoaFisica", violations);
        }
        
        this.dataNascimento = dataNascimento;
    }
    
    public Integer calcularIdade() {
        if (dataNascimento == null) {
            return null;
        }
        return LocalDate.now().getYear() - dataNascimento.getYear();
    }
    
    public boolean isMaiorIdade() {
        Integer idade = calcularIdade();
        return idade != null && idade >= 18;
    }
    
    // Getters
    public CPF getCpf() {
        return cpf;
    }
    
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PessoaFisica)) return false;
        if (!super.equals(o)) return false;
        PessoaFisica that = (PessoaFisica) o;
        return Objects.equals(cpf, that.cpf);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cpf);
    }
}
