import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class AgendaRegraDTO {
  @Expose() id?: string;
  @Expose() agendaId: string = '';
  @Expose() dataInicio: string = '';
  @Expose() dataFim?: string;
  @Expose() horaInicio: string = '';
  @Expose() horaFim: string = '';
  @Expose() duracaoSessaoMinutos: number = 30;
  @Expose() diasSemana: string[] = [];
  @Expose() convenioIds: string[] = [];
  @Expose() convenioNomes?: string[];
  @Expose() procedimentoIds: string[] = [];
  @Expose() procedimentoNomes?: string[];
}
