export interface ContaBancariaGridDTO {
  id: string;
  nome: string;
  banco?: string;
  tipo: string;
  saldoInicial?: number;
  ativa: boolean;
}
