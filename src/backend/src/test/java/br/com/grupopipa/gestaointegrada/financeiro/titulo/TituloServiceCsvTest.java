package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.setor.SetorRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioService;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.Session;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.condicaopagamento.CondicaoPagamentoRepository;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.planocontas.PlanoContasRepository;
import br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaRepository;

/**
 * Testes unitários para a geração de CSV em TituloServiceImpl.
 * Cobre: headers, mapeamento de campos, formatação de data,
 * escaping de valores e estrutura do arquivo gerado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TituloService - Geração de CSV")
class TituloServiceCsvTest {

    @Mock
    private TituloRepository repository;
    @Mock
    private Specifications<Titulo> specifications;
    @Mock
    private PessoaRepository pessoaRepository;
    @Mock
    private PlanoContasRepository planoContasRepository;
    @Mock
    private UnidadeNegocioRepository unidadeNegocioRepository;
    @Mock
    private UnidadeNegocioService unidadeNegocioService;
    @Mock
    private SetorRepository setorRepository;
    @Mock
    private TituloCategoriaRepository tituloCategoriaRepository;
    @Mock
    private CondicaoPagamentoRepository condicaoPagamentoRepository;

    @InjectMocks
    private TituloServiceImpl service;

    private TituloGridDTO gridDto;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() {
        gridDto = TituloGridDTO.builder()
                .tipo("A_PAGAR")
                .status("ABERTO")
                .numeroDocumento("NF-001")
                .descricao("Fornecedor X")
                .pessoaNome("Empresa LTDA")
                .tituloCategoriaNome("Fornecedores")
                .unidadeNegocioCodigo("UN1")
                .valorOriginal(new BigDecimal("1500.50"))
                .valorPago(new BigDecimal("500.00"))
                .saldo(new BigDecimal("1000.50"))
                .dataVencimento(LocalDate.of(2026, 3, 15))
                .parcelamento("1/3")
                .build();

        pageRequest = PageRequest.builder()
                .filter(null)
                .order(List.of())
                .page(0)
                .size(1000)
                .build();
    }

    // ── getCsvHeaders() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar exatamente 12 colunas com os nomes corretos")
    void deveCsvHeadersConterColunasCorretas() {
        String[] headers = service.getCsvHeaders();

        assertThat(headers).containsExactly(
                "Tipo", "Status", "Nº Documento", "Descrição",
                "Pessoa", "Categoria", "Unidade", "Valor Original",
                "Valor Pago", "Saldo", "Vencimento", "Parcelamento");
    }

    // ── buildCsvRow() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve mapear todos os campos do GridDTO nas posições corretas")
    void deveBuildCsvRowMapearTodosOsCampos() {
        String[] row = service.buildCsvRow(gridDto);

        assertThat(row).hasSize(12);
        assertThat(row[0]).isEqualTo("A_PAGAR");
        assertThat(row[1]).isEqualTo("ABERTO");
        assertThat(row[2]).isEqualTo("NF-001");
        assertThat(row[3]).isEqualTo("Fornecedor X");
        assertThat(row[4]).isEqualTo("Empresa LTDA");
        assertThat(row[5]).isEqualTo("Fornecedores");
        assertThat(row[6]).isEqualTo("UN1");
        assertThat(row[7]).isEqualTo("1500.50");
        assertThat(row[8]).isEqualTo("500.00");
        assertThat(row[9]).isEqualTo("1000.50");
        assertThat(row[10]).isEqualTo("15/03/2026");
        assertThat(row[11]).isEqualTo("1/3");
    }

    @Test
    @DisplayName("Deve formatar a data de vencimento no padrão dd/MM/yyyy")
    void deveFormatarDataNoFormatoBrasileiro() {
        TituloGridDTO dto = TituloGridDTO.builder()
                .tipo("A_PAGAR").status("ABERTO").descricao("Teste")
                .pessoaNome("P").unidadeNegocioCodigo("U")
                .valorOriginal(BigDecimal.ONE).valorPago(BigDecimal.ZERO)
                .saldo(BigDecimal.ONE)
                .dataVencimento(LocalDate.of(2026, 12, 31))
                .build();

        assertThat(service.buildCsvRow(dto)[10]).isEqualTo("31/12/2026");
    }

    @Test
    @DisplayName("Deve retornar string vazia para campos monetários nulos")
    void deveRetornarStringVaziaParaValoresMonetariosNulos() {
        TituloGridDTO dto = TituloGridDTO.builder()
                .tipo("A_RECEBER").status("ABERTO").descricao("Sem valores")
                .pessoaNome("Cliente").unidadeNegocioCodigo("UN2")
                .dataVencimento(LocalDate.of(2026, 6, 1))
                .build(); // valorOriginal, valorPago, saldo = null

        String[] row = service.buildCsvRow(dto);

        assertThat(row[7]).isEmpty(); // valorOriginal
        assertThat(row[8]).isEmpty(); // valorPago
        assertThat(row[9]).isEmpty(); // saldo
    }

    // ── exportToCsv() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve iniciar o arquivo com BOM UTF-8 e linha de cabeçalho")
    void deveExportarCsvComBomECabecalho() {
        try (MockedStatic<Session> sessionMock = Mockito.mockStatic(Session.class)) {
            sessionMock.when(Session::getUnidadeNegocioIds)
                    .thenReturn(Set.of(UUID.randomUUID()));
            when(repository.findAll(any(Specification.class), any(Sort.class)))
                    .thenReturn(List.of());

            byte[] csv = service.exportToCsv(pageRequest);
            String content = new String(csv, StandardCharsets.UTF_8);
            String firstLine = content.lines().findFirst().orElse("");

            assertThat(content).startsWith("\uFEFF");
            assertThat(firstLine).contains("Tipo;Status;Nº Documento;Descrição");
            assertThat(firstLine).contains("Valor Original;Valor Pago;Saldo;Vencimento");
        }
    }

    @Test
    @DisplayName("Deve gerar linha de dados separada por ponto-e-vírgula")
    void deveExportarCsvComLinhaDeDadosCorretamente() {
        Titulo mockTitulo = buildMockTitulo("Fornecedor X");
        try (MockedStatic<Session> sessionMock = Mockito.mockStatic(Session.class)) {
            sessionMock.when(Session::getUnidadeNegocioIds)
                    .thenReturn(Set.of(UUID.randomUUID()));
            when(repository.findAll(any(Specification.class), any(Sort.class)))
                    .thenReturn(List.of(mockTitulo));

            byte[] csv = service.exportToCsv(pageRequest);
            String[] lines = new String(csv, StandardCharsets.UTF_8).split("\n");

            // lines[0] = header, lines[1] = primeira linha de dados
            assertThat(lines).hasSizeGreaterThan(1);
            assertThat(lines[1]).contains("A_PAGAR;ABERTO;NF-001;Fornecedor X;Empresa LTDA");
            assertThat(lines[1]).contains("1500.50");
            assertThat(lines[1]).contains("15/03/2026");
        }
    }

    @Test
    @DisplayName("Deve colocar entre aspas duplas valores que contenham ponto-e-vírgula")
    void deveEscaparValoresComPontoEVirgula() {
        Titulo mockTitulo = buildMockTitulo("Pagamento; parcela extra");
        try (MockedStatic<Session> sessionMock = Mockito.mockStatic(Session.class)) {
            sessionMock.when(Session::getUnidadeNegocioIds)
                    .thenReturn(Set.of(UUID.randomUUID()));
            when(repository.findAll(any(Specification.class), any(Sort.class)))
                    .thenReturn(List.of(mockTitulo));

            byte[] csv = service.exportToCsv(pageRequest);
            String content = new String(csv, StandardCharsets.UTF_8);

            assertThat(content).contains("\"Pagamento; parcela extra\"");
        }
    }

    @Test
    @DisplayName("Não deve conter a string literal 'null' no CSV gerado")
    void naoDeveConterStringNullNoCsv() {
        Titulo mockTitulo = buildMockTitulo("Titulo sem categoria");
        try (MockedStatic<Session> sessionMock = Mockito.mockStatic(Session.class)) {
            sessionMock.when(Session::getUnidadeNegocioIds)
                    .thenReturn(Set.of(UUID.randomUUID()));
            when(repository.findAll(any(Specification.class), any(Sort.class)))
                    .thenReturn(List.of(mockTitulo));

            byte[] csv = service.exportToCsv(pageRequest);
            String content = new String(csv, StandardCharsets.UTF_8);

            assertThat(content).doesNotContain(";null;");
        }
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private Titulo buildMockTitulo(String descricao) {
        Titulo mockTitulo = mock(Titulo.class);
        Pessoa mockPessoa = mock(Pessoa.class);
        UnidadeNegocio mockUnidade = mock(UnidadeNegocio.class);
        Money mockValorOriginal = mock(Money.class);
        Money mockSaldo = mock(Money.class);

        when(mockTitulo.isOrigemParcelamento()).thenReturn(false);
        when(mockTitulo.isParcelado()).thenReturn(false);
        when(mockTitulo.getId()).thenReturn(UUID.randomUUID());
        when(mockTitulo.getTipo()).thenReturn(TipoTitulo.A_PAGAR);
        when(mockTitulo.getStatus()).thenReturn(StatusTitulo.ABERTO);
        when(mockTitulo.getNumeroDocumento()).thenReturn("NF-001");
        when(mockTitulo.getDescricao()).thenReturn(descricao);
        when(mockTitulo.getPessoa()).thenReturn(mockPessoa);
        when(mockPessoa.getNome()).thenReturn("Empresa LTDA");
        when(mockTitulo.getTituloCategoria()).thenReturn(null);
        when(mockTitulo.getUnidadeNegocio()).thenReturn(mockUnidade);
        when(mockUnidade.getCodigo()).thenReturn("UN1");
        when(mockValorOriginal.getValue()).thenReturn(new BigDecimal("1500.50"));
        when(mockTitulo.getValorOriginal()).thenReturn(mockValorOriginal);
        when(mockSaldo.getValue()).thenReturn(new BigDecimal("1000.50"));
        when(mockTitulo.calcularSaldo()).thenReturn(mockSaldo);
        when(mockTitulo.getDataVencimento()).thenReturn(LocalDate.of(2026, 3, 15));
        when(mockTitulo.getDeleted()).thenReturn(false);

        return mockTitulo;
    }
}
