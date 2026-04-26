package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgendaRegraDTO implements DTO {

    private UUID id;
    private UUID agendaId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFim;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFim;

    private Integer duracaoSessaoMinutos;
    private Set<String> diasSemana;
    private Set<UUID> convenioIds;
    private List<String> convenioNomes;
    private Set<UUID> procedimentoIds;
    private List<String> procedimentoNomes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
