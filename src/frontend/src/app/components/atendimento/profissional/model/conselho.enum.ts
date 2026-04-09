export class Conselho {
  static readonly CRM = new Conselho('CRM', $localize`CRM — Medicina`);
  static readonly CRP = new Conselho('CRP', $localize`CRP — Psicologia`);
  static readonly CREFONO = new Conselho('CREFONO', $localize`CREFONO — Fonoaudiologia`);
  static readonly CREFITO = new Conselho('CREFITO', $localize`CREFITO — Fisioterapia e Terapia Ocupacional`);
  static readonly COREN = new Conselho('COREN', $localize`COREN — Enfermagem`);
  static readonly CRN = new Conselho('CRN', $localize`CRN — Nutrição`);
  static readonly CRO = new Conselho('CRO', $localize`CRO — Odontologia`);
  static readonly CRESS = new Conselho('CRESS', $localize`CRESS — Serviço Social`);
  static readonly CRBio = new Conselho('CRBio', $localize`CRBio — Biomedicina`);

  private key: string;
  private label: string;

  private constructor(key: string, label: string) {
    this.key = key;
    this.label = label;
  }

  static getList(): Conselho[] {
    return [
      Conselho.CRM,
      Conselho.CRP,
      Conselho.CREFONO,
      Conselho.CREFITO,
      Conselho.COREN,
      Conselho.CRN,
      Conselho.CRO,
      Conselho.CRESS,
      Conselho.CRBio,
    ];
  }

  getKey(): string {
    return this.key;
  }

  getLabel(): string {
    return this.label;
  }

  static getByKey(key: string): Conselho | undefined {
    return Conselho.getList().find((c) => c.key === key);
  }
}
