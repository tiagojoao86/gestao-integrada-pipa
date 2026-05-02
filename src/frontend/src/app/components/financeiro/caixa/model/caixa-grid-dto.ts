import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class CaixaGridDTO {
  @Expose() id: string = '';
  @Expose() nome: string = '';
  @Expose() valorPadraoAbertura: number = 0;
  @Expose() percentualParcialConfigurado: boolean = false;
  @Expose() ativo: boolean = true;
  @Expose() deleted: boolean = false;
}
