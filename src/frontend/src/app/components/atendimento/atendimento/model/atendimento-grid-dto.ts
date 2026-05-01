import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class AtendimentoGridDTO {
  @Expose() id?: string;
  @Expose() numero?: number;
  @Expose() dataInicio?: string;
  @Expose() pacienteNome?: string;
  @Expose() profissionalAtendimentoNome?: string;
  @Expose() procedimentosCount?: number;
  @Expose() convenioNome?: string;
  @Expose() createdAt?: Date;
  @Expose() deleted?: boolean;
}
