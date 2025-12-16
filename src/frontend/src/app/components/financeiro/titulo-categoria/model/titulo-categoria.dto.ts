import { TituloCategoriaTipoEnum } from './titulo-categoria-tipo.enum';
import {
  Exclude,
  Expose,
  Transform,
  TransformationType,
  TransformFnParams,
} from 'class-transformer';

@Exclude()
export class TituloCategoriaDTO {
  @Expose()
  id?: string;

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

  constructor(
    nome: string,
    tipo: TituloCategoriaTipoEnum,
    descricao?: string,
    id?: string
  ) {
    this.nome = nome;
    this.tipo = tipo;
    this.descricao = descricao;
    this.id = id;
  }
}
