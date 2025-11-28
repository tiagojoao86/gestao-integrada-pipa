import { PerfilDTO } from '../../perfil/model/perfil-dto';

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
}
