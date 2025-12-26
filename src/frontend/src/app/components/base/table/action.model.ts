export interface ActionModel<T> {
  icon: string;
  title: string;
  action: (rowData: T) => void;
  iconType?: 'material-icons' | 'material-symbols-outlined';
}
