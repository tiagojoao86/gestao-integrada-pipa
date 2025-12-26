export class PerfilGridDTO {
  id: string;
  nome: string;
  createdAt: Date;
  deleted?: boolean;

  constructor(id: string, nome: string, createdAt: Date, deleted?: boolean) {
    this.id = id;
    this.nome = nome;
    this.createdAt = createdAt;
    this.deleted = deleted;
  }
}
