package br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.cadastro.usuario.UsuarioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.usuario.entity.UsuarioEntity;
import br.com.grupopipa.gestaointegrada.core.Session;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.entity.AberturaCaixa;
import br.com.grupopipa.gestaointegrada.financeiro.caixa.CaixaRepository;
import br.com.grupopipa.gestaointegrada.financeiro.caixa.entity.Caixa;

@Service
@Transactional(readOnly = true)
public class AberturaCaixaServiceImpl implements AberturaCaixaService {

    private final AberturaCaixaRepository repository;
    private final CaixaRepository caixaRepository;
    private final UsuarioRepository usuarioRepository;

    public AberturaCaixaServiceImpl(
            AberturaCaixaRepository repository,
            CaixaRepository caixaRepository,
            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.caixaRepository = caixaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public AberturaCaixaDTO abrir(AbrirCaixaRequest request) {
        validarRequestAbrir(request);

        Caixa caixa = buscarCaixa(request.getCaixaId());
        UsuarioEntity usuario = buscarUsuarioAtual();
        validarAcessoAoCaixa(caixa, usuario);
        validarSemSessaoAberta(request.getCaixaId());

        AberturaCaixa abertura = new AberturaCaixa.Builder()
                .caixa(caixa)
                .usuarioId(usuario.getId())
                .usuarioNome(usuario.getNome())
                .valorAbertura(
                        request.getValorAbertura() != null
                                ? request.getValorAbertura()
                                : caixa.getValorPadraoAbertura())
                .build();

        return buildDTO(repository.save(abertura));
    }

    @Override
    @Transactional
    public AberturaCaixaDTO fechar(UUID id, FecharCaixaRequest request) {
        AberturaCaixa abertura = repository.findById(id)
                .orElseThrow(() -> new BeanValidationException("aberturaCaixa",
                        Set.of(new BeanValidationMessage("id", "Sessão de caixa não encontrada."))));

        if (abertura.getStatus() != StatusAberturaCaixa.ABERTO) {
            throw new BeanValidationException("aberturaCaixa",
                    Set.of(new BeanValidationMessage("status", "Esta sessão não está aberta.")));
        }

        abertura.fechar(request.getValorConferencia(), request.getObservacoes());
        return buildDTO(repository.save(abertura));
    }

    @Override
    public Optional<AberturaCaixaDTO> findAtivaByCaixaId(UUID caixaId) {
        return repository.findByCaixaIdAndStatus(caixaId, StatusAberturaCaixa.ABERTO)
                .map(this::buildDTO);
    }

    @Override
    public List<CaixaComStatusDTO> listarMeusCaixas() {
        UsuarioEntity usuario = buscarUsuarioAtual();
        List<Caixa> caixas = caixaRepository.findByUsuarioId(usuario.getId());

        return caixas.stream()
                .map(caixa -> buildCaixaComStatus(caixa))
                .collect(Collectors.toList());
    }

    private void validarRequestAbrir(AbrirCaixaRequest request) {
        if (request.getCaixaId() == null) {
            throw new BeanValidationException("aberturaCaixa",
                    Set.of(new BeanValidationMessage("caixaId", "Caixa é obrigatório.")));
        }
    }

    private Caixa buscarCaixa(UUID caixaId) {
        return caixaRepository.findById(caixaId)
                .orElseThrow(() -> new BeanValidationException("aberturaCaixa",
                        Set.of(new BeanValidationMessage("caixaId", "Caixa não encontrado."))));
    }

    private UsuarioEntity buscarUsuarioAtual() {
        String login = Session.getUsuarioUsername();
        return usuarioRepository.findUsuarioByLoginValue(login)
                .orElseThrow(() -> new BeanValidationException("aberturaCaixa",
                        Set.of(new BeanValidationMessage("usuario", "Usuário não encontrado."))));
    }

    private void validarAcessoAoCaixa(Caixa caixa, UsuarioEntity usuario) {
        if (!caixa.getUsuarioIds().contains(usuario.getId())) {
            throw new BeanValidationException("aberturaCaixa",
                    Set.of(new BeanValidationMessage("caixaId",
                            "Você não tem acesso a este caixa.")));
        }
    }

    private void validarSemSessaoAberta(UUID caixaId) {
        repository.findByCaixaIdAndStatus(caixaId, StatusAberturaCaixa.ABERTO)
                .ifPresent(a -> {
                    throw new BeanValidationException("aberturaCaixa",
                            Set.of(new BeanValidationMessage("caixaId",
                                    "Este caixa já possui uma sessão aberta.")));
                });
    }

    private CaixaComStatusDTO buildCaixaComStatus(Caixa caixa) {
        Optional<AberturaCaixa> abertura =
                repository.findByCaixaIdAndStatus(caixa.getId(), StatusAberturaCaixa.ABERTO);

        return CaixaComStatusDTO.builder()
                .caixaId(caixa.getId())
                .caixaNome(caixa.getNome())
                .valorPadraoAbertura(caixa.getValorPadraoAbertura())
                .statusSessao(abertura.map(AberturaCaixa::getStatus).orElse(null))
                .aberturaCaixaId(abertura.map(AberturaCaixa::getId).orElse(null))
                .dataAbertura(abertura.map(AberturaCaixa::getDataAbertura).orElse(null))
                .usuarioNomeAbertura(abertura.map(AberturaCaixa::getUsuarioNome).orElse(null))
                .build();
    }

    private AberturaCaixaDTO buildDTO(AberturaCaixa abertura) {
        return AberturaCaixaDTO.builder()
                .id(abertura.getId())
                .caixaId(abertura.getCaixa().getId())
                .caixaNome(abertura.getCaixa().getNome())
                .usuarioId(abertura.getUsuarioId())
                .usuarioNome(abertura.getUsuarioNome())
                .status(abertura.getStatus())
                .dataAbertura(abertura.getDataAbertura())
                .dataFechamento(abertura.getDataFechamento())
                .valorAbertura(abertura.getValorAbertura())
                .valorConferencia(abertura.getValorConferencia())
                .observacoes(abertura.getObservacoes())
                .build();
    }
}
