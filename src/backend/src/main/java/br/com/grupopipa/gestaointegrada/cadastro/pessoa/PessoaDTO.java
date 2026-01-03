package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PessoaDTO implements DTO {

    private UUID id;
    private String nome;
    private String email;
    private String telefone;
    private String observacoes;
    private Boolean ativa;

    // Pessoa Física
    private String cpf;
    private LocalDate dataNascimento;

    // Pessoa Jurídica
    private String cnpj;
    private String razaoSocial;
    private String inscricaoEstadual;

    // Tipo para identificar se é PF ou PJ
    private String tipoPessoa; // "FISICA" ou "JURIDICA"

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
