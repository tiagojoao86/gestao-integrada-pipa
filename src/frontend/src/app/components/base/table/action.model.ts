export interface ActionModel<T> {
  icon: string;
  action: (rowData: T) => void;
}
