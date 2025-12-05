export class TipoConta {
  private static CORRENTE_INSTANCE = new TipoConta(
    'CORRENTE',
    $localize`Conta Corrente`
  );
  private static POUPANCA_INSTANCE = new TipoConta(
    'POUPANCA',
    $localize`Poupança`
  );
  private static CAIXA_INSTANCE = new TipoConta('CAIXA', $localize`Caixa`);
  private static INVESTIMENTO_INSTANCE = new TipoConta(
    'INVESTIMENTO',
    $localize`Investimento`
  );

  private constructor(private key: string, private label: string) {}

  public getKey(): string {
    return this.key;
  }

  public getLabel(): string {
    return this.label;
  }

  public static get CORRENTE(): TipoConta {
    return TipoConta.CORRENTE_INSTANCE;
  }

  public static get POUPANCA(): TipoConta {
    return TipoConta.POUPANCA_INSTANCE;
  }

  public static get CAIXA(): TipoConta {
    return TipoConta.CAIXA_INSTANCE;
  }

  public static get INVESTIMENTO(): TipoConta {
    return TipoConta.INVESTIMENTO_INSTANCE;
  }

  public static getList(): TipoConta[] {
    return [
      TipoConta.CORRENTE,
      TipoConta.POUPANCA,
      TipoConta.CAIXA,
      TipoConta.INVESTIMENTO,
    ];
  }

  public static getByKey(key: string): TipoConta | undefined {
    return TipoConta.getList().find((tipo) => tipo.getKey() === key);
  }
}
