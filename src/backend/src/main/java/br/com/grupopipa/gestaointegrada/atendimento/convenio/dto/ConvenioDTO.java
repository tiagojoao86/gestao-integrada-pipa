package br.com.grupopipa.gestaointegrada.atendimento.convenio.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.dto.CodigoConvenioDTO;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.ConvenioTipoCobrancaEnum;
import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ConvenioDTO implements DTO {

    private UUID id;
    private String nome;

    private UUID pessoaId;
    private String pessoaNome;

    private String registroAns;
    private Boolean ativo;
    private ConvenioTipoCobrancaEnum tipoCobranca;

    private List<CodigoConvenioDTO> codigos;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
