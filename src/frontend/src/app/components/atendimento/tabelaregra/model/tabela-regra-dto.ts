import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class TabelaRegraDTO {
  @Expose() id?: string;
  @Expose() convenioId?: string;
  @Expose() convenioNome?: string;
  @Expose() convenioCategoriaId?: string;
  @Expose() convenioCategoriaNome?: string;
  @Expose() tabelaId?: string;
  @Expose() tabelaNome?: string;
  @Expose() createdAt?: string;
  @Expose() updatedAt?: string;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
