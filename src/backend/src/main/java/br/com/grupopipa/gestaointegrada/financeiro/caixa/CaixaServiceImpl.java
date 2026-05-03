package br.com.grupopipa.gestaointegrada.financeiro.caixa;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.cadastro.usuario.UsuarioRepository;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.financeiro.caixa.entity.Caixa;

@Service
public class CaixaServiceImpl
        extends CrudServiceImpl<CaixaDTO, CaixaGridDTO, Caixa, CaixaRepository>
        implements CaixaService {

    private final UsuarioRepository usuarioRepository;

    public CaixaServiceImpl(
            CaixaRepository repository,
            Specifications<Caixa> specifications,
            UsuarioRepository usuarioRepository) {
        super(repository, specifications);
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected Caixa mergeEntityAndDTO(Caixa entity, CaixaDTO dto) {
        if (Objects.isNull(entity)) {
            return criarCaixa(dto);
        }
        return atualizarCaixa(entity, dto);
    }

    private Caixa criarCaixa(CaixaDTO dto) {
        return new Caixa.Builder()
                .nome(dto.getNome())
                .valorPadraoAbertura(defaultZero(dto.getValorPadraoAbertura()))
                .percentualPagamentoParcial(dto.getPercentualPagamentoParcial())
                .valorMinimoParcela(dto.getValorMinimoParcela())
                .build();
    }

    private Caixa atualizarCaixa(Caixa entity, CaixaDTO dto) {
        entity.atualizar(
                dto.getNome(),
                defaultZero(dto.getValorPadraoAbertura()),
                dto.getPercentualPagamentoParcial(),
                dto.getValorMinimoParcela());

        if (Boolean.TRUE.equals(dto.getAtivo())) {
            entity.ativar();
        } else if (Boolean.FALSE.equals(dto.getAtivo())) {
            entity.inativar();
        }

        return entity;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Override
    protected CaixaDTO buildDTOFromEntity(Caixa entity) {
        return CaixaDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .valorPadraoAbertura(entity.getValorPadraoAbertura())
                .percentualPagamentoParcial(entity.getPercentualPagamentoParcial())
                .valorMinimoParcela(entity.getValorMinimoParcela())
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected CaixaGridDTO buildGridDTOFromEntity(Caixa entity) {
        return CaixaGridDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .valorPadraoAbertura(entity.getValorPadraoAbertura())
                .percentualParcialConfigurado(entity.getPercentualPagamentoParcial() != null)
                .ativo(entity.getAtivo())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "ativo");
    }

    @Override
    protected Class<Caixa> getEntityClass() {
        return Caixa.class;
    }

    @Override
    public List<UsuarioCaixaDTO> listarUsuarios(UUID caixaId) {
        Caixa caixa = repository.findById(caixaId)
                .orElseThrow(() -> new BeanValidationException("caixa",
                        Set.of(new BeanValidationMessage("id", "Caixa não encontrado."))));
        if (caixa.getUsuarioIds().isEmpty()) {
            return List.of();
        }
        return usuarioRepository.findAllById(caixa.getUsuarioIds()).stream()
                .map(u -> UsuarioCaixaDTO.builder()
                        .usuarioId(u.getId())
                        .usuarioNome(u.getNome())
                        .usuarioLogin(u.getLogin())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void atualizarUsuarios(UUID caixaId, List<UUID> usuarioIds) {
        Caixa caixa = repository.findById(caixaId)
                .orElseThrow(() -> new BeanValidationException("caixa",
                        Set.of(new BeanValidationMessage("id", "Caixa não encontrado."))));
        caixa.setUsuarioIds(usuarioIds != null ? new HashSet<>(usuarioIds) : new HashSet<>());
        repository.save(caixa);
    }

    @Override
    public List<CaixaGridDTO> listarTodosAtivos() {
        return repository.findAllAtivos().stream()
                .map(this::buildGridDTOFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UUID> listarCaixasPorUsuario(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(Caixa::getId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void atualizarCaixasDoUsuario(UUID usuarioId, List<UUID> caixaIds) {
        List<Caixa> caixasAtuais = repository.findByUsuarioId(usuarioId);
        caixasAtuais.forEach(c -> c.getUsuarioIds().remove(usuarioId));
        repository.saveAll(caixasAtuais);

        if (caixaIds != null && !caixaIds.isEmpty()) {
            List<Caixa> caixasNovas = repository.findAllById(caixaIds);
            caixasNovas.forEach(c -> c.getUsuarioIds().add(usuarioId));
            repository.saveAll(caixasNovas);
        }
    }
}
