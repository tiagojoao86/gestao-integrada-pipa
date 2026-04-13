import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class AtendimentoProcedimentoDTO {
  @Expose() id?: string;
  @Expose() procedimentoId?: string;
  @Expose() procedimentoCodigo?: string;
  @Expose() procedimentoDescricao?: string;
  @Expose() convenioId?: string;
  @Expose() convenioNome?: string;
  @Expose() tabelaItemId?: string;
  @Expose() tabelaItemValor?: number;
  @Expose() dataInicio?: string;
  @Expose() dataFim?: string;
}
