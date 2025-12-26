package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import br.com.grupopipa.gestaointegrada.financeiro.titulo.TituloService;
import br.com.grupopipa.gestaointegrada.financeiro.titulo.TituloDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static br.com.grupopipa.gestaointegrada.financeiro.movimentacao.MovimentacaoFinanceiraConstants.R_MOVIMENTACAO;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_MOVIMENTACAO)
public class MovimentacaoFinanceiraController extends
        BaseController<MovimentacaoFinanceiraDTO, MovimentacaoFinanceiraGridDTO, MovimentacaoFinanceiraService> {

    private final TituloService tituloService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MovimentacaoFinanceiraController.class);

    public MovimentacaoFinanceiraController(MovimentacaoFinanceiraService service, TituloService tituloService) {
        super(service);
        this.tituloService = tituloService;
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_MOVIMENTACAO_FINANCEIRA_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_MOVIMENTACAO_FINANCEIRA_EDITAR')")
    public Response save(@RequestBody MovimentacaoFinanceiraDTO body) {
        if (LOGGER.isInfoEnabled()) {
            try {
                LOGGER.info("Saving MovimentacaoFinanceira - contaBancariaId={}, valor={}, titulosCount={}",
                        body != null ? body.getContaBancariaId() : null,
                        body != null ? body.getValor() : null,
                        body != null && body.getTitulos() != null ? body.getTitulos().size() : 0);
            } catch (Exception e) {
                LOGGER.warn("Error logging MovimentacaoFinanceiraDTO", e);
            }
        }
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_MOVIMENTACAO_FINANCEIRA_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_MOVIMENTACAO_FINANCEIRA_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @GetMapping("/titulos/search")
    @PreAuthorize("hasAuthority('FINANCEIRO_MOVIMENTACAO_FINANCEIRA_LISTAR')")
    public Response searchTitulos(@RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        List<TituloDTO> results = tituloService.searchByQuery(q, size);
        return Response.ok(results);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_MOVIMENTACAO_FINANCEIRA_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }
}
