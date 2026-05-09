package br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.LancamentoFinanceiroRepository;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiro;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroSituacaoEnum;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroStatusFinanceiroEnum;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.cadastro.usuario.UsuarioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.usuario.entity.UsuarioEntity;
import br.com.grupopipa.gestaointegrada.core.Session;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.entity.AberturaCaixa;
import br.com.grupopipa.gestaointegrada.financeiro.caixa.CaixaRepository;
import br.com.grupopipa.gestaointegrada.financeiro.caixa.entity.Caixa;
import br.com.grupopipa.gestaointegrada.financeiro.contabancaria.ContaBancariaRepository;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.entity.MovimentacaoFinanceira;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;
import br.com.grupopipa.gestaointegrada.financeiro.movimentacao.MovimentacaoFinanceiraRepository;
import br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa.MovimentacaoCaixaGridDTO;
import br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa.MovimentacaoCaixaRepository;
import br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa.entity.MovimentacaoCaixa;
import br.com.grupopipa.gestaointegrada.financeiro.titulo.TituloRepository;

@Service
@Transactional(readOnly = true)
public class AberturaCaixaServiceImpl implements AberturaCaixaService {

    private final AberturaCaixaRepository repository;
    private final CaixaRepository caixaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final MovimentacaoCaixaRepository movimentacaoCaixaRepository;
    private final ContaBancariaRepository contaBancariaRepository;
    private final TituloRepository tituloRepository;
    private final MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    public AberturaCaixaServiceImpl(
            AberturaCaixaRepository repository,
            CaixaRepository caixaRepository,
            UsuarioRepository usuarioRepository,
            LancamentoFinanceiroRepository lancamentoRepository,
            MovimentacaoCaixaRepository movimentacaoCaixaRepository,
            ContaBancariaRepository contaBancariaRepository,
            TituloRepository tituloRepository,
            MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository) {
        this.repository = repository;
        this.caixaRepository = caixaRepository;
        this.usuarioRepository = usuarioRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.movimentacaoCaixaRepository = movimentacaoCaixaRepository;
        this.contaBancariaRepository = contaBancariaRepository;
        this.tituloRepository = tituloRepository;
        this.movimentacaoFinanceiraRepository = movimentacaoFinanceiraRepository;
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

        List<MovimentacaoCaixa> movimentacoes = movimentacaoCaixaRepository.findByAberturaCaixaId(id);
        validarContasParaFechamento(movimentacoes);
        gerarMovimentacoesFinanceiras(movimentacoes);

        abertura.fechar(request.getValorConferencia(), request.getObservacoes());
        return buildDTO(repository.save(abertura));
    }

    private void validarContasParaFechamento(List<MovimentacaoCaixa> movimentacoes) {
        List<String> erros = new ArrayList<>();
        for (MovimentacaoCaixa mov : movimentacoes) {
            FormaPagamento forma = mov.getFormaPagamento();
            if (contaBancariaRepository.findByFormaPagamento(forma).isEmpty()) {
                erros.add("Forma de pagamento '" + forma.getDescricao()
                        + "' não possui conta financeira configurada.");
            }
        }
        if (!erros.isEmpty()) {
            Set<BeanValidationMessage> msgs = erros.stream()
                    .map(e -> new BeanValidationMessage("contaBancaria", e))
                    .collect(Collectors.toSet());
            throw new BeanValidationException("aberturaCaixa", msgs);
        }
    }

    private void gerarMovimentacoesFinanceiras(List<MovimentacaoCaixa> movimentacoes) {
        LocalDate dataFechamento = LocalDate.now();

        for (MovimentacaoCaixa mov : movimentacoes) {
            if (mov.getTituloId() == null) {
                continue;
            }

            ContaBancaria conta = contaBancariaRepository
                    .findByFormaPagamento(mov.getFormaPagamento())
                    .get(0);

            Titulo titulo = tituloRepository.findById(mov.getTituloId())
                    .orElseThrow(() -> new BeanValidationException("aberturaCaixa",
                            Set.of(new BeanValidationMessage("tituloId",
                                    "Título não encontrado ao fechar caixa."))));

            MovimentacaoFinanceira movFinanceira = new MovimentacaoFinanceira.Builder()
                    .addTituloComValor(titulo, Money.of(mov.getValor()))
                    .contaBancaria(conta)
                    .tipo(TipoMovimentacao.RECEBIMENTO)
                    .formaPagamento(mov.getFormaPagamento())
                    .valor(Money.of(mov.getValor()))
                    .data(dataFechamento)
                    .movimentacaoCaixaId(mov.getId())
                    .build();

            movimentacaoFinanceiraRepository.save(movFinanceira);

            atualizarStatusLancamento(mov.getLancamentoId(), titulo);
        }
    }

