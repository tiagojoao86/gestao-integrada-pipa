package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlotDTO {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataHoraInicio;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataHoraFim;

    private boolean livre;

    private UUID agendamentoId;

    private UUID atendimentoId;

    private Long atendimentoNumero;

    private String pacienteNome;

    private String convenioNome;

    private String procedimentoNome;

    private String status;
}
