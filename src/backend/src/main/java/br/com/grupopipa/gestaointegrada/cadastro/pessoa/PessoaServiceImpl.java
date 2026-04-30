package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
public class PessoaServiceImpl
        extends CrudServiceImpl<PessoaDTO, PessoaGridDTO, Pessoa, PessoaRepository>
        implements PessoaService {

    public PessoaServiceImpl(PessoaRepository repository, Specifications<Pessoa> specifications) {
        super(repository, specifications);
    }

    @Override
    protected Pessoa mergeEntityAndDTO(Pessoa entity, PessoaDTO dto) {
        Pessoa responsavel = resolverResponsavel(dto.getResponsavelId());

        if (Objects.isNull(entity)) {
            TipoPessoa tipo = TipoPessoa.valueOf(dto.getTipoPessoa());

            Pessoa.Builder builder = new Pessoa.Builder()
                    .tipoPessoa(tipo)
                    .nome(dto.getNome())
                    .email(dto.getEmail())
                    .telefone(dto.getTelefone())
                    .responsavel(responsavel);

            if (tipo == TipoPessoa.FISICA) {
                builder.cpf(dto.getCpf()).dataNascimento(dto.getDataNascimento());
            } else if (tipo == TipoPessoa.JURIDICA) {
                builder
                        .cnpj(dto.getCnpj())
                        .razaoSocial(dto.getRazaoSocial())
                        .inscricaoEstadual(dto.getInscricaoEstadual());
            }

            if (dto.getObservacoes() != null && !dto.getObservacoes().isBlank()) {
                builder.observacoes(dto.getObservacoes());
            }

            if (dto.getAtiva() != null) {
                builder.ativa(dto.getAtiva());
            }

            builder.enderecoCEP(dto.getEnderecoCep())
                    .enderecoLogradouro(dto.getEnderecoLogradouro())
                    .enderecoNumero(dto.getEnderecoNumero())
                    .enderecoComplemento(dto.getEnderecoComplemento())
                    .enderecoBairro(dto.getEnderecoBairro())
                    .enderecoCidade(dto.getEnderecoCidade())
                    .enderecoUF(dto.getEnderecoUf());

            return builder.build();
        }

        entity.atualizar(
                dto.getNome(),
                dto.getEmail(),
                dto.getTelefone(),
                dto.getCpf(),
                dto.getDataNascimento(),
                dto.getCnpj(),
                dto.getRazaoSocial(),
                dto.getInscricaoEstadual(),
                responsavel);

        entity.atualizarEndereco(
                dto.getEnderecoCep(),
                dto.getEnderecoLogradouro(),
                dto.getEnderecoNumero(),
                dto.getEnderecoComplemento(),
                dto.getEnderecoBairro(),
                dto.getEnderecoCidade(),
                dto.getEnderecoUf());

        if (dto.getAtiva() != null) {
            if (dto.getAtiva()) {
                entity.ativar();
            } else {
                entity.inativar();
            }
        }

        return entity;
    }

    private Pessoa resolverResponsavel(UUID responsavelId) {
        if (responsavelId == null) {
            return null;
        }
        return repository.findById(responsavelId).orElse(null);
    }

    @Override
    protected PessoaDTO buildDTOFromEntity(Pessoa entity) {
        return PessoaDTO.builder()
                .id(entity.getId())
                .tipoPessoa(entity.getTipoPessoa().name())
                .nome(entity.getNome())
                .email(entity.getEmail())
                .telefone(entity.getTelefone())
                .cpf(entity.getCpf())
                .dataNascimento(entity.getDataNascimento())
                .cnpj(entity.getCnpj())
                .razaoSocial(entity.getRazaoSocial())
                .inscricaoEstadual(entity.getInscricaoEstadual())
                .observacoes(entity.getObservacoes())
                .ativa(entity.getAtiva())
                .enderecoCep(entity.getEnderecoCep())
                .enderecoLogradouro(entity.getEnderecoLogradouro())
                .enderecoNumero(entity.getEnderecoNumero())
                .enderecoComplemento(entity.getEnderecoComplemento())
                .enderecoBairro(entity.getEnderecoBairro())
                .enderecoCidade(entity.getEnderecoCidade())
                .enderecoUf(entity.getEnderecoUF())
                .responsavelId(entity.getResponsavelId())
                .responsavelNome(entity.getResponsavelNome())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected PessoaGridDTO buildGridDTOFromEntity(Pessoa entity) {
        String documento = entity.isPessoaFisica()
                ? formatCpf(entity.getCpf())
                : formatCnpj(entity.getCnpj());

        return PessoaGridDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .documento(documento)
                .tipoPessoa(entity.getTipoPessoa().name())
                .ativa(entity.getAtiva())
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    private static String formatCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    private static String formatCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) return cnpj;
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "."
                + cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "ativa", "createdAt", "tipoPessoa", "cpf", "cnpj", "dataNascimento");
    }

    @Override
    protected Class<Pessoa> getEntityClass() {
        return Pessoa.class;
    }

    @Override
    public List<PessoaDTO> listarParaVinculo() {
        return repository.findByAtivaTrue().stream()
                .map(
                        pessoa -> PessoaDTO.builder()
                                .id(pessoa.getId())
                                .nome(pessoa.getNome())
                                .tipoPessoa(pessoa.getTipoPessoa().name())
                                .cpf(pessoa.getCpf())
                                .cnpj(pessoa.getCnpj())
                                .build())
                .toList();
    }
}
