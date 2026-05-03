package br.com.grupopipa.gestaointegrada.financeiro.caixa;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioCaixaDTO {
    private UUID usuarioId;
    private String usuarioNome;
    private String usuarioLogin;
}
