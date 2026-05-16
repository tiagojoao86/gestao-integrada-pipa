import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class CaixaGridDTO {
  @Expose() id = '';
  @Expose() nome = '';
  @Expose() valorPadraoAbertura = 0;
  @Expose() percentualParcialConfigurado = false;
  @Expose() ativo = true;
  @Expose() unidadeNegocioNome?: string;
  @Expose() deleted = false;
}
