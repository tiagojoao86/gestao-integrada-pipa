import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class AgendaGridDTO {
  @Expose() id?: string;
  @Expose() nome?: string;
  @Expose() profissionalNome?: string;
  @Expose() setorNome?: string;
  @Expose() ativo?: boolean;
  @Expose() createdAt?: string;
  @Expose() deleted?: boolean;
}
