import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class AgendaDTO {
  @Expose() id?: string;
  @Expose() nome?: string;
  @Expose() profissionalId?: string;
  @Expose() profissionalNome?: string;
  @Expose() setorId?: string;
  @Expose() setorNome?: string;
  @Expose() ativo?: boolean;
  @Expose() createdAt?: string;
  @Expose() updatedAt?: string;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
