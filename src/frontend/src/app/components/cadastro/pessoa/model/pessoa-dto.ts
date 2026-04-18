import {
  Exclude,
  Expose,
  Transform,
  TransformationType,
  TransformFnParams,
} from 'class-transformer';

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

  public static getByKey(key: string): TipoPessoa | undefined {
    return TipoPessoa.getList().find((tipo) => tipo.key === key);
  }
}

@Exclude()
export class PessoaDTO {
  @Expose()
  id: string;
  @Expose()
  nome: string;
  @Expose()
  email?: string;
  @Expose()
  telefone?: string;
  @Expose()
  observacoes?: string;
  @Expose()
  ativa: boolean;

  // Pessoa Física
  @Expose()
  cpf?: string;
  @Expose()
  dataNascimento?: Date;

  // Pessoa Jurídica
  @Expose()
  cnpj?: string;
  @Expose()
  razaoSocial?: string;
  @Expose()
  inscricaoEstadual?: string;

  /**
   * Factory method to ensure we always work with proper class instances
   */
  static from(data: Partial<PessoaDTO>): PessoaDTO {
    const instance = new PessoaDTO(
      data.id || '',
      data.nome || '',
      data.ativa ?? true,
      data.tipoPessoa || TipoPessoa.FISICA,
      data.email,
      data.telefone,
      data.observacoes,
      data.cpf,
      data.dataNascimento,
      data.cnpj,
      data.razaoSocial,
      data.inscricaoEstadual,
      data.createdAt,
      data.updatedAt,
      data.createdBy,
      data.updatedBy,
      data.responsavelId,
      data.responsavelNome
    );
    return instance;
  }

  // Tipo para identificar se é PF ou PJ
  @Transform((params: TransformFnParams) => {
    const { type, value } = params;

    if (TransformationType.PLAIN_TO_CLASS === type) {
      // Converting from backend (string) to frontend (TipoPessoa object)
      if (typeof value === 'string') {
        return TipoPessoa.getByKey(value);
      }
      return value;
    }

    if (TransformationType.CLASS_TO_PLAIN === type) {
      // Converting from frontend (TipoPessoa object) to backend (string)
      if (value && typeof value.getKey === 'function') {
        return value.getKey();
      }
      if (typeof value === 'string') {
        return TipoPessoa.getByKey(value);
      }
      return value;
    }

    return value;
  })
  @Expose()
  tipoPessoa: TipoPessoa;

  // Endereço
  @Expose()
  enderecoCep?: string;
  @Expose()
  enderecoLogradouro?: string;
  @Expose()
  enderecoNumero?: string;
  @Expose()
  enderecoComplemento?: string;
  @Expose()
  enderecoBairro?: string;
  @Expose()
  enderecoCidade?: string;
  @Expose()
  enderecoUf?: string;

  // Responsável (para menores/incapazes)
  @Expose()
  responsavelId?: string;
  @Expose()
  responsavelNome?: string;

  @Expose()
  createdAt?: Date;
  @Expose()
  updatedAt?: Date;
  @Expose()
  createdBy?: string;
  @Expose()
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
    updatedBy?: string,
    responsavelId?: string,
    responsavelNome?: string
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
    this.responsavelId = responsavelId;
    this.responsavelNome = responsavelNome;
  }
}
