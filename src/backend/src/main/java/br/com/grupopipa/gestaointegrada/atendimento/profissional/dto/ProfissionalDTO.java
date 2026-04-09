package br.com.grupopipa.gestaointegrada.atendimento.profissional.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProfissionalDTO implements DTO {

    private UUID id;

    private UUID pessoaId;
    private String pessoaNome;

    private String conselho;
    private String codigoConselho;
    private String tipoRemuneracao;

    private String banco;
    private String conta;
    private String chavePix;

    private Boolean ativo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
