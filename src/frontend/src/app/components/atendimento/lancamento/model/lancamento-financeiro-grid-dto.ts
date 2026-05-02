import { Exclude, Expose } from 'class-transformer';
import { LancamentoFinanceiroSituacao } from './lancamento-financeiro-situacao.enum';
import { LancamentoFinanceiroStatusFinanceiro } from './lancamento-financeiro-status-financeiro.enum';

@Exclude()
export class LancamentoFinanceiroGridDTO {
  @Expose() id?: string;
  @Expose() atendimentoNumero?: number;
  @Expose() dataAtendimento?: string;
  @Expose() pacienteNome?: string;
  @Expose() convenioNome?: string;
  @Expose() valorTotal?: number;
  @Expose() situacao?: LancamentoFinanceiroSituacao;
  @Expose() statusFinanceiro?: LancamentoFinanceiroStatusFinanceiro;
  @Expose() procedimentosCount?: number;
  @Expose() createdAt?: Date;
  @Expose() deleted?: boolean;
}
