import { TituloCategoriaTipoEnum } from './titulo-categoria-tipo.enum';
import {
  Exclude,
  Expose,
  Transform,
  TransformationType,
  TransformFnParams,
} from 'class-transformer';

@Exclude()
export class TituloCategoriaGridDTO {
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
      return TituloCategoriaTipoEnum.getByKey(value);
    }

    if (TransformationType.CLASS_TO_PLAIN === type) {
      return value.key;
    }

    return value;
  })
  @Expose()
  tipo: TituloCategoriaTipoEnum;

  @Expose()
  agrupadorNome?: string;

  @Expose()
  padrao?: boolean;

  @Expose()
  deleted?: boolean;

  constructor(
    codigo: string,
    nome: string,
    tipo: TituloCategoriaTipoEnum,
    descricao?: string,
    agrupadorNome?: string,
    id?: string,
    deleted?: boolean,
    padrao?: boolean
  ) {
    this.codigo = codigo;
    this.nome = nome;
    this.tipo = tipo;
    this.descricao = descricao;
    this.agrupadorNome = agrupadorNome;
    this.id = id;
    this.deleted = deleted;
    this.padrao = padrao;
  }
}
