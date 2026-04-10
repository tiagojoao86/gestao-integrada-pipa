export enum StatusAtendimento {
  AGENDADO = 'AGENDADO',
  REALIZADO = 'REALIZADO',
  CANCELADO = 'CANCELADO',
  FALTOU = 'FALTOU',
}

export namespace StatusAtendimento {
  export function getLabel(status: StatusAtendimento): string {
    switch (status) {
      case StatusAtendimento.AGENDADO: return $localize`Agendado`;
      case StatusAtendimento.REALIZADO: return $localize`Realizado`;
      case StatusAtendimento.CANCELADO: return $localize`Cancelado`;
      case StatusAtendimento.FALTOU: return $localize`Faltou`;
      default: return status;
    }
  }

  export function getAll(): { key: string; label: string }[] {
    return [
      { key: StatusAtendimento.AGENDADO, label: StatusAtendimento.getLabel(StatusAtendimento.AGENDADO) },
      { key: StatusAtendimento.REALIZADO, label: StatusAtendimento.getLabel(StatusAtendimento.REALIZADO) },
      { key: StatusAtendimento.CANCELADO, label: StatusAtendimento.getLabel(StatusAtendimento.CANCELADO) },
      { key: StatusAtendimento.FALTOU, label: StatusAtendimento.getLabel(StatusAtendimento.FALTOU) },
    ];
  }
}
