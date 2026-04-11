package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria;

import static br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.ConvenioCategoriaConstants.R_CONVENIO_CATEGORIA;
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

import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto.ConvenioCategoriaDTO;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto.ConvenioCategoriaGridDTO;
import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_CONVENIO_CATEGORIA)
public class ConvenioCategoriaController
        extends BaseController<ConvenioCategoriaDTO, ConvenioCategoriaGridDTO, ConvenioCategoriaService> {

    public ConvenioCategoriaController(ConvenioCategoriaService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_CATEGORIA_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_CATEGORIA_EDITAR')")
    public Response save(@RequestBody ConvenioCategoriaDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_CATEGORIA_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_CATEGORIA_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_CATEGORIA_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }

    @GetMapping("/por-convenio/{convenioId}")
    @PreAuthorize("hasAuthority('ATENDIMENTO_CONVENIO_CATEGORIA_LISTAR')")
    public List<ConvenioCategoriaGridDTO> listarPorConvenio(@PathVariable UUID convenioId) {
        return service.listarPorConvenio(convenioId);
    }
}
