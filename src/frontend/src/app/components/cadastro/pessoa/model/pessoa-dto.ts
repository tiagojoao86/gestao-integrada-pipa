export interface PessoaDTO {
  id: string;
  nome: string;
  email?: string;
  telefone?: string;
  observacoes?: string;
  ativa: boolean;

  // Pessoa Física
  cpf?: string;
  dataNascimento?: Date;

  // Pessoa Jurídica
  cnpj?: string;
  razaoSocial?: string;
  inscricaoEstadual?: string;

  // Tipo para identificar se é PF ou PJ
  tipoPessoa: TipoPessoa;

  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;
}

export class TipoPessoa {
  static readonly FISICA: TipoPessoa = new TipoPessoa(
    'FISICA',
    $localize`Pessoa Física`
  );
  static readonly JURIDICA: TipoPessoa = new TipoPessoa(
    'JURIDICA',
    $localize`Pessoa Jurídica`
  );

  private key: string;
  private label: string;

  private constructor(key: string, label: string) {
    this.key = key;
    this.label = label;
  }

  static getList(): TipoPessoa[] {
    return [TipoPessoa.FISICA, TipoPessoa.JURIDICA];
  }

  static getKeys(): string[] {
    return [TipoPessoa.FISICA.key, TipoPessoa.JURIDICA.key];
  }

  getKey(): string {
    return this.key;
  }

  getLabel(): string {
    return this.label;
  }
}
