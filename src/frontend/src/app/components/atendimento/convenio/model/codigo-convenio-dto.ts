import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class CodigoConvenioDTO {
  @Expose() id?: string;
  @Expose() convenioId?: string;
  @Expose() procedimentoId?: string;
  @Expose() procedimentoCodigo?: string;
  @Expose() procedimentoDescricao?: string;
  @Expose() codigo?: string;
}
