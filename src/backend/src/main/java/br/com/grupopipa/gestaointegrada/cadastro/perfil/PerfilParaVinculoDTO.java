package br.com.grupopipa.gestaointegrada.cadastro.perfil;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO simplificado para listagem de perfis em autocomplete/vinculação. Contém apenas os campos
 * essenciais (id e nome).
 */
@Builder
@Getter
@Setter
public class PerfilParaVinculoDTO {
  private UUID id;
  private String nome;
}
