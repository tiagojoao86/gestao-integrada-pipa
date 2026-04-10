export enum TipoTabela {
  PARTICULAR = 'PARTICULAR',
  CONVENIO = 'CONVENIO',
}

export namespace TipoTabela {
  export function getLabel(tipo: TipoTabela): string {
    switch (tipo) {
      case TipoTabela.PARTICULAR: return $localize`Particular`;
      case TipoTabela.CONVENIO: return $localize`Convênio`;
      default: return tipo;
    }
  }

  export function getAll(): { key: string; label: string }[] {
    return [
      { key: TipoTabela.PARTICULAR, label: TipoTabela.getLabel(TipoTabela.PARTICULAR) },
      { key: TipoTabela.CONVENIO, label: TipoTabela.getLabel(TipoTabela.CONVENIO) },
    ];
  }
}
