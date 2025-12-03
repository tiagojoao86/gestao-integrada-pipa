package br.com.grupopipa.gestaointegrada.financeiro.entity;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade para Conta Bancária
 */
@Entity
@Table(name = "conta_bancaria")
public class ContaBancaria extends BaseEntity {
    
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;
    
    @Column(name = "banco", length = 100)
    private String banco;
    
    @Column(name = "agencia", length = 10)
    private String agencia;
    
    @Column(name = "numero_conta", length = 20)
    private String numeroConta;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoConta tipo;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "saldo_inicial", precision = 15, scale = 2))
    private Money saldoInicial;
    
    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;
    
    protected ContaBancaria() {}
    
    public ContaBancaria(String nome, TipoConta tipo) {
        validarCamposObrigatorios(nome, tipo);
        this.nome = nome;
        this.tipo = tipo;
        this.saldoInicial = Money.zero();
    }
    
    public ContaBancaria(String nome, TipoConta tipo, String banco, String agencia, String numeroConta) {
        this(nome, tipo);
        this.banco = banco;
        this.agencia = agencia;
        this.numeroConta = numeroConta;
    }
    
    private void validarCamposObrigatorios(String nome, TipoConta tipo) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        
        if (nome == null || nome.isBlank()) {
            violations.add(new BeanValidationMessage("nome", "Nome da conta é obrigatório"));
        } else if (nome.length() > 100) {
            violations.add(new BeanValidationMessage("nome", "Nome deve ter no máximo 100 caracteres"));
        }
        
        if (tipo == null) {
            violations.add(new BeanValidationMessage("tipo", "Tipo da conta é obrigatório"));
        }
        
        if (!violations.isEmpty()) {
            throw new BeanValidationException("contaBancaria", violations);
        }
    }
    
    public void definirSaldoInicial(Money saldoInicial) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        
        if (saldoInicial == null) {
            violations.add(new BeanValidationMessage("saldoInicial", "Saldo inicial não pode ser nulo"));
        }
        
        if (!violations.isEmpty()) {
            throw new BeanValidationException("contaBancaria", violations);
        }
        
        this.saldoInicial = saldoInicial;
    }
    
    public void atualizar(String nome, String banco, String agencia, String numeroConta) {
        if (nome != null && !nome.isBlank()) {
            validarCamposObrigatorios(nome, this.tipo);
            this.nome = nome;
        }
        this.banco = banco;
        this.agencia = agencia;
        this.numeroConta = numeroConta;
    }
    
    public void ativar() {
        this.ativa = true;
    }
    
    public void inativar() {
        this.ativa = false;
    }
    
    public boolean isCaixa() {
        return tipo == TipoConta.CAIXA;
    }
    
    public boolean isBancaria() {
        return tipo == TipoConta.CORRENTE || tipo == TipoConta.POUPANCA;
    }
    
    // Getters
    public String getNome() {
        return nome;
    }
    
    public String getBanco() {
        return banco;
    }
    
    public String getAgencia() {
        return agencia;
    }
    
    public String getNumeroConta() {
        return numeroConta;
    }
    
    public TipoConta getTipo() {
        return tipo;
    }
    
    public Money getSaldoInicial() {
        return saldoInicial;
    }
    
    public Boolean getAtiva() {
        return ativa;
    }
    
    public boolean isAtiva() {
        return ativa != null && ativa;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContaBancaria)) return false;
        if (!super.equals(o)) return false;
        ContaBancaria that = (ContaBancaria) o;
        return Objects.equals(nome, that.nome) && 
               Objects.equals(numeroConta, that.numeroConta);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nome, numeroConta);
    }
    
    @Override
    public String toString() {
        if (banco != null && numeroConta != null) {
            return nome + " - " + banco + " Ag: " + agencia + " Conta: " + numeroConta;
        }
        return nome;
    }
}
