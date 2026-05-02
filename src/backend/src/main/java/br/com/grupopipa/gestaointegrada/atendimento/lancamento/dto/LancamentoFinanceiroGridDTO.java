package br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroSituacaoEnum;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroStatusFinanceiroEnum;
import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class LancamentoFinanceiroGridDTO implements GridDTO {

    private UUID id;
    private Long atendimentoNumero;
    private LocalDate dataAtendimento;
    private String pacienteNome;
    private String convenioNome;
    private BigDecimal valorTotal;
    private LancamentoFinanceiroSituacaoEnum situacao;
    private LancamentoFinanceiroStatusFinanceiroEnum statusFinanceiro;
    private int procedimentosCount;
    private LocalDateTime createdAt;
    private Boolean deleted;
}
