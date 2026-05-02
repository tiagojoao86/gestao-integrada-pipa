import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class CaixaDTO {
  @Expose() id?: string;
  @Expose() nome: string = '';
  @Expose() valorPadraoAbertura: number = 0;
  @Expose() percentualPagamentoParcial?: number;
  @Expose() valorMinimoParcela?: number;
  @Expose() ativo: boolean = true;
  @Expose() createdAt?: string;
  @Expose() updatedAt?: string;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
