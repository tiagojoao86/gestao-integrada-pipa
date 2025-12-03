package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.PessoaFisica;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.PessoaJuridica;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.core.valueobject.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class PessoaServiceImpl extends CrudServiceImpl<PessoaDTO, PessoaGridDTO, Pessoa, PessoaRepository>
        implements PessoaService {

    public PessoaServiceImpl(PessoaRepository repository, Specifications<Pessoa> specifications) {
        super(repository, specifications);
    }

    @Override
    protected Pessoa mergeEntityAndDTO(Pessoa entity, PessoaDTO dto) {
        Email email = dto.getEmail() != null && !dto.getEmail().isBlank() 
            ? new Email(dto.getEmail()) : null;
        PhoneNumber telefone = dto.getTelefone() != null && !dto.getTelefone().isBlank() 
            ? new PhoneNumber(dto.getTelefone()) : null;

        if (Objects.isNull(entity)) {
            // Criar nova pessoa
            if ("FISICA".equals(dto.getTipoPessoa())) {
                CPF cpf = new CPF(dto.getCpf());
                entity = new PessoaFisica(dto.getNome(), email, telefone, cpf, dto.getDataNascimento());
            } else if ("JURIDICA".equals(dto.getTipoPessoa())) {
                CNPJ cnpj = new CNPJ(dto.getCnpj());
                PessoaJuridica pj = new PessoaJuridica(dto.getNome(), email, telefone, cnpj, dto.getRazaoSocial());
                if (dto.getNomeFantasia() != null || dto.getInscricaoEstadual() != null) {
                    pj.atualizarDados(dto.getRazaoSocial(), dto.getNomeFantasia(), dto.getInscricaoEstadual());
                }
                entity = pj;
            } else {
                throw new IllegalArgumentException("Tipo de pessoa inválido: " + dto.getTipoPessoa());
            }
            
            if (dto.getObservacoes() != null && !dto.getObservacoes().isBlank()) {
                entity.adicionarObservacao(dto.getObservacoes());
            }
            
            return entity;
        }

        // Atualizar pessoa existente
        entity.atualizar(dto.getNome(), email, telefone);
        
        if (entity instanceof PessoaFisica pf) {
            pf.definirDataNascimento(dto.getDataNascimento());
        } else if (entity instanceof PessoaJuridica pj) {
            pj.atualizarDados(dto.getRazaoSocial(), dto.getNomeFantasia(), dto.getInscricaoEstadual());
        }
        
        if (dto.getAtiva() != null) {
            if (dto.getAtiva()) {
                entity.ativar();
            } else {
                entity.inativar();
            }
        }

        return entity;
    }

    @Override
    protected PessoaDTO buildDTOFromEntity(Pessoa entity) {
        PessoaDTO.PessoaDTOBuilder builder = PessoaDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .email(entity.getEmail() != null ? entity.getEmail().getValue() : null)
                .telefone(entity.getTelefone() != null ? entity.getTelefone().getValue() : null)
                .observacoes(entity.getObservacoes())
                .ativa(entity.getAtiva())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy());

        if (entity instanceof PessoaFisica pf) {
            builder.tipoPessoa("FISICA")
                    .cpf(pf.getCpf().getValue())
                    .dataNascimento(pf.getDataNascimento());
        } else if (entity instanceof PessoaJuridica pj) {
            builder.tipoPessoa("JURIDICA")
                    .cnpj(pj.getCnpj().getValue())
                    .razaoSocial(pj.getRazaoSocial())
                    .nomeFantasia(pj.getNomeFantasia())
                    .inscricaoEstadual(pj.getInscricaoEstadual());
        }

        return builder.build();
    }

    @Override
    protected PessoaGridDTO buildGridDTOFromEntity(Pessoa entity) {
        String documento = "";
        String tipoPessoa = "";
        
        if (entity instanceof PessoaFisica pf) {
            documento = pf.getCpf().getFormatted();
            tipoPessoa = "FISICA";
        } else if (entity instanceof PessoaJuridica pj) {
            documento = pj.getCnpj().getFormatted();
            tipoPessoa = "JURIDICA";
        }

        return PessoaGridDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .documento(documento)
                .tipoPessoa(tipoPessoa)
                .ativa(entity.getAtiva())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "ativa", "createdAt");
    }

    @Override
    protected Class<Pessoa> getEntityClass() {
        return Pessoa.class;
    }
}
