package br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AtendimentoDTO implements DTO {

    private UUID id;
    private Long numero;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

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

    private List<AtendimentoProcedimentoDTO> procedimentos;

    private String observacoes;

    private UUID lancamentoFinanceiroId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
