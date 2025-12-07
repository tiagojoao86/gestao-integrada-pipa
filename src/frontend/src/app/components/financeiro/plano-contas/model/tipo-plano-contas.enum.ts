export class TipoPlanoContas {
  private static RECEITA_INSTANCE = new TipoPlanoContas(
    'RECEITA',
    $localize`Receita`
  );
  private static DESPESA_INSTANCE = new TipoPlanoContas(
    'DESPESA',
    $localize`Despesa`
  );
  private static ATIVO_INSTANCE = new TipoPlanoContas(
    'ATIVO',
    $localize`Ativo`
  );
  private static PASSIVO_INSTANCE = new TipoPlanoContas(
    'PASSIVO',
    $localize`Passivo`
  );

  private constructor(private key: string, private label: string) {}

  public getKey(): string {
    return this.key;
  }

  public getLabel(): string {
    return this.label;
  }

  public static get RECEITA(): TipoPlanoContas {
    return TipoPlanoContas.RECEITA_INSTANCE;
  }

  public static get DESPESA(): TipoPlanoContas {
    return TipoPlanoContas.DESPESA_INSTANCE;
  }

  public static get ATIVO(): TipoPlanoContas {
    return TipoPlanoContas.ATIVO_INSTANCE;
  }

  public static get PASSIVO(): TipoPlanoContas {
    return TipoPlanoContas.PASSIVO_INSTANCE;
  }

  public static getList(): TipoPlanoContas[] {
    return [
      TipoPlanoContas.RECEITA,
      TipoPlanoContas.DESPESA,
      TipoPlanoContas.ATIVO,
      TipoPlanoContas.PASSIVO,
    ];
  }

  public static getByKey(key: string): TipoPlanoContas | undefined {
    return TipoPlanoContas.getList().find((tipo) => tipo.getKey() === key);
  }
}
