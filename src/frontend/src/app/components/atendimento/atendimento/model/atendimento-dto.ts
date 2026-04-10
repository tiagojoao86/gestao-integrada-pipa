import { Exclude, Expose } from 'class-transformer';
import { StatusAtendimento } from './status-atendimento.enum';

@Exclude()
export class AtendimentoDTO {
  @Expose() id?: string;
  @Expose() dataHora?: string;

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

  @Expose() procedimentoId?: string;
  @Expose() procedimentoCodigo?: string;
  @Expose() procedimentoDescricao?: string;

  @Expose() tabelaItemId?: string;
  @Expose() tabelaItemValor?: number;

  @Expose() status?: StatusAtendimento;
  @Expose() observacoes?: string;

  @Expose() createdAt?: Date;
  @Expose() updatedAt?: Date;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
