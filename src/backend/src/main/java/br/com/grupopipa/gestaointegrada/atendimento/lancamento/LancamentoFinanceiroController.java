package br.com.grupopipa.gestaointegrada.atendimento.lancamento;

import static br.com.grupopipa.gestaointegrada.atendimento.lancamento.LancamentoFinanceiroConstants.R_LANCAMENTO_FINANCEIRO;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroDTO;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroGridDTO;
import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa.ReceberLancamentoRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_LANCAMENTO_FINANCEIRO)
public class LancamentoFinanceiroController
        extends BaseController<LancamentoFinanceiroDTO, LancamentoFinanceiroGridDTO,
                               LancamentoFinanceiroService> {

    public LancamentoFinanceiroController(LancamentoFinanceiroService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('LANCAMENTO_FINANCEIRO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('LANCAMENTO_FINANCEIRO_EDITAR')")
    public Response save(@RequestBody LancamentoFinanceiroDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('LANCAMENTO_FINANCEIRO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('LANCAMENTO_FINANCEIRO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('LANCAMENTO_FINANCEIRO_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }

    @PostMapping("/{id}/fechar-pagamento")
    @PreAuthorize("hasAuthority('LANCAMENTO_FINANCEIRO_EDITAR')")
    public Response fecharParaPagamento(@PathVariable UUID id) {
        return service.fecharParaPagamento(id);
    }

    @PostMapping("/{id}/fechar-faturamento")
    @PreAuthorize("hasAuthority('LANCAMENTO_FINANCEIRO_EDITAR')")
    public Response fecharParaFaturamento(@PathVariable UUID id) {
        return service.fecharParaFaturamento(id);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('LANCAMENTO_FINANCEIRO_EDITAR')")
    public Response cancelar(@PathVariable UUID id) {
        return service.cancelar(id);
    }

    @PostMapping("/{id}/receber")
    @PreAuthorize("hasAuthority('LANCAMENTO_FINANCEIRO_EDITAR')")
    public Response receber(@PathVariable UUID id, @RequestBody ReceberLancamentoRequest request) {
        return service.receber(id, request);
    }

    @GetMapping("/{id}/resolver-procedimento")
    @PreAuthorize("hasAuthority('LANCAMENTO_FINANCEIRO_EDITAR')")
    public Response resolverProcedimento(
            @PathVariable UUID id,
            @RequestParam UUID procedimentoId) {
        return Response.ok(service.resolverProcedimento(id, procedimentoId));
    }
}
