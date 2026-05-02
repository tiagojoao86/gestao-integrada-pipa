package br.com.grupopipa.gestaointegrada.atendimento.lancamento;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroDTO;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroGridDTO;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface LancamentoFinanceiroService
        extends CrudService<LancamentoFinanceiroDTO, LancamentoFinanceiroGridDTO> {

    Response pagar(UUID id);

    Response fechar(UUID id);

    Response cancelar(UUID id);
}
