import { StatusAberturaCaixa } from './status-abertura-caixa.enum';

export interface CaixaComStatusDTO {
  caixaId: string;
  caixaNome: string;
  valorPadraoAbertura: number;
  statusSessao: StatusAberturaCaixa | null;
  aberturaCaixaId: string | null;
  dataAbertura: string | null;
  usuarioNomeAbertura: string | null;
}
