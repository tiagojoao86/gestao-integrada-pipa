export enum ConvenioTipoCobranca {
  PAGO_NO_ATO = 'PAGO_NO_ATO',
  FATURADO = 'FATURADO',
}

export function convenioTipoCobrancaLabel(tipo: string): string {
  switch (tipo) {
    case ConvenioTipoCobranca.PAGO_NO_ATO: return $localize`Pago no Ato`;
    case ConvenioTipoCobranca.FATURADO:    return $localize`Faturado`;
    default: return tipo;
  }
}
