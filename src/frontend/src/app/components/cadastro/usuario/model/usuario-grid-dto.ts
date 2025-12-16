export class UsuarioGridDTO {
  id: string;
  nome: string;
  login: string;
  createdAt: Date;

  constructor(id: string, nome: string, login: string, createdAt: Date) {
    this.id = id;
    this.nome = nome;
    this.login = login;
    this.createdAt = createdAt;
  }
}
