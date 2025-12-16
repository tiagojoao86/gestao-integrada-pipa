export class PerfilGridDTO {
  id: string;
  nome: string;
  createdAt: Date;

  constructor(id: string, nome: string, createdAt: Date) {
    this.id = id;
    this.nome = nome;
    this.createdAt = createdAt;
  }
}
