import { PerfilModuloDTO } from "./perfil-modulo-dto";

export interface PerfilDTO {
  id: string;
  nome: string;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;
  permissoes: PerfilModuloDTO[];
}
