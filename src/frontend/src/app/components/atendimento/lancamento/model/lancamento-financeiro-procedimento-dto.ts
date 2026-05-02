import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class LancamentoFinanceiroProcedimentoDTO {
  @Expose() id?: string;
  @Expose() procedimentoId?: string;
  @Expose() procedimentoCodigo?: string;
  @Expose() procedimentoDescricao?: string;
  @Expose() convenioId?: string;
  @Expose() convenioNome?: string;
  @Expose() tabelaItemId?: string;
  @Expose() valor?: number;
}
