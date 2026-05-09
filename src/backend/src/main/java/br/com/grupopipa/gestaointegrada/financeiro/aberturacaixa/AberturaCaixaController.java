package br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa;

import static br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.AberturaCaixaConstants.R_ABERTURA_CAIXA;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroGridDTO;
import br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa.MovimentacaoCaixaGridDTO;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_ABERTURA_CAIXA)
public class AberturaCaixaController {

    private final AberturaCaixaService service;

    public AberturaCaixaController(AberturaCaixaService service) {
        this.service = service;
    }

    @PostMapping("/abrir")
    @PreAuthorize("hasAuthority('OPERACAO_CAIXA_EDITAR')")
    public ResponseEntity<AberturaCaixaDTO> abrir(@RequestBody AbrirCaixaRequest request) {
        return ResponseEntity.ok(service.abrir(request));
    }

    @PostMapping("/{id}/fechar")
    @PreAuthorize("hasAuthority('OPERACAO_CAIXA_EDITAR')")
    public ResponseEntity<AberturaCaixaDTO> fechar(
            @PathVariable UUID id,
            @RequestBody FecharCaixaRequest request) {
        return ResponseEntity.ok(service.fechar(id, request));
    }

    @GetMapping("/ativa")
    @PreAuthorize("hasAuthority('OPERACAO_CAIXA_VISUALIZAR')")
    public ResponseEntity<AberturaCaixaDTO> findAtiva(@RequestParam UUID caixaId) {
        return service.findAtivaByCaixaId(caixaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/meus-caixas")
    @PreAuthorize("hasAuthority('OPERACAO_CAIXA_VISUALIZAR')")
    public ResponseEntity<List<CaixaComStatusDTO>> listarMeusCaixas() {
        return ResponseEntity.ok(service.listarMeusCaixas());
    }

    @GetMapping("/status/{caixaId}")
    @PreAuthorize("hasAuthority('OPERACAO_CAIXA_VISUALIZAR')")
    public ResponseEntity<CaixaComStatusDTO> statusPorCaixa(@PathVariable UUID caixaId) {
        return ResponseEntity.ok(service.statusPorCaixa(caixaId));
    }

    @GetMapping("/{id}/lancamentos-pendentes")
    @PreAuthorize("hasAuthority('OPERACAO_CAIXA_VISUALIZAR')")
    public ResponseEntity<List<LancamentoFinanceiroGridDTO>> listarLancamentosPendentes(
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.listarLancamentosPendentes(id));
    }

    @GetMapping("/{id}/movimentacoes")
    @PreAuthorize("hasAuthority('OPERACAO_CAIXA_VISUALIZAR')")
    public ResponseEntity<List<MovimentacaoCaixaGridDTO>> listarMovimentacoes(@PathVariable UUID id) {
        return ResponseEntity.ok(service.listarMovimentacoes(id));
    }
}
