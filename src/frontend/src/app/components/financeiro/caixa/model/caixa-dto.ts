import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class CaixaDTO {
  @Expose() id?: string;
  @Expose() nome = '';
  @Expose() valorPadraoAbertura = 0;
  @Expose() percentualPagamentoParcial?: number;
  @Expose() valorMinimoParcela?: number;
  @Expose() ativo = true;
  @Expose() unidadeNegocioId?: string;
  @Expose() unidadeNegocioNome?: string;
  @Expose() createdAt?: string;
  @Expose() updatedAt?: string;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
