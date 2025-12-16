export class UsuarioUnidadeNegocioDTO {
  unidadeNegocioId: string;
  unidadeNegocioNome: string;
  unidadeNegocioCodigo: string;
  isDefault: boolean;

  constructor(
    unidadeNegocioId: string,
    unidadeNegocioNome: string,
    unidadeNegocioCodigo: string,
    isDefault: boolean
  ) {
    this.unidadeNegocioId = unidadeNegocioId;
    this.unidadeNegocioNome = unidadeNegocioNome;
    this.unidadeNegocioCodigo = unidadeNegocioCodigo;
    this.isDefault = isDefault;
  }
}
