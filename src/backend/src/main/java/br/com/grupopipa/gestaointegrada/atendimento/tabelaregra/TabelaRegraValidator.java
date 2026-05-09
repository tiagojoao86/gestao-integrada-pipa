package br.com.grupopipa.gestaointegrada.atendimento.tabelaregra;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.Tabela;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;

public final class TabelaRegraValidator {

    private TabelaRegraValidator() {
    }

    public static ValidatedData validate(Convenio convenio, ConvenioCategoria convenioCategoria, Tabela tabela) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (convenio == null) {
            violations.add(new BeanValidationMessage("convenio", "Convênio é obrigatório."));
        }
        if (tabela == null) {
            violations.add(new BeanValidationMessage("tabela", "Tabela é obrigatória."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("tabelaRegra", violations);
        }

        return new ValidatedData(convenio, convenioCategoria, tabela);
    }

    public static class ValidatedData {
        public final Convenio convenio;
        public final ConvenioCategoria convenioCategoria;
        public final Tabela tabela;

        ValidatedData(Convenio convenio, ConvenioCategoria convenioCategoria, Tabela tabela) {
            this.convenio = convenio;
            this.convenioCategoria = convenioCategoria;
            this.tabela = tabela;
        }
    }
}
