export class PessoaDTO {
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

  constructor(
    id: string,
    nome: string,
    ativa: boolean,
    tipoPessoa: TipoPessoa,
    email?: string,
    telefone?: string,
    observacoes?: string,
    cpf?: string,
    dataNascimento?: Date,
    cnpj?: string,
    razaoSocial?: string,
    inscricaoEstadual?: string,
    createdAt?: Date,
    updatedAt?: Date,
    createdBy?: string,
    updatedBy?: string
  ) {
    this.id = id;
    this.nome = nome;
    this.ativa = ativa;
    this.tipoPessoa = tipoPessoa;
    this.email = email;
    this.telefone = telefone;
    this.observacoes = observacoes;
    this.cpf = cpf;
    this.dataNascimento = dataNascimento;
    this.cnpj = cnpj;
    this.razaoSocial = razaoSocial;
    this.inscricaoEstadual = inscricaoEstadual;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
  }
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
