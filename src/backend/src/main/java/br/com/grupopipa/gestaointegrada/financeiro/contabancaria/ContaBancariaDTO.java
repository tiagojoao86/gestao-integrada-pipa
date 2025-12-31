package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ContaBancariaDTO implements DTO {

  private UUID id;
  private String nome;
  private String banco;
  private String agencia;
  private String numeroConta;
  private String tipo; // CORRENTE, POUPANCA, CAIXA, INVESTIMENTO
  private BigDecimal saldoInicial;
  private UUID unidadeNegocioId;
  private Boolean ativa;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;
}
