import { GrupoModuloEnum } from "./grupo-modulo.enum";

export interface ModuloDTO {
  id: string;
  chave: string;
  nome: string;
  grupoEnum: GrupoModuloEnum;
}
