package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgendaRegraGridDTO implements DTO, GridDTO {

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
    private String diasSemanaFormatado;
    private Integer qtdConvenios;
    private Integer qtdProcedimentos;

    private LocalDateTime createdAt;
    private Boolean deleted;
}
