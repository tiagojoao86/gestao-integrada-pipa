export class UF {
  static readonly AC = new UF('AC', 'AC — Acre');
  static readonly AL = new UF('AL', 'AL — Alagoas');
  static readonly AM = new UF('AM', 'AM — Amazonas');
  static readonly AP = new UF('AP', 'AP — Amapá');
  static readonly BA = new UF('BA', 'BA — Bahia');
  static readonly CE = new UF('CE', 'CE — Ceará');
  static readonly DF = new UF('DF', 'DF — Distrito Federal');
  static readonly ES = new UF('ES', 'ES — Espírito Santo');
  static readonly GO = new UF('GO', 'GO — Goiás');
  static readonly MA = new UF('MA', 'MA — Maranhão');
  static readonly MG = new UF('MG', 'MG — Minas Gerais');
  static readonly MS = new UF('MS', 'MS — Mato Grosso do Sul');
  static readonly MT = new UF('MT', 'MT — Mato Grosso');
  static readonly PA = new UF('PA', 'PA — Pará');
  static readonly PB = new UF('PB', 'PB — Paraíba');
  static readonly PE = new UF('PE', 'PE — Pernambuco');
  static readonly PI = new UF('PI', 'PI — Piauí');
  static readonly PR = new UF('PR', 'PR — Paraná');
  static readonly RJ = new UF('RJ', 'RJ — Rio de Janeiro');
  static readonly RN = new UF('RN', 'RN — Rio Grande do Norte');
  static readonly RO = new UF('RO', 'RO — Rondônia');
  static readonly RR = new UF('RR', 'RR — Roraima');
  static readonly RS = new UF('RS', 'RS — Rio Grande do Sul');
  static readonly SC = new UF('SC', 'SC — Santa Catarina');
  static readonly SE = new UF('SE', 'SE — Sergipe');
  static readonly SP = new UF('SP', 'SP — São Paulo');
  static readonly TO = new UF('TO', 'TO — Tocantins');

  private constructor(
    private readonly key: string,
    private readonly label: string,
  ) {}

  getKey(): string { return this.key; }
  getLabel(): string { return this.label; }

  static getList(): UF[] {
    return [
      UF.AC, UF.AL, UF.AM, UF.AP, UF.BA, UF.CE, UF.DF, UF.ES,
      UF.GO, UF.MA, UF.MG, UF.MS, UF.MT, UF.PA, UF.PB, UF.PE,
      UF.PI, UF.PR, UF.RJ, UF.RN, UF.RO, UF.RR, UF.RS, UF.SC,
      UF.SE, UF.SP, UF.TO,
    ];
  }

  static getByKey(key: string): UF | undefined {
    return UF.getList().find((uf) => uf.key === key);
  }
}
