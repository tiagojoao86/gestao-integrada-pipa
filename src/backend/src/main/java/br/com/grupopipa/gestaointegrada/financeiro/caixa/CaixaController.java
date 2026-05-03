package br.com.grupopipa.gestaointegrada.financeiro.caixa;

import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;
import static br.com.grupopipa.gestaointegrada.financeiro.caixa.CaixaConstants.R_CAIXA;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_CAIXA)
public class CaixaController
        extends BaseController<CaixaDTO, CaixaGridDTO, CaixaService> {

    public CaixaController(CaixaService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_CAIXA_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_CAIXA_EDITAR')")
    public Response save(@RequestBody CaixaDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_CAIXA_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_CAIXA_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_CAIXA_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }

    @GetMapping("/{id}/usuarios")
    @PreAuthorize("hasAuthority('CADASTRO_CAIXA_VISUALIZAR')")
    public ResponseEntity<List<UsuarioCaixaDTO>> listarUsuarios(@PathVariable UUID id) {
        return ResponseEntity.ok(service.listarUsuarios(id));
    }

    @PutMapping("/{id}/usuarios")
    @PreAuthorize("hasAuthority('CADASTRO_CAIXA_EDITAR')")
    public ResponseEntity<Void> atualizarUsuarios(
            @PathVariable UUID id,
            @RequestBody List<UUID> usuarioIds) {
        service.atualizarUsuarios(id, usuarioIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/todos-ativos")
    @PreAuthorize("hasAuthority('CADASTRO_CAIXA_LISTAR')")
    public ResponseEntity<List<CaixaGridDTO>> listarTodosAtivos() {
        return ResponseEntity.ok(service.listarTodosAtivos());
    }

    @GetMapping("/por-usuario/{usuarioId}")
    @PreAuthorize("hasAuthority('CADASTRO_CAIXA_LISTAR')")
    public ResponseEntity<List<UUID>> listarPorUsuario(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(service.listarCaixasPorUsuario(usuarioId));
    }

    @PutMapping("/por-usuario/{usuarioId}")
    @PreAuthorize("hasAuthority('CADASTRO_CAIXA_EDITAR')")
    public ResponseEntity<Void> atualizarCaixasDoUsuario(
            @PathVariable UUID usuarioId,
            @RequestBody List<UUID> caixaIds) {
        service.atualizarCaixasDoUsuario(usuarioId, caixaIds);
        return ResponseEntity.ok().build();
    }
}
