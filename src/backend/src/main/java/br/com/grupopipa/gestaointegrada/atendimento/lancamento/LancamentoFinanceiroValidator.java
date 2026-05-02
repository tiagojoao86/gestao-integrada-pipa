package br.com.grupopipa.gestaointegrada.atendimento.lancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.ConvenioTipoCobrancaEnum;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroSituacaoEnum;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroStatusFinanceiroEnum;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;

public class LancamentoFinanceiroValidator {

    private LancamentoFinanceiroValidator() {
    }

    public static ValidatedData validate(
            UUID atendimentoId,
            Long atendimentoNumero,
            LocalDate dataAtendimento,
            UUID pacienteId,
            String pacienteNome,
            UUID convenioId,
            String convenioNome,
            ConvenioTipoCobrancaEnum convenioTipoCobranca,
            BigDecimal valorTotal,
            LancamentoFinanceiroSituacaoEnum situacao,
            LancamentoFinanceiroStatusFinanceiroEnum statusFinanceiro,
            String observacoes,
            UUID setorId,
            String setorNome,
            UUID unidadeNegocioId,
            String unidadeNegocioNome,
            UUID tituloId) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (atendimentoId == null) {
            violations.add(new BeanValidationMessage("atendimentoId", "Atendimento é obrigatório."));
        }
        if (pacienteId == null) {
            violations.add(new BeanValidationMessage("pacienteId", "Paciente é obrigatório."));
        }
        if (situacao == null) {
            violations.add(new BeanValidationMessage("situacao", "Situação é obrigatória."));
        }
        if (statusFinanceiro == null) {
            violations.add(new BeanValidationMessage(
                "statusFinanceiro", "Status financeiro é obrigatório."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("lancamentoFinanceiro", violations);
        }

        Money valor = valorTotal != null ? Money.positiveOrZero(valorTotal) : Money.zero();
        return new ValidatedData(
            atendimentoId, atendimentoNumero, dataAtendimento,
            pacienteId, pacienteNome, convenioId, convenioNome, convenioTipoCobranca,
            valor, situacao, statusFinanceiro, observacoes,
            setorId, setorNome, unidadeNegocioId, unidadeNegocioNome, tituloId);
    }

    public static class ValidatedData {
        public final UUID atendimentoId;
        public final Long atendimentoNumero;
        public final LocalDate dataAtendimento;
        public final UUID pacienteId;
        public final String pacienteNome;
        public final UUID convenioId;
        public final String convenioNome;
        public final ConvenioTipoCobrancaEnum convenioTipoCobranca;
        public final Money valorTotal;
        public final LancamentoFinanceiroSituacaoEnum situacao;
        public final LancamentoFinanceiroStatusFinanceiroEnum statusFinanceiro;
        public final String observacoes;
        public final UUID setorId;
        public final String setorNome;
        public final UUID unidadeNegocioId;
        public final String unidadeNegocioNome;
        public final UUID tituloId;

        ValidatedData(UUID atendimentoId, Long atendimentoNumero, LocalDate dataAtendimento,
                UUID pacienteId, String pacienteNome, UUID convenioId, String convenioNome,
                ConvenioTipoCobrancaEnum convenioTipoCobranca,
                Money valorTotal, LancamentoFinanceiroSituacaoEnum situacao,
                LancamentoFinanceiroStatusFinanceiroEnum statusFinanceiro, String observacoes,
                UUID setorId, String setorNome,
                UUID unidadeNegocioId, String unidadeNegocioNome, UUID tituloId) {
            this.atendimentoId = atendimentoId;
            this.atendimentoNumero = atendimentoNumero;
            this.dataAtendimento = dataAtendimento;
            this.pacienteId = pacienteId;
            this.pacienteNome = pacienteNome;
            this.convenioId = convenioId;
            this.convenioNome = convenioNome;
            this.convenioTipoCobranca = convenioTipoCobranca;
            this.valorTotal = valorTotal;
            this.situacao = situacao;
            this.statusFinanceiro = statusFinanceiro;
            this.observacoes = observacoes;
            this.setorId = setorId;
            this.setorNome = setorNome;
            this.unidadeNegocioId = unidadeNegocioId;
            this.unidadeNegocioNome = unidadeNegocioNome;
            this.tituloId = tituloId;
        }
    }
}
