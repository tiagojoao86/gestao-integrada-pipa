export class ConvenioGridDTO {
  id: string;
  nome: string;
  pessoaNome?: string;
  registroAns?: string;
  ativo?: boolean;
  deleted?: boolean;

  constructor(
    id: string,
    nome: string,
    pessoaNome?: string,
    registroAns?: string,
    ativo?: boolean,
    deleted?: boolean
  ) {
    this.id = id;
    this.nome = nome;
    this.pessoaNome = pessoaNome;
    this.registroAns = registroAns;
    this.ativo = ativo;
    this.deleted = deleted;
  }
}
