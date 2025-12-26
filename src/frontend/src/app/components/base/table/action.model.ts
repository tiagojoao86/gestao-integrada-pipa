export interface ActionModel<T> {
  icon: string;
  action: (rowData: T) => void;
  iconType?: 'material-icons' | 'material-symbols-outlined';
}
