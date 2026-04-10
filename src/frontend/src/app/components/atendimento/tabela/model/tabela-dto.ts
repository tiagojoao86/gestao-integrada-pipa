import { Exclude, Expose, Type } from 'class-transformer';
import { TabelaItemDTO } from './tabela-item-dto';
import { TipoTabela } from './tipo-tabela.enum';

@Exclude()
export class TabelaDTO {
  @Expose() id?: string;
  @Expose() nome?: string;
  @Expose() tipo?: TipoTabela;
  @Expose() ativo?: boolean;

  @Expose()
  @Type(() => TabelaItemDTO)
  itens?: TabelaItemDTO[];

  @Expose() createdAt?: Date;
  @Expose() updatedAt?: Date;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
