export class PessoaGridDTO {
  id: string;
  nome: string;
  documento: string; // CPF ou CNPJ formatado
  tipoPessoa: 'FISICA' | 'JURIDICA';
  ativa: boolean;
  createdAt: Date;

  constructor(
    id: string,
    nome: string,
    documento: string,
    tipoPessoa: 'FISICA' | 'JURIDICA',
    ativa: boolean,
    createdAt: Date
  ) {
    this.id = id;
    this.nome = nome;
    this.documento = documento;
    this.tipoPessoa = tipoPessoa;
    this.ativa = ativa;
    this.createdAt = createdAt;
  }
}
