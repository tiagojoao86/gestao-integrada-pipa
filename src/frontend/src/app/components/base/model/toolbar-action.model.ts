export interface ToolbarActionModel {
  action: () => void;
  icon: string;
  title: string;
  shortcut?: string;
  value?: string;
  iconType?: 'material-icons' | 'material-symbols-outlined';
}
