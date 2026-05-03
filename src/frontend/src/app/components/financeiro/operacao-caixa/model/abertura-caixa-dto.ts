import { StatusAberturaCaixa } from './status-abertura-caixa.enum';

export interface AberturaCaixaDTO {
  id: string;
  caixaId: string;
  caixaNome: string;
  usuarioId: string;
  usuarioNome: string;
  status: StatusAberturaCaixa;
  dataAbertura: string;
  dataFechamento?: string;
  valorAbertura: number;
  valorConferencia?: number;
  observacoes?: string;
}
