export interface PerfilModuloDTO {
  id?: string;
  perfilId: string;
  moduloId: string;
  moduloNome: string;
  podeListar: boolean;
  podeVisualizar: boolean;
  podeEditar: boolean;
  podeDeletar: boolean;
}
