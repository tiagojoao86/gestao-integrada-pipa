package br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.AberturaCaixaValidator;
import br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.StatusAberturaCaixa;
import br.com.grupopipa.gestaointegrada.financeiro.caixa.entity.Caixa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "abertura_caixa")
public class AberturaCaixa extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caixa_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_abertura_caixa_caixa"))
    private Caixa caixa;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "usuario_nome", nullable = false, length = 255)
    private String usuarioNome;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @Column(name = "valor_abertura", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAbertura;

    @Column(name = "valor_conferencia", precision = 15, scale = 2)
    private BigDecimal valorConferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusAberturaCaixa status;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    private AberturaCaixa(
            Caixa caixa, UUID usuarioId, String usuarioNome, BigDecimal valorAbertura) {
        this.caixa = caixa;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.valorAbertura = valorAbertura;
        this.dataAbertura = LocalDateTime.now();
        this.status = StatusAberturaCaixa.ABERTO;
    }

    protected AberturaCaixa() {
    }

    public void fechar(BigDecimal valorConferencia, String observacoes) {
        AberturaCaixaValidator.validateFechar(valorConferencia);
        this.valorConferencia = valorConferencia;
        this.observacoes = observacoes;
        this.dataFechamento = LocalDateTime.now();
        this.status = StatusAberturaCaixa.FECHADO;
    }

    public Caixa getCaixa() {
        return caixa;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public BigDecimal getValorAbertura() {
        return valorAbertura;
    }

    public BigDecimal getValorConferencia() {
        return valorConferencia;
    }

    public StatusAberturaCaixa getStatus() {
        return status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public static class Builder {
        private Caixa caixa;
        private UUID usuarioId;
        private String usuarioNome;
        private BigDecimal valorAbertura;

        public Builder caixa(Caixa caixa) {
            this.caixa = caixa;
            return this;
        }

        public Builder usuarioId(UUID usuarioId) {
            this.usuarioId = usuarioId;
            return this;
        }

        public Builder usuarioNome(String usuarioNome) {
            this.usuarioNome = usuarioNome;
            return this;
        }

        public Builder valorAbertura(BigDecimal valorAbertura) {
            this.valorAbertura = valorAbertura;
            return this;
        }

        public AberturaCaixa build() {
            AberturaCaixaValidator.ValidatedData data =
                    AberturaCaixaValidator.validate(this.valorAbertura);
            return new AberturaCaixa(caixa, usuarioId, usuarioNome, data.valorAbertura);
        }
    }
}
