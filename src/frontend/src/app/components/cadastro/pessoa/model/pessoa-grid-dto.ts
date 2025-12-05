export interface PessoaGridDTO {
  id: string;
  nome: string;
  documento: string; // CPF ou CNPJ formatado
  tipoPessoa: 'FISICA' | 'JURIDICA';
  ativa: boolean;
  createdAt: Date;
}
