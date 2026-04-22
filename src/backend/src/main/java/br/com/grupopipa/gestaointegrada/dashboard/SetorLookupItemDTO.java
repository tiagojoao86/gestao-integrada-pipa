package br.com.grupopipa.gestaointegrada.dashboard;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetorLookupItemDTO {
    private UUID id;
    private String nome;
}
