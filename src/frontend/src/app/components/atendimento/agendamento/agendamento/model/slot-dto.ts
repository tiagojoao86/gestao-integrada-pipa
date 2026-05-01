import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class SlotDTO {
  @Expose() dataHoraInicio?: string;
  @Expose() dataHoraFim?: string;
  @Expose() livre?: boolean;
  @Expose() agendamentoId?: string;
  @Expose() atendimentoId?: string;
  @Expose() pacienteNome?: string;
  @Expose() convenioNome?: string;
  @Expose() procedimentoNome?: string;
  @Expose() status?: string;
}
