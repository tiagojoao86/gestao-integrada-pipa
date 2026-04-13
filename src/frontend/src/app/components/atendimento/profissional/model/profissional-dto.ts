import {
  Exclude,
  Expose,
  Transform,
  TransformationType,
  TransformFnParams,
} from 'class-transformer';
import { TipoRemuneracao } from './tipo-remuneracao.enum';
import { UF } from './uf.enum';

@Exclude()
export class ProfissionalDTO {
  @Expose() id?: string;
  @Expose() pessoaId?: string;
  @Expose() pessoaNome?: string;
  @Expose() conselho?: string;
  @Expose() codigoConselho?: string;

  @Transform((params: TransformFnParams) => {
    const { type, value } = params;
    if (TransformationType.PLAIN_TO_CLASS === type) {
      return typeof value === 'string' ? TipoRemuneracao.getByKey(value) : value;
    }
    if (TransformationType.CLASS_TO_PLAIN === type) {
      if (value && typeof value.getKey === 'function') return value.getKey();
      if (typeof value === 'string') return value;
      return value;
    }
    return value;
  })
  @Expose()
  tipoRemuneracao?: TipoRemuneracao;

  @Expose() banco?: string;
  @Expose() conta?: string;
  @Expose() chavePix?: string;

  @Transform((params: TransformFnParams) => {
    const { type, value } = params;
    if (TransformationType.PLAIN_TO_CLASS === type) {
      return typeof value === 'string' ? UF.getByKey(value) : value;
    }
    if (TransformationType.CLASS_TO_PLAIN === type) {
      if (value && typeof value.getKey === 'function') return value.getKey();
      if (typeof value === 'string') return value;
      return value;
    }
    return value;
  })
  @Expose()
  uf?: UF;

  @Expose() ativo?: boolean;
  @Expose() createdAt?: Date;
  @Expose() updatedAt?: Date;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
