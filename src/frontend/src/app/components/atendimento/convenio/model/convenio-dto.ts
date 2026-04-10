import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class ConvenioDTO {
  @Expose() id?: string;
  @Expose() nome?: string;
  @Expose() pessoaId?: string;
  @Expose() pessoaNome?: string;
  @Expose() registroAns?: string;
  @Expose() ativo?: boolean;
  @Expose() createdAt?: Date;
  @Expose() updatedAt?: Date;
  @Expose() createdBy?: string;
  @Expose() updatedBy?: string;
}
