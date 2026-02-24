package br.com.grupopipa.gestaointegrada.financeiro.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.financeiro.condicaopagamento.CondicaoPagamentoValidator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Entidade que representa uma condição de pagamento.
 *
 * <p>
 * O campo {@code condicao} aceita dois formatos e serve como identificador visual:
 * <ul>
 * <li><b>"Nx"</b> (ex: "3x") — gera N parcelas com intervalos de 30 dias
 * (30, 60, 90...)</li>
 * <li><b>"dias/dias/dias"</b> (ex: "10/20/40") — cada número representa os
 * dias absolutos de vencimento a partir da data de emissão do título</li>
 * </ul>
 *
 * <p>
 * Validações DDD dentro da entidade:
 * <ul>
 * <li>Formato "Nx": N deve ser &gt; 0</li>
 * <li>Formato "d/d/d": cada valor deve ser &gt; 0 e em ordem crescente</li>
 * <li>Qualquer outro formato: BeanValidationException</li>
 * </ul>
 */
@Entity
@Table(name = "condicao_pagamento")
public class CondicaoPagamento extends BaseEntity {

    private static final Pattern PATTERN_NX = Pattern.compile("^(\\d+)x$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_DIAS = Pattern.compile("^\\d+(/\\d+)*$");
    private static final int INTERVALO_PADRAO = 30;

    @Column(name = "condicao", nullable = false, length = 100, unique = true)
    private String condicao;

    @Column(name = "descricao", length = 400)
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    private CondicaoPagamento(
            String condicao,
            String descricao,
            Boolean ativo) {
        this.condicao = condicao;
        this.descricao = descricao;
        this.ativo = ativo;
    }

    protected CondicaoPagamento() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private String condicao;
        private String descricao;
        private Boolean ativo;

        public Builder condicao(String condicao) {
            this.condicao = condicao;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public CondicaoPagamento build() {
            CondicaoPagamentoValidator.ValidatedData data = CondicaoPagamentoValidator.validate(
                    this.condicao, this.descricao, this.ativo);
            return new CondicaoPagamento(data.condicao, data.descricao, data.ativo);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(
            String condicao,
            String descricao,
            Boolean ativo) {
        CondicaoPagamentoValidator.ValidatedData data = CondicaoPagamentoValidator.validate(
                condicao, descricao, ativo);
        this.condicao = data.condicao;
        this.descricao = data.descricao;
        this.ativo = data.ativo;
    }

    /**
     * Retorna a quantidade de parcelas definida pela condição de pagamento.
     * <ul>
     * <li>"3x" → 3</li>
     * <li>"10/20/40" → 3 (quantidade de elementos separados por /)</li>
     * </ul>
     */
    @Transient
    public int getQuantidadeParcelas() {
        if (condicao == null || condicao.trim().isEmpty()) {
            return 0;
        }

        var matcher = PATTERN_NX.matcher(condicao.trim());
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }

        if (PATTERN_DIAS.matcher(condicao.trim()).matches()) {
            return condicao.trim().split("/").length;
        }

        return 0;
    }

    /**
     * Retorna a lista de dias de vencimento calculados a partir da condição.
     * <ul>
     * <li>"3x" → [30, 60, 90]</li>
     * <li>"10/20/40" → [10, 20, 40]</li>
     * </ul>
     */
    @Transient
    public List<Integer> getDiasVencimento() {
        if (condicao == null || condicao.trim().isEmpty()) {
            return List.of();
        }

        String trimmed = condicao.trim();

        var matcher = PATTERN_NX.matcher(trimmed);
        if (matcher.matches()) {
            int n = Integer.parseInt(matcher.group(1));
            List<Integer> dias = new ArrayList<>(n);
            for (int i = 1; i <= n; i++) {
                dias.add(i * INTERVALO_PADRAO);
            }
            return dias;
        }

        if (PATTERN_DIAS.matcher(trimmed).matches()) {
            String[] parts = trimmed.split("/");
            List<Integer> dias = new ArrayList<>(parts.length);
            for (String part : parts) {
                dias.add(Integer.parseInt(part));
            }
            return dias;
        }

        return List.of();
    }

    // Getters
    public String getCondicao() {
        return condicao;
    }

    public String getDescricao() {
        return descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CondicaoPagamento)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        CondicaoPagamento that = (CondicaoPagamento) o;
        return Objects.equals(condicao, that.condicao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), condicao);
    }
}
