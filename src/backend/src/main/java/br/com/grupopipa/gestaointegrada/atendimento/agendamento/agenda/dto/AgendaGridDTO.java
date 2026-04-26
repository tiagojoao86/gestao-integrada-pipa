package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgendaGridDTO implements DTO, GridDTO {
    private UUID id;
    private String nome;
    private String profissionalNome;
    private String setorNome;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private Boolean deleted;
}
