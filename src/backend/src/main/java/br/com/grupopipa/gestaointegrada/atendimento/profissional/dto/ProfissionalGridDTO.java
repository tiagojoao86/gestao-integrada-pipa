package br.com.grupopipa.gestaointegrada.atendimento.profissional.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ProfissionalGridDTO implements GridDTO {
    private UUID id;
    private String pessoaNome;
    private String conselho;
    private String codigoConselho;
    private String tipoRemuneracao;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private Boolean deleted;
}
