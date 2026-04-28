import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class AgendaRegraDTO {
  @Expose() id?: string;
  @Expose() agendaId = '';
  @Expose() dataInicio = '';
  @Expose() dataFim?: string;
  @Expose() horaInicio = '';
  @Expose() horaFim = '';
  @Expose() duracaoSessaoMinutos = 30;
  @Expose() diasSemana: string[] = [];
  @Expose() convenioIds: string[] = [];
  @Expose() convenioNomes?: string[];
  @Expose() procedimentoIds: string[] = [];
  @Expose() procedimentoNomes?: string[];
}
