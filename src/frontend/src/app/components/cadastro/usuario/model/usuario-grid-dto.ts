export class UsuarioGridDTO {
  id: string;
  nome: string;
  login: string;
  createdAt: Date;
  deleted?: boolean;

  constructor(id: string, nome: string, login: string, createdAt: Date, deleted?: boolean) {
    this.id = id;
    this.nome = nome;
    this.login = login;
    this.createdAt = createdAt;
    this.deleted = deleted;
  }
}
