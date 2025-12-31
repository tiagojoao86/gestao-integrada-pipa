package br.com.grupopipa.gestaointegrada.cadastro.setor;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetorDTO implements DTO {
  private UUID id;
  private String nome;
  private String descricao;
  private UUID centroCustoId;
  private String centroCustoNome;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;
}
