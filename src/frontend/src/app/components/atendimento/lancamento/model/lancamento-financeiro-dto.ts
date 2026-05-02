import { Exclude, Expose } from 'class-transformer';
import { LancamentoFinanceiroSituacao } from './lancamento-financeiro-situacao.enum';
import { LancamentoFinanceiroStatusFinanceiro } from './lancamento-financeiro-status-financeiro.enum';
import { LancamentoFinanceiroProcedimentoDTO } from './lancamento-financeiro-procedimento-dto';
import { ConvenioTipoCobranca } from '../../convenio/model/convenio-tipo-cobranca.enum';

@Exclude()
export class LancamentoFinanceiroDTO {
  @Expose() id?: string;

  @Expose() atendimentoId?: string;
  @Expose() atendimentoNumero?: number;
  @Expose() dataAtendimento?: string;

  @Expose() pacienteId?: string;
  @Expose() pacienteNome?: string;

  @Expose() convenioId?: string;
  @Expose() convenioNome?: string;
  @Expose() convenioTipoCobranca?: ConvenioTipoCobranca;

  @Expose() valorTotal?: number;
  @Expose() situacao?: LancamentoFinanceiroSituacao;
  @Expose() statusFinanceiro?: LancamentoFinanceiroStatusFinanceiro;

  @Expose() procedimentos?: LancamentoFinanceiroProcedimentoDTO[];

  @Expose() observacoes?: string;

  @Expose() setorId?: string;
  @Expose() setorNome?: string;
  @Expose() unidadeNegocioId?: string;
  @Expose() unidadeNegocioNome?: string;
  @Expose() tituloId?: string;

  @Expose() createdAt?: Date;
  @Expose() updatedAt?: Date;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
