export class TipoRemuneracao {
  static readonly CLT: TipoRemuneracao = new TipoRemuneracao('CLT', $localize`CLT`);
  static readonly PJ: TipoRemuneracao = new TipoRemuneracao('PJ', $localize`PJ`);
  static readonly HORA: TipoRemuneracao = new TipoRemuneracao('HORA', $localize`Por Hora`);

  private key: string;
  private label: string;

  private constructor(key: string, label: string) {
    this.key = key;
    this.label = label;
  }

  static getList(): TipoRemuneracao[] {
    return [TipoRemuneracao.CLT, TipoRemuneracao.PJ, TipoRemuneracao.HORA];
  }

  getKey(): string {
    return this.key;
  }

  getLabel(): string {
    return this.label;
  }

  static getByKey(key: string): TipoRemuneracao | undefined {
    return TipoRemuneracao.getList().find((t) => t.key === key);
  }
}
