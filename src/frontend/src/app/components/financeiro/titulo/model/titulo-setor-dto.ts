export class TituloSetorDTO {
  setorId: string;
  setorNome?: string;
  percentualRateio: number;

  constructor(setorId: string, percentualRateio: number, setorNome?: string) {
    this.setorId = setorId;
    this.percentualRateio = percentualRateio;
    this.setorNome = setorNome;
  }
}
