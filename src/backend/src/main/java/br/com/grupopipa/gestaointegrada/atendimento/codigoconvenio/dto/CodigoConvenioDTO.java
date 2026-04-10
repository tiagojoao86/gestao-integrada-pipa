package br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.dto;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CodigoConvenioDTO implements DTO {

    private UUID id;
    private UUID convenioId;
    private UUID procedimentoId;
    private String procedimentoCodigo;
    private String procedimentoDescricao;
    private String codigo;
}
