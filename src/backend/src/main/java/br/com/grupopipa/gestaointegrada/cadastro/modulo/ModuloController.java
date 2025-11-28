package br.com.grupopipa.gestaointegrada.cadastro.modulo;

import static br.com.grupopipa.gestaointegrada.core.controller.Response.ok;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(ModuloConstants.R_MODULO)
public class ModuloController extends BaseController<ModuloDTO, ModuloGridDTO, ModuloService> {

    @PreAuthorize("hasAuthority('CADASTRO_PERFIL_LISTAR')")
    @GetMapping
    public Response listAll() {
        List<ModuloDTO> list = service.findAllSimple();
        return ok(list);
    }

    @PreAuthorize("hasAuthority('CADASTRO_PERFIL_LISTAR')")
    @GetMapping("/grouped")
    public Response listGrouped() {
        List<ModuloDTO> list = service.findAllSimple();
        Map<String, List<ModuloDTO>> grouped = list.stream()
                .collect(Collectors.groupingBy(m -> m.getGrupoEnum() != null ? m.getGrupoEnum().name() : "UNDEFINED"));
        return ok(grouped);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PERFIL_EDITAR')")
    public Response save(ModuloDTO body) {
        throw new UnsupportedOperationException("Save not supported for Modulo");
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PERFIL_DELETAR')")
    public Response delete(UUID id) {
        throw new UnsupportedOperationException("Delete not supported for Modulo");
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PERFIL_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }
    
    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PERFIL_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

}
