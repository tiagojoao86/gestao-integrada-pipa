import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class AgendamentoDTO {
  @Expose() id?: string;
  @Expose() agendaId?: string;
  @Expose() agendaNome?: string;
  @Expose() profissionalId?: string;
  @Expose() profissionalNome?: string;
  @Expose() pacienteId?: string;
  @Expose() pacienteNome?: string;
  @Expose() convenioId?: string;
  @Expose() convenioNome?: string;
  @Expose() categoriaId?: string;
  @Expose() categoriaNome?: string;
  @Expose() procedimentoId?: string;
  @Expose() procedimentoNome?: string;
  @Expose() observacao?: string;
  @Expose() status?: string;
  @Expose() atendimentoId?: string;
  @Expose() horariosInicio?: string[];
  @Expose() horariosFim?: string[];
  @Expose() createdAt?: string;
  @Expose() updatedAt?: string;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
