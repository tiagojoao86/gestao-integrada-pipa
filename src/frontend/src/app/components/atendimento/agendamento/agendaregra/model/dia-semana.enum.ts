export interface DiaSemanaOption {
  key: string;
  label: string;
}

export const DIA_SEMANA_OPTIONS: DiaSemanaOption[] = [
  { key: 'SEG', label: $localize`Seg` },
  { key: 'TER', label: $localize`Ter` },
  { key: 'QUA', label: $localize`Qua` },
  { key: 'QUI', label: $localize`Qui` },
  { key: 'SEX', label: $localize`Sex` },
  { key: 'SAB', label: $localize`Sáb` },
  { key: 'DOM', label: $localize`Dom` },
];
