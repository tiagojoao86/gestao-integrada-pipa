package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PessoaGridDTO implements GridDTO {
    private UUID id;
    private String nome;
    private String documento; // CPF ou CNPJ formatado
    private String tipoPessoa;
    private Boolean ativa;
    private LocalDateTime createdAt;
    private Boolean deleted;
}
