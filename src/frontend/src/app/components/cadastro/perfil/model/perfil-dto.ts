import { PerfilModuloDTO } from './perfil-modulo-dto';

export class PerfilDTO {
  id?: string;
  nome: string;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;
  permissoes: PerfilModuloDTO[];

  constructor(
    id: string | undefined,
    nome: string,
    createdAt: Date | undefined,
    updatedAt: Date | undefined,
    createdBy: string | undefined,
    updatedBy: string | undefined,
    permissoes: PerfilModuloDTO[]
  ) {
    this.id = id;
    this.nome = nome;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
    this.permissoes = permissoes;
  }
}
