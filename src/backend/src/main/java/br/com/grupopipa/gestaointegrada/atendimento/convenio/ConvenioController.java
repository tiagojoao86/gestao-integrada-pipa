package br.com.grupopipa.gestaointegrada.atendimento.convenio;

import static br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioConstants.R_CONVENIO;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.dto.ConvenioDTO;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.dto.ConvenioGridDTO;
import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_CONVENIO)
public class ConvenioController
        extends BaseController<ConvenioDTO, ConvenioGridDTO, ConvenioService> {

    public ConvenioController(ConvenioService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_EDITAR')")
    public Response save(@RequestBody ConvenioDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }

    @GetMapping("/ativos")
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_LISTAR')")
    public Response listarAtivos() {
        return Response.ok(service.listarAtivos());
    }
}
