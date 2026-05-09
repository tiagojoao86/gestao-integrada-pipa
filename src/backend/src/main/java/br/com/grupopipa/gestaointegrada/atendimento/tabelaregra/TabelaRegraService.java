package br.com.grupopipa.gestaointegrada.atendimento.tabelaregra;

import java.time.LocalDate;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto.ResolverProcedimentoResponse;
import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto.TabelaRegraDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto.TabelaRegraGridDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface TabelaRegraService extends CrudService<TabelaRegraDTO, TabelaRegraGridDTO> {

    ResolverProcedimentoResponse resolverProcedimento(
        UUID convenioId,
        UUID convenioCategoriaId,
        UUID procedimentoId,
        LocalDate dataReferencia);
}
