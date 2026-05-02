package br.com.grupopipa.gestaointegrada.atendimento.lancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioRepository;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.ConvenioTipoCobrancaEnum;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiro;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroProcedimento;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.setor.SetorRepository;
import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.titulo.TituloRepository;
import br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaRepository;

@Service
public class LancamentoTituloServiceImpl implements LancamentoTituloService {

    private static final String CODIGO_CATEGORIA_ATEND = "ATEND";
    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TituloRepository tituloRepository;
    private final TituloCategoriaRepository tituloCategoriaRepository;
    private final PessoaRepository pessoaRepository;
    private final ConvenioRepository convenioRepository;
    private final SetorRepository setorRepository;

    public LancamentoTituloServiceImpl(
            TituloRepository tituloRepository,
            TituloCategoriaRepository tituloCategoriaRepository,
            PessoaRepository pessoaRepository,
            ConvenioRepository convenioRepository,
            SetorRepository setorRepository) {
        this.tituloRepository = tituloRepository;
        this.tituloCategoriaRepository = tituloCategoriaRepository;
        this.pessoaRepository = pessoaRepository;
        this.convenioRepository = convenioRepository;
        this.setorRepository = setorRepository;
    }

    @Override
    public Titulo gerarTitulo(LancamentoFinanceiro lancamento) {
        TituloCategoria categoria = buscarCategoria();
        Pessoa pessoa = resolverPessoa(lancamento);
        Setor setor = resolverSetor(lancamento);

        String descricao = "Atendimento #" + lancamento.getAtendimentoNumero();
        String numeroDocumento = lancamento.getAtendimentoNumero() != null
            ? String.valueOf(lancamento.getAtendimentoNumero()) : null;
        String observacoes = montarObservacoes(lancamento);

        Titulo titulo = new Titulo.Builder()
            .tipo(TipoTitulo.A_RECEBER)
            .descricao(descricao)
            .numeroDocumento(numeroDocumento)
            .pessoa(pessoa)
            .tituloCategoria(categoria)
            .unidadeNegocio(setor.getCentroCusto().getUnidadeNegocio())
            .valorOriginal(lancamento.getValorTotal() != null
                ? lancamento.getValorTotal() : BigDecimal.ZERO)
            .dataEmissao(LocalDate.now())
            .dataVencimento(LocalDate.now())
            .rateioAutomatico(false)
            .build();

        titulo.adicionarObservacao(observacoes);
        titulo.adicionarSetor(setor, BigDecimal.valueOf(100));
        titulo.validarSetores();

        return tituloRepository.save(titulo);
    }

    private TituloCategoria buscarCategoria() {
        return tituloCategoriaRepository.findByCodigo(CODIGO_CATEGORIA_ATEND)
            .orElseThrow(() -> {
                Set<BeanValidationMessage> violations = new HashSet<>();
                violations.add(new BeanValidationMessage(
                    "tituloCategoria",
                    "Categoria de título 'ATEND' não encontrada. "
                    + "Verifique se a migration foi executada."));
                return new BeanValidationException("lancamentoFinanceiro", violations);
            });
    }

    private Pessoa resolverPessoa(LancamentoFinanceiro lancamento) {
        if (lancamento.getConvenioTipoCobranca() == ConvenioTipoCobrancaEnum.FATURADO) {
            Convenio convenio = convenioRepository.findById(lancamento.getConvenioId())
                .orElseThrow(() -> {
                    Set<BeanValidationMessage> v = new HashSet<>();
                    v.add(new BeanValidationMessage("convenio", "Convênio não encontrado."));
                    return new BeanValidationException("lancamentoFinanceiro", v);
                });
            Pessoa pessoa = convenio.getPessoa();
            if (pessoa == null) {
                Set<BeanValidationMessage> v = new HashSet<>();
                v.add(new BeanValidationMessage(
                    "convenio", "Convênio faturado deve ter uma pessoa jurídica vinculada."));
                throw new BeanValidationException("lancamentoFinanceiro", v);
            }
            return pessoa;
        }

        return pessoaRepository.findById(lancamento.getPacienteId())
            .orElseThrow(() -> {
                Set<BeanValidationMessage> v = new HashSet<>();
                v.add(new BeanValidationMessage("paciente", "Paciente não encontrado."));
                return new BeanValidationException("lancamentoFinanceiro", v);
            });
    }

    private Setor resolverSetor(LancamentoFinanceiro lancamento) {
        if (lancamento.getSetorId() == null) {
            Set<BeanValidationMessage> violations = new HashSet<>();
            violations.add(new BeanValidationMessage(
                "setorId",
                "Setor do atendimento não informado. "
                + "Salve o atendimento com um setor antes de fechar o lançamento."));
            throw new BeanValidationException("lancamentoFinanceiro", violations);
        }
        Setor setor = setorRepository.findById(lancamento.getSetorId())
            .orElseThrow(() -> {
                Set<BeanValidationMessage> v = new HashSet<>();
                v.add(new BeanValidationMessage("setorId", "Setor não encontrado."));
                return new BeanValidationException("lancamentoFinanceiro", v);
            });
        if (setor.getCentroCusto() == null || setor.getCentroCusto().getUnidadeNegocio() == null) {
            Set<BeanValidationMessage> v = new HashSet<>();
            v.add(new BeanValidationMessage(
                "setorId",
                "Setor deve estar vinculado a um centro de custo com unidade de negócio."));
            throw new BeanValidationException("lancamentoFinanceiro", v);
        }
        return setor;
    }

    private String montarObservacoes(LancamentoFinanceiro lancamento) {
        StringBuilder sb = new StringBuilder();
        LocalDate dataAtend = lancamento.getDataAtendimento();
        String dataFormatada = dataAtend != null ? dataAtend.format(BR_DATE) : "—";

        sb.append("Atendimento #").append(lancamento.getAtendimentoNumero())
            .append(" — ").append(dataFormatada).append("\n");
        sb.append("Paciente: ").append(lancamento.getPacienteNome()).append("\n");
        sb.append("Convênio: ").append(
            lancamento.getConvenioNome() != null ? lancamento.getConvenioNome() : "Particular")
            .append("\n");

        if (lancamento.getProcedimentos() != null && !lancamento.getProcedimentos().isEmpty()) {
            sb.append("\nProcedimentos:\n");
            for (LancamentoFinanceiroProcedimento proc : lancamento.getProcedimentos()) {
                String nome = proc.getProcedimentoDescricao() != null
                    ? proc.getProcedimentoDescricao() : proc.getProcedimentoCodigo();
                String convenioProc = proc.getConvenioNome() != null
                    ? proc.getConvenioNome() : "Particular";
                String valor = proc.getValor() != null
                    ? String.format("R$ %.2f", proc.getValor()).replace(".", ",") : "R$ 0,00";
                sb.append("- ").append(nome).append(" (").append(convenioProc)
                    .append("): ").append(valor).append("\n");
            }
        }

        BigDecimal total = lancamento.getValorTotal() != null
            ? lancamento.getValorTotal() : BigDecimal.ZERO;
        sb.append("\nValor total: ")
            .append(String.format("R$ %.2f", total).replace(".", ","));

        return sb.toString();
    }
}
