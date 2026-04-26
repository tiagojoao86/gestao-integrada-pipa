import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class AgendamentoGridDTO {
  @Expose() id?: string;
  @Expose() agendaNome?: string;
  @Expose() pacienteNome?: string;
  @Expose() convenioNome?: string;
  @Expose() procedimentoNome?: string;
  @Expose() status?: string;
  @Expose() primeiraData?: string;
  @Expose() qtdHorarios?: number;
  @Expose() deleted?: boolean;
  @Expose() createdAt?: string;
}
