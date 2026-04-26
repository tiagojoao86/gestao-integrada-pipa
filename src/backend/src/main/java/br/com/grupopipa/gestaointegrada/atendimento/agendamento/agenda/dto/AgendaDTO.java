package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgendaDTO implements DTO {
    private UUID id;
    private String nome;
    private UUID profissionalId;
    private String profissionalNome;
    private UUID setorId;
    private String setorNome;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
