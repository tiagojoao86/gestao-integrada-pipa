import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class TabelaItemDTO {
  @Expose() id?: string;
  @Expose() tabelaId?: string;
  @Expose() procedimentoId?: string;
  @Expose() procedimentoCodigo?: string;
  @Expose() procedimentoDescricao?: string;
  @Expose() valor?: number;
  @Expose() vigenciaInicio?: string;
  @Expose() vigenciaFim?: string;
}
