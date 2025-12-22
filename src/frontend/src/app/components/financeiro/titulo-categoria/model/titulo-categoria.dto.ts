import { TituloCategoriaTipoEnum } from './titulo-categoria-tipo.enum';
import {
  Exclude,
  Expose,
  plainToInstance,
  Transform,
  TransformationType,
  TransformFnParams,
} from 'class-transformer';

@Exclude()
export class TituloCategoriaDTO {
  @Expose()
  id?: string;

  @Expose()
  codigo: string;

  @Expose()
  nome: string;

  @Expose()
  descricao?: string;

  @Transform((params: TransformFnParams) => {
    const { type, value } = params;

    if (TransformationType.PLAIN_TO_CLASS === type) {
      if (typeof value === 'string') {
        return TituloCategoriaTipoEnum.getByKey(value);
      } else if (value instanceof TituloCategoriaTipoEnum) {
        return value;
      }
      return undefined;
    }

    if (TransformationType.CLASS_TO_PLAIN === type) {
      return value.key;
    }

    return value;
  })
  @Expose()
  tipo: TituloCategoriaTipoEnum;

  @Expose()
  agrupadorId?: string;

  @Expose()
  agrupadorNome?: string;

  constructor(
    codigo: string,
    nome: string,
    tipo: TituloCategoriaTipoEnum,
    descricao?: string,
    agrupadorId?: string,
    agrupadorNome?: string,
    id?: string
  ) {
    this.codigo = codigo;
    this.nome = nome;
    this.tipo = tipo;
    this.descricao = descricao;
    this.agrupadorId = agrupadorId;
    this.agrupadorNome = agrupadorNome;
    this.id = id;
  }

  static from(data: Partial<TituloCategoriaDTO>): TituloCategoriaDTO {
    return plainToInstance(TituloCategoriaDTO, data);
  }
}
