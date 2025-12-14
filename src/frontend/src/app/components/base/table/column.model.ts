export interface ColumnModel<T> {
  name: string;
  label: string;
  getValue: (rowData: T) => string | null | undefined;
}
