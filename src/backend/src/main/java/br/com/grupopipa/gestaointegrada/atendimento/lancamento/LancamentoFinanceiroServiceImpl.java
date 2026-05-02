package br.com.grupopipa.gestaointegrada.atendimento.lancamento;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroDTO;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroProcedimentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiro;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroProcedimento;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroSituacaoEnum;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
public class LancamentoFinanceiroServiceImpl
        extends CrudServiceImpl<LancamentoFinanceiroDTO, LancamentoFinanceiroGridDTO,
                                LancamentoFinanceiro, LancamentoFinanceiroRepository>
        implements LancamentoFinanceiroService {

    public LancamentoFinanceiroServiceImpl(
            LancamentoFinanceiroRepository repository,
            Specifications<LancamentoFinanceiro> specifications) {
        super(repository, specifications);
    }

    @Override
    protected LancamentoFinanceiro mergeEntityAndDTO(
            LancamentoFinanceiro entity, LancamentoFinanceiroDTO dto) {
        LancamentoFinanceiro lancamento;
        if (Objects.isNull(entity)) {
            lancamento = criarLancamento(dto);
        } else {
            entity.atualizar(dto.getObservacoes());
            lancamento = entity;
        }

        if (lancamento.getSituacao() == LancamentoFinanceiroSituacaoEnum.ABERTO
                && dto.getProcedimentos() != null) {
            List<LancamentoFinanceiroProcedimento> procs =
                resolverProcedimentos(dto.getProcedimentos(), lancamento);
            lancamento.syncProcedimentos(procs);
        }

        return lancamento;
    }

    private LancamentoFinanceiro criarLancamento(LancamentoFinanceiroDTO dto) {
        return new LancamentoFinanceiro.Builder()
                .atendimentoId(dto.getAtendimentoId())
                .atendimentoNumero(dto.getAtendimentoNumero())
                .dataAtendimento(dto.getDataAtendimento())
                .pacienteId(dto.getPacienteId())
                .pacienteNome(dto.getPacienteNome())
                .convenioId(dto.getConvenioId())
                .convenioNome(dto.getConvenioNome())
                .convenioTipoCobranca(dto.getConvenioTipoCobranca())
                .valorTotal(dto.getValorTotal())
                .build();
    }

    private List<LancamentoFinanceiroProcedimento> resolverProcedimentos(
            List<LancamentoFinanceiroProcedimentoDTO> dtos, LancamentoFinanceiro lancamento) {
        List<LancamentoFinanceiroProcedimento> result = new ArrayList<>();
        for (LancamentoFinanceiroProcedimentoDTO dto : dtos) {
            result.add(new LancamentoFinanceiroProcedimento(
                lancamento,
                dto.getProcedimentoId(),
                dto.getProcedimentoCodigo(),
                dto.getProcedimentoDescricao(),
                dto.getConvenioId(),
                dto.getConvenioNome(),
                dto.getTabelaItemId(),
                dto.getValor()));
        }
        return result;
    }

    @Override
    public Response pagar(UUID id) {
        LancamentoFinanceiro lancamento = findEntity(id);
        lancamento.pagar();
        return Response.ok(buildDTOFromEntity(repository.save(lancamento)));
    }

    @Override
    public Response fechar(UUID id) {
        LancamentoFinanceiro lancamento = findEntity(id);
        lancamento.fechar();
        return Response.ok(buildDTOFromEntity(repository.save(lancamento)));
    }

    @Override
    public Response cancelar(UUID id) {
        LancamentoFinanceiro lancamento = findEntity(id);
        lancamento.cancelar();
        return Response.ok(buildDTOFromEntity(repository.save(lancamento)));
    }

    private LancamentoFinanceiro findEntity(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("LancamentoFinanceiro", id));
    }

    @Override
    protected LancamentoFinanceiroDTO buildDTOFromEntity(LancamentoFinanceiro entity) {
        List<LancamentoFinanceiroProcedimentoDTO> procedimentos = entity.getProcedimentos().stream()
            .map(p -> LancamentoFinanceiroProcedimentoDTO.builder()
                    .id(p.getId())
                    .procedimentoId(p.getProcedimentoId())
                    .procedimentoCodigo(p.getProcedimentoCodigo())
                    .procedimentoDescricao(p.getProcedimentoDescricao())
                    .convenioId(p.getConvenioId())
                    .convenioNome(p.getConvenioNome())
                    .tabelaItemId(p.getTabelaItemId())
                    .valor(p.getValor())
                    .build())
            .toList();

        return LancamentoFinanceiroDTO.builder()
                .id(entity.getId())
                .atendimentoId(entity.getAtendimentoId())
                .atendimentoNumero(entity.getAtendimentoNumero())
                .dataAtendimento(entity.getDataAtendimento())
                .pacienteId(entity.getPacienteId())
                .pacienteNome(entity.getPacienteNome())
                .convenioId(entity.getConvenioId())
                .convenioNome(entity.getConvenioNome())
                .convenioTipoCobranca(entity.getConvenioTipoCobranca())
                .valorTotal(entity.getValorTotal())
                .situacao(entity.getSituacao())
                .statusFinanceiro(entity.getStatusFinanceiro())
                .procedimentos(procedimentos)
                .observacoes(entity.getObservacoes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected LancamentoFinanceiroGridDTO buildGridDTOFromEntity(LancamentoFinanceiro entity) {
        return LancamentoFinanceiroGridDTO.builder()
                .id(entity.getId())
                .atendimentoNumero(entity.getAtendimentoNumero())
                .dataAtendimento(entity.getDataAtendimento())
                .pacienteNome(entity.getPacienteNome())
                .convenioNome(entity.getConvenioNome())
                .valorTotal(entity.getValorTotal())
                .situacao(entity.getSituacao())
                .statusFinanceiro(entity.getStatusFinanceiro())
                .procedimentosCount(entity.getProcedimentos().size())
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("dataAtendimento", "createdAt");
    }

    @Override
    protected Class<LancamentoFinanceiro> getEntityClass() {
        return LancamentoFinanceiro.class;
    }
}
