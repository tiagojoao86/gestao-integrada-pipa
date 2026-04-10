import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class ProcedimentoDTO {
  @Expose() id?: string;
  @Expose() codigo?: string;
  @Expose() codigoTiss?: string;
  @Expose() codigoTuss?: string;
  @Expose() descricao?: string;
  @Expose() ativo?: boolean;
  @Expose() createdAt?: Date;
  @Expose() updatedAt?: Date;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
