package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;
import static br.com.grupopipa.gestaointegrada.core.controller.Response.ok;
import static br.com.grupopipa.gestaointegrada.financeiro.planocontas.PlanoContasConstants.R_PLANO_CONTAS;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping(R_PLANO_CONTAS)
public class PlanoContasController
    extends BaseController<PlanoContasDTO, PlanoContasGridDTO, PlanoContasService> {

  public PlanoContasController(PlanoContasService service) {
    super(service);
  }

  @Override
  @PreAuthorize("hasAuthority('CADASTRO_PLANO_CONTAS_LISTAR')")
  public Response list(@RequestBody PageRequest request) {
    return super.list(request);
  }

  @Override
  @PreAuthorize("hasAuthority('CADASTRO_PLANO_CONTAS_EDITAR')")
  public Response save(@RequestBody PlanoContasDTO body) {
    return super.save(body);
  }

  @Override
  @PreAuthorize("hasAuthority('CADASTRO_PLANO_CONTAS_VISUALIZAR')")
  public Response findById(@RequestParam(F_ID) UUID id) {
    return super.findById(id);
  }

  @Override
  @PreAuthorize("hasAuthority('CADASTRO_PLANO_CONTAS_DELETAR')")
  public Response delete(@PathVariable(F_ID) UUID id) {
    return super.delete(id);
  }

  @GetMapping("/unidades-disponiveis")
  @PreAuthorize("hasAuthority('CADASTRO_PLANO_CONTAS_EDITAR')")
  public Response listarUnidadesDisponiveis() {
    return ok(service.listarUnidadesDisponiveis());
  }

  @Override
  @PreAuthorize("hasAuthority('FINANCEIRO_PLANO_CONTAS_AUDITAR')")
  public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
    return super.getAuditInfo(id);
  }
}
