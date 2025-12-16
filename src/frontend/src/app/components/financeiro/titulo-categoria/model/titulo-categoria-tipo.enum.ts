export class TituloCategoriaTipoEnum {
  public static readonly RECEITA = new TituloCategoriaTipoEnum(
    'RECEITA',
    $localize`Receita`
  );
  public static readonly DESPESA = new TituloCategoriaTipoEnum(
    'DESPESA',
    $localize`Despesa`
  );

  constructor(public readonly key: string, public readonly label: string) {}

  public static getList(): TituloCategoriaTipoEnum[] {
    return [this.RECEITA, this.DESPESA];
  }

  public static getByKey(key: string): TituloCategoriaTipoEnum | undefined {
    return TituloCategoriaTipoEnum.getList().find((tipo) => tipo.key === key);
  }
}
