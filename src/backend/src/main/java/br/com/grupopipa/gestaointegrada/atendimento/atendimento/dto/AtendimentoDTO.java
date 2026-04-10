package br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.atendimento.StatusAtendimento;
import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AtendimentoDTO implements DTO {

    private UUID id;
    private LocalDateTime dataHora;

    private UUID setorId;
    private String setorNome;

    private UUID pacienteId;
    private String pacienteNome;

    private UUID responsavelId;
    private String responsavelNome;

    private UUID convenioId;
    private String convenioNome;

    private UUID convenioCategoriaId;
    private String convenioCategoriaNome;

    private UUID profissionalAtendimentoId;
    private String profissionalAtendimentoNome;

    private UUID profissionalResponsavelId;
    private String profissionalResponsavelNome;

    private UUID procedimentoId;
    private String procedimentoCodigo;
    private String procedimentoDescricao;

    private UUID tabelaItemId;
    private BigDecimal tabelaItemValor;

    private StatusAtendimento status;
    private String observacoes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
