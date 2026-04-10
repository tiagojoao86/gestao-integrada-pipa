import { Exclude, Expose } from 'class-transformer';
import { StatusAtendimento } from './status-atendimento.enum';

@Exclude()
export class AtendimentoGridDTO {
  @Expose() id?: string;
  @Expose() dataHora?: string;
  @Expose() pacienteNome?: string;
  @Expose() profissionalAtendimentoNome?: string;
  @Expose() procedimentoCodigo?: string;
  @Expose() convenioNome?: string;
  @Expose() status?: StatusAtendimento;
  @Expose() createdAt?: Date;
  @Expose() deleted?: boolean;
}
