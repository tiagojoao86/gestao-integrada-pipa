export interface ContaBancariaDTO {
  id?: string;
  nome: string;
  banco?: string;
  agencia?: string;
  numeroConta?: string;
  tipo: string; // CORRENTE, POUPANCA, CAIXA, INVESTIMENTO
  saldoInicial?: number;
  ativa?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;
}
