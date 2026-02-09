import { Exclude, Expose, plainToInstance } from 'class-transformer';

@Exclude()
export class CondicaoPagamentoDTO {
  @Expose()
  id?: string;

  @Expose()
  condicao: string;

  @Expose()
  descricao?: string;

  @Expose()
  ativo: boolean;

  @Expose()
  quantidadeParcelas?: number;

  @Expose()
  diasVencimento?: number[];

  constructor(
    condicao: string,
    ativo: boolean,
    descricao?: string,
    quantidadeParcelas?: number,
    diasVencimento?: number[],
    id?: string
  ) {
    this.condicao = condicao;
    this.ativo = ativo;
    this.descricao = descricao;
    this.quantidadeParcelas = quantidadeParcelas;
    this.diasVencimento = diasVencimento;
    this.id = id;
  }

  static from(data: Partial<CondicaoPagamentoDTO>): CondicaoPagamentoDTO {
    return plainToInstance(CondicaoPagamentoDTO, data);
  }
}
