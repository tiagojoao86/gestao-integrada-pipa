import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class ConvenioCategoriaDTO {
  @Expose() id?: string;
  @Expose() convenioId?: string;
  @Expose() convenioNome?: string;
  @Expose() nome?: string;
  @Expose() codigoAnsPlano?: string;
  @Expose() ativo?: boolean;
  @Expose() createdAt?: Date;
  @Expose() updatedAt?: Date;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
