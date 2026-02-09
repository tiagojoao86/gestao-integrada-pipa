import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class CondicaoPagamentoGridDTO {
  @Expose()
  id?: string;

  @Expose()
  condicao: string;

  @Expose()
  ativo: boolean;

  @Expose()
  quantidadeParcelas?: number;

  @Expose()
  deleted?: boolean;

  constructor(
    condicao: string,
    ativo: boolean,
    quantidadeParcelas?: number,
    id?: string,
    deleted?: boolean
  ) {
    this.condicao = condicao;
    this.ativo = ativo;
    this.quantidadeParcelas = quantidadeParcelas;
    this.id = id;
    this.deleted = deleted;
  }
}
