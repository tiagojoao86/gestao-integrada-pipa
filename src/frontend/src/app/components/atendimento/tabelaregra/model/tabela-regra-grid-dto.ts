import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class TabelaRegraGridDTO {
  @Expose() id?: string;
  @Expose() convenioNome?: string;
  @Expose() convenioCategoriaNome?: string;
  @Expose() tabelaNome?: string;
  @Expose() createdAt?: string;
  @Expose() deleted?: boolean;
}
