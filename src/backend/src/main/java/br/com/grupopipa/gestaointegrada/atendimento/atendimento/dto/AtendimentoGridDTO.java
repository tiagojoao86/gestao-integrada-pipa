package br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AtendimentoGridDTO implements GridDTO {

    private UUID id;
    private Long numero;
    private LocalDateTime dataInicio;
    private String pacienteNome;
    private String profissionalAtendimentoNome;
    private int procedimentosCount;
    private String convenioNome;
    private LocalDateTime createdAt;
    private Boolean deleted;
}
