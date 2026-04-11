export enum TipoTabela {
  PARTICULAR = 'PARTICULAR',
  CONVENIO = 'CONVENIO',
}

export function getTipoTabelaLabel(tipo: TipoTabela): string {
  switch (tipo) {
    case TipoTabela.PARTICULAR: return $localize`Particular`;
    case TipoTabela.CONVENIO: return $localize`Convênio`;
    default: return tipo;
  }
}

export function getTiposTabela(): { key: string; label: string }[] {
  return [
    { key: TipoTabela.PARTICULAR, label: getTipoTabelaLabel(TipoTabela.PARTICULAR) },
    { key: TipoTabela.CONVENIO, label: getTipoTabelaLabel(TipoTabela.CONVENIO) },
  ];
}
