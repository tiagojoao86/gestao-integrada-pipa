package br.com.grupopipa.gestaointegrada.core.controller;

import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.PV_ID;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.R_EXPORT_CSV;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.R_FIND_BY_ID;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.R_LIST;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.R_QUERY;
import static br.com.grupopipa.gestaointegrada.core.controller.Response.ok;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.grupopipa.gestaointegrada.core.dto.AuditInfoDTO;
import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import br.com.grupopipa.gestaointegrada.core.dto.OrderDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseController<D extends DTO, G extends GridDTO, S extends CrudService<D, G>> {

    protected S service;

    public BaseController(S service) {
        this.service = service;
    }

    @PostMapping(R_QUERY)
    public Response list(@RequestBody PageRequest request) {
        Sort sort = Sort.by(request.getOrder().stream().map(OrderDTO::getOrder).toList());
        Pageable pageable = org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize(), sort);
        return ok(service.list(request.getFilter(), pageable));
    }

    @PostMapping(R_LIST)
    public Response listAll(@RequestBody PageRequest request) {
        Sort sort = Sort.by(request.getOrder().stream().map(OrderDTO::getOrder).toList());
        return ok(service.list(request.getFilter(), sort));
    }

    @PostMapping
    public Response save(@RequestBody D body) {
        return ok(service.save(body));
    }

    @GetMapping(R_FIND_BY_ID)
    public Response findById(@RequestParam(F_ID) UUID id) {
        return ok(service.findById(id));
    }

    @DeleteMapping(PV_ID)
    public Response delete(@PathVariable(F_ID) UUID id) {
        return ok(service.delete(id));
    }

    /**
     * Busca informações de auditoria de uma entidade. Este método deve ser
     * sobrescrito nos
     * controllers filhos para adicionar verificação de permissão.
     *
     * @param id ID da entidade
     * @return Response com AuditInfoDTO
     */
    @GetMapping("/{id}/audit-info")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        AuditInfoDTO auditInfo = service.getAuditInfo(id);
        return ok(auditInfo);
    }

    @PostMapping(R_EXPORT_CSV)
    public ResponseEntity<byte[]> exportCsv(@RequestBody PageRequest request) {
        byte[] csv = service.exportToCsv(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "export.csv");
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }
}
