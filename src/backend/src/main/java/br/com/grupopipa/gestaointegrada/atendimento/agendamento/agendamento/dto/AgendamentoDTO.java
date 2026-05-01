package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgendamentoDTO implements DTO {

    private UUID id;
    private UUID agendaId;
    private String agendaNome;
    private UUID profissionalId;
    private String profissionalNome;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID convenioId;
    private String convenioNome;
    private UUID categoriaId;
    private String categoriaNome;
    private UUID procedimentoId;
    private String procedimentoNome;
    private String observacao;
    private String status;
    private UUID atendimentoId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private List<LocalDateTime> horariosInicio;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private List<LocalDateTime> horariosFim;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
