export interface ContaBancariaDTO {
  id?: string;
  nome: string;
  banco?: string;
  agencia?: string;
  numeroConta?: string;
  tipo: string; // CORRENTE, POUPANCA, CAIXA, INVESTIMENTO
  saldoInicial?: number;
  unidadeNegocioId?: string;
  unidadeNegocioNome?: string;
  unidadeNegocioCodigo?: string;
  ativa?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;
}