    private void atualizarStatusLancamento(UUID lancamentoId, Titulo titulo) {
        if (lancamentoId == null) {
            return;
        }
        lancamentoRepository.findById(lancamentoId).ifPresent(lancamento -> {
            if (titulo.getStatus() == StatusTitulo.PAGO) {
                lancamento.marcarComoPago();
            } else {
                lancamento.marcarComoPagoParcial();
            }
            lancamentoRepository.save(lancamento);
        });
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

    @Override
    public CaixaComStatusDTO statusPorCaixa(UUID caixaId) {
        Caixa caixa = caixaRepository.findById(caixaId)
                .orElseThrow(() -> new BeanValidationException("aberturaCaixa",
                        Set.of(new BeanValidationMessage("caixaId", "Caixa não encontrado."))));
        return buildCaixaComStatus(caixa);
    }

    @Override
    public List<LancamentoFinanceiroGridDTO> listarLancamentosPendentes(UUID aberturaCaixaId) {
        AberturaCaixa abertura = repository.findById(aberturaCaixaId)
                .orElseThrow(() -> new BeanValidationException("aberturaCaixa",
                        Set.of(new BeanValidationMessage("id", "Sessão de caixa não encontrada."))));

        UnidadeNegocio unidade = abertura.getCaixa().getUnidadeNegocio();
        if (unidade == null) {
            return List.of();
        }

        List<LancamentoFinanceiroStatusFinanceiroEnum> statusPermitidos = List.of(
                LancamentoFinanceiroStatusFinanceiroEnum.PENDENTE,
                LancamentoFinanceiroStatusFinanceiroEnum.PAGO_PARCIAL);

        return lancamentoRepository
                .findPendentesParaRecebimento(
                        LancamentoFinanceiroSituacaoEnum.FECHADO,
                        statusPermitidos,
                        unidade.getId())
                .stream()
                .map(this::toLancamentoGrid)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimentacaoCaixaGridDTO> listarMovimentacoes(UUID aberturaCaixaId) {
        return movimentacaoCaixaRepository.findByAberturaCaixaId(aberturaCaixaId)
                .stream()
                .map(this::toMovimentacaoGrid)
                .collect(Collectors.toList());
    }

    private LancamentoFinanceiroGridDTO toLancamentoGrid(LancamentoFinanceiro l) {
        return LancamentoFinanceiroGridDTO.builder()
                .id(l.getId())
                .atendimentoNumero(l.getAtendimentoNumero())
                .dataAtendimento(l.getDataAtendimento())
                .pacienteNome(l.getPacienteNome())
                .convenioNome(l.getConvenioNome())
                .valorTotal(l.getValorTotal())
                .situacao(l.getSituacao())
                .statusFinanceiro(l.getStatusFinanceiro())
                .build();
    }

    private MovimentacaoCaixaGridDTO toMovimentacaoGrid(MovimentacaoCaixa m) {
        return MovimentacaoCaixaGridDTO.builder()
                .id(m.getId())
                .valor(m.getValor())
                .formaPagamento(m.getFormaPagamento() != null ? m.getFormaPagamento().name() : null)
                .formaPagamentoDescricao(
                        m.getFormaPagamento() != null ? m.getFormaPagamento().getDescricao() : null)
                .dataHora(m.getDataHora())
                .observacoes(m.getObservacoes())
                .lancamentoId(m.getLancamentoId())
                .build();
    }

    private CaixaComStatusDTO buildCaixaComStatus(Caixa caixa) {
        Optional<AberturaCaixa> abertura =
                repository.findByCaixaIdAndStatus(caixa.getId(), StatusAberturaCaixa.ABERTO);

        var unidade = caixa.getUnidadeNegocio();
        return CaixaComStatusDTO.builder()
                .caixaId(caixa.getId())
                .caixaNome(caixa.getNome())
                .valorPadraoAbertura(caixa.getValorPadraoAbertura())
                .statusSessao(abertura.map(AberturaCaixa::getStatus).orElse(null))
                .aberturaCaixaId(abertura.map(AberturaCaixa::getId).orElse(null))
                .dataAbertura(abertura.map(AberturaCaixa::getDataAbertura).orElse(null))
                .usuarioNomeAbertura(abertura.map(AberturaCaixa::getUsuarioNome).orElse(null))
                .unidadeNegocioId(unidade != null ? unidade.getId() : null)
                .unidadeNegocioNome(unidade != null ? unidade.getNome() : null)
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
