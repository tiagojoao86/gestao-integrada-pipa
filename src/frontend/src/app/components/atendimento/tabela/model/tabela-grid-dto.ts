import { Exclude, Expose } from 'class-transformer';
import { TipoTabela } from './tipo-tabela.enum';

@Exclude()
export class TabelaGridDTO {
  @Expose() id?: string;
  @Expose() nome?: string;
  @Expose() tipo?: TipoTabela;
  @Expose() ativo?: boolean;
  @Expose() createdAt?: Date;
  @Expose() deleted?: boolean;
}
