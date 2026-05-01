import { Exclude, Expose } from 'class-transformer';
import { AtendimentoProcedimentoDTO } from './atendimento-procedimento-dto';

@Exclude()
export class AtendimentoDTO {
  @Expose() id?: string;
  @Expose() numero?: number;
  @Expose() dataInicio?: string;
  @Expose() dataFim?: string;

  @Expose() setorId?: string;
  @Expose() setorNome?: string;

  @Expose() pacienteId?: string;
  @Expose() pacienteNome?: string;

  @Expose() responsavelId?: string;
  @Expose() responsavelNome?: string;

  @Expose() convenioId?: string;
  @Expose() convenioNome?: string;

  @Expose() convenioCategoriaId?: string;
  @Expose() convenioCategoriaNome?: string;

  @Expose() profissionalAtendimentoId?: string;
  @Expose() profissionalAtendimentoNome?: string;

  @Expose() profissionalResponsavelId?: string;
  @Expose() profissionalResponsavelNome?: string;

  @Expose() procedimentos?: AtendimentoProcedimentoDTO[];

  @Expose() observacoes?: string;

  @Expose() createdAt?: Date;
  @Expose() updatedAt?: Date;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
