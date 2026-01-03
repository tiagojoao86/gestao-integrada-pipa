package br.com.grupopipa.gestaointegrada.financeiro.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaTipoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidade que representa uma categoria de título financeiro. Permite
 * classificar receitas e
 * despesas com suporte a agrupamento hierárquico.
 *
 * <p>
 * Exemplo de uso: - Agrupador: "DESPESAS OPERACIONAIS" (codigo: "001") -
 * Categoria: "Material de
 * Escritório" (codigo: "001.001") - Categoria: "Material de Limpeza" (codigo:
 * "001.002")
 *
 * <p>
 * - Agrupador: "RECEITA DE SERVIÇOS" (codigo: "002") - Categoria: "Consultoria"
 * (codigo:
 * "002.001") - Categoria: "Manutenção" (codigo: "002.002")
 */
@Entity
@Table(name = "titulo_categoria")
public class TituloCategoria extends BaseEntity {

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Embedded
    private Nome nome;

    @Column(name = "descricao", length = 400)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TituloCategoriaTipoEnum tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agrupador_id", foreignKey = @ForeignKey(name = "fk_titulo_categoria_agrupador"))
    private TituloCategoria agrupador;

    private TituloCategoria(
            String codigo,
            Nome nome,
            String descricao,
            TituloCategoriaTipoEnum tipo,
            TituloCategoria agrupador) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.agrupador = agrupador;
    }

    protected TituloCategoria() {
    }

    private static class ValidatedData {
        final String codigo;
        final Nome nome;
        final String descricao;
        final TituloCategoriaTipoEnum tipo;
        final TituloCategoria agrupador;

        ValidatedData(
                String codigo,
                Nome nome,
                String descricao,
                TituloCategoriaTipoEnum tipo,
                TituloCategoria agrupador) {
            this.codigo = codigo;
            this.nome = nome;
            this.descricao = descricao;
            this.tipo = tipo;
            this.agrupador = agrupador;
        }
    }

    private static ValidatedData validate(
            String codigo,
            String nomeStr,
            String descricaoStr,
            TituloCategoriaTipoEnum tipo,
            TituloCategoria agrupador) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (codigo == null || codigo.trim().isEmpty()) {
            violations.add(new BeanValidationMessage("codigo", "Código é obrigatório"));
        }

        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);

        if (descricaoStr != null && descricaoStr.length() > 400) {
            violations.add(
                    new BeanValidationMessage("descricao", "Descrição deve ter no máximo 400 caracteres"));
        }

        if (tipo == null) {
            violations.add(new BeanValidationMessage("tipo", "Tipo é obrigatório"));
        }

        // Validação: se tem agrupador, o tipo deve ser o mesmo
        if (agrupador != null && tipo != null && agrupador.getTipo() != tipo) {
            violations.add(
                    new BeanValidationMessage(
                            "agrupador", "O tipo da categoria deve ser o mesmo do agrupador"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("tituloCategoria", violations);
        }

        return new ValidatedData(
                codigo != null ? codigo.trim() : null, nome, descricaoStr, tipo, agrupador);
    }

    public void atualizar(
            String codigo,
            String nome,
            String descricao,
            TituloCategoriaTipoEnum tipo,
            TituloCategoria agrupador) {
        ValidatedData data = validate(codigo, nome, descricao, tipo, agrupador);
        this.codigo = data.codigo;
        this.nome = data.nome;
        this.descricao = data.descricao;
        this.tipo = data.tipo;
        this.agrupador = data.agrupador;
    }

    /** Verifica se esta categoria é um agrupador (não tem agrupador pai) */
    public boolean isAgrupador() {
        return this.agrupador == null;
    }

    /** Verifica se esta categoria pertence a um agrupador */
    public boolean temAgrupador() {
        return this.agrupador != null;
    }

    // Getters
    public String getCodigo() {
        return codigo;
    }

    public Nome getNome() {
        if (Objects.isNull(this.nome)) {
            nome = Nome.of("");
        }

        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public TituloCategoriaTipoEnum getTipo() {
        return tipo;
    }

    public TituloCategoria getAgrupador() {
        return agrupador;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TituloCategoria)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TituloCategoria that = (TituloCategoria) o;
        return Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nome);
    }

    public static class Builder {
        private String codigo;
        private String nome;
        private String descricao;
        private TituloCategoriaTipoEnum tipo;
        private TituloCategoria agrupador;

        public Builder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder tipo(TituloCategoriaTipoEnum tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder agrupador(TituloCategoria agrupador) {
            this.agrupador = agrupador;
            return this;
        }

        public TituloCategoria build() {
            ValidatedData data = validate(this.codigo, this.nome, this.descricao, this.tipo, this.agrupador);
            return new TituloCategoria(data.codigo, data.nome, data.descricao, this.tipo, this.agrupador);
        }
    }
}
