import { Exclude, Expose } from 'class-transformer';
import { CodigoConvenioDTO } from './codigo-convenio-dto';
import { ConvenioTipoCobranca } from './convenio-tipo-cobranca.enum';

@Exclude()
export class ConvenioDTO {
  @Expose() id?: string;
  @Expose() nome?: string;
  @Expose() pessoaId?: string;
  @Expose() pessoaNome?: string;
  @Expose() registroAns?: string;
  @Expose() ativo?: boolean;
  @Expose() tipoCobranca?: ConvenioTipoCobranca;
  @Expose() codigos?: CodigoConvenioDTO[];
  @Expose() createdAt?: Date;
  @Expose() updatedAt?: Date;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
