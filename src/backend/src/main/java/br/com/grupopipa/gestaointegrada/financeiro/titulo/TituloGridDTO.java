package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
public class TituloGridDTO implements GridDTO {
    private UUID id;
    private String tipo;
    private String status;
    private String numeroDocumento;
    private String descricao;
    private String pessoaNome;
    private BigDecimal valorOriginal;
    private BigDecimal saldo;
    private LocalDate dataVencimento;
    private String parcelamento; // "3/12" se for parcelado
}
