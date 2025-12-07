package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
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

    public MovimentacaoFinanceiraController(MovimentacaoFinanceiraService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_MOVIMENTACAO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_MOVIMENTACAO_EDITAR')")
    public Response save(@RequestBody MovimentacaoFinanceiraDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_MOVIMENTACAO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_MOVIMENTACAO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }
}
