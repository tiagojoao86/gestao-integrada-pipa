import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class AgendaRegraGridDTO {
  @Expose() id?: string;
  @Expose() agendaId?: string;
  @Expose() dataInicio?: string;
  @Expose() dataFim?: string;
  @Expose() horaInicio?: string;
  @Expose() horaFim?: string;
  @Expose() duracaoSessaoMinutos?: number;
  @Expose() diasSemanaFormatado?: string;
  @Expose() qtdConvenios?: number;
  @Expose() qtdProcedimentos?: number;
  @Expose() deleted?: boolean;
}
