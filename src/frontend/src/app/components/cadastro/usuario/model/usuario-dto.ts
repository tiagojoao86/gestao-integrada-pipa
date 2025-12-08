import { PerfilDTO } from '../../perfil/model/perfil-dto';
import { UsuarioUnidadeNegocioDTO } from './usuario-unidade-negocio-dto';

export interface UsuarioDTO {
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
}
