package br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.ConvenioTipoCobrancaEnum;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroSituacaoEnum;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroStatusFinanceiroEnum;
import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LancamentoFinanceiroDTO implements DTO {

    private UUID id;

    private UUID atendimentoId;
    private Long atendimentoNumero;
    private LocalDate dataAtendimento;

    private UUID pacienteId;
    private String pacienteNome;

    private UUID convenioId;
    private String convenioNome;
    private ConvenioTipoCobrancaEnum convenioTipoCobranca;

    private BigDecimal valorTotal;
    private LancamentoFinanceiroSituacaoEnum situacao;
    private LancamentoFinanceiroStatusFinanceiroEnum statusFinanceiro;

    private List<LancamentoFinanceiroProcedimentoDTO> procedimentos;

    private String observacoes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
