package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgendamentoGridDTO implements GridDTO {

    private UUID id;
    private String agendaNome;
    private String profissionalNome;
    private String pacienteNome;
    private String convenioNome;
    private String procedimentoNome;
    private String status;
    private LocalDate primeiraData;
    private LocalDateTime primeiraDataHora;
    private int qtdHorarios;
    private Boolean deleted;
    private LocalDateTime createdAt;
}
