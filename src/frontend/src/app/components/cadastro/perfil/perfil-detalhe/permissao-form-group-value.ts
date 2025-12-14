export interface PermissaoFormGroupValue {
  id?: string;
  perfilId: string;
  moduloId: string;
  moduloNome: string;
  chave: string;
  grupo: string;
  selecionado: boolean;
  podeListar: boolean;
  podeVisualizar: boolean;
  podeEditar: boolean;
  podeDeletar: boolean;
}
