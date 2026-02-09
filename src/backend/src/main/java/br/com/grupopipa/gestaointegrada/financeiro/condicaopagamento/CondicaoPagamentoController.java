package br.com.grupopipa.gestaointegrada.financeiro.condicaopagamento;

import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;
import static br.com.grupopipa.gestaointegrada.financeiro.condicaopagamento.CondicaoPagamentoConstants.R_CONDICAO_PAGAMENTO;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_CONDICAO_PAGAMENTO)
public class CondicaoPagamentoController
        extends BaseController<CondicaoPagamentoDTO, CondicaoPagamentoGridDTO, CondicaoPagamentoService> {

    public CondicaoPagamentoController(CondicaoPagamentoService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CONDICAO_PAGAMENTO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CONDICAO_PAGAMENTO_EDITAR')")
    public Response save(@RequestBody CondicaoPagamentoDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CONDICAO_PAGAMENTO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CONDICAO_PAGAMENTO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CONDICAO_PAGAMENTO_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }
}
