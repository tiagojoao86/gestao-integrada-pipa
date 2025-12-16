import { PerfilDTO } from '../../perfil/model/perfil-dto';
import { UsuarioUnidadeNegocioDTO } from './usuario-unidade-negocio-dto';

export class UsuarioDTO {
  id: string;
  nome: string;
  login: string;
  senha: string;
  createdat?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;
  perfis?: PerfilDTO[];
  unidadesNegocio?: UsuarioUnidadeNegocioDTO[];

  constructor(
    id: string,
    nome: string,
    login: string,
    senha: string,
    createdat?: Date,
    updatedAt?: Date,
    createdBy?: string,
    updatedBy?: string,
    perfis?: PerfilDTO[],
    unidadesNegocio?: UsuarioUnidadeNegocioDTO[]
  ) {
    this.id = id;
    this.nome = nome;
    this.login = login;
    this.senha = senha;
    this.createdat = createdat;
    this.updatedAt = updatedAt;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
    this.perfis = perfis;
    this.unidadesNegocio = unidadesNegocio;
  }
}
