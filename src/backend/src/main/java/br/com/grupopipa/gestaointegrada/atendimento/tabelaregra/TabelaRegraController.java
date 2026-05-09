package br.com.grupopipa.gestaointegrada.atendimento.tabelaregra;

import static br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.TabelaRegraConstants.R_TABELA_REGRA;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto.TabelaRegraDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto.TabelaRegraGridDTO;
import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_TABELA_REGRA)
public class TabelaRegraController
        extends BaseController<TabelaRegraDTO, TabelaRegraGridDTO, TabelaRegraService> {

    public TabelaRegraController(TabelaRegraService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_TABELA_REGRA_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_TABELA_REGRA_EDITAR')")
    public Response save(@RequestBody TabelaRegraDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_TABELA_REGRA_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_TABELA_REGRA_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_TABELA_REGRA_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }

    @GetMapping("/resolver-procedimento")
    @PreAuthorize("hasAuthority('ATENDIMENTO_TABELA_REGRA_LISTAR')")
    public Response resolverProcedimento(
            @RequestParam UUID convenioId,
            @RequestParam(required = false) UUID convenioCategoriaId,
            @RequestParam UUID procedimentoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        return Response.ok(service.resolverProcedimento(
            convenioId, convenioCategoriaId, procedimentoId, dataReferencia));
    }
}
