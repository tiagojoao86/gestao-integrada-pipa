export interface FilterDTO {
  filterLogicOperator: string;
  items: FilterItem[];
}

export interface FilterItem {
  property: string;
  values: unknown[];
  operator: FilterOperator;
}

export class FilterLogicOperator {
  static readonly AND: FilterLogicOperator = new FilterLogicOperator(
    'E',
    'AND'
  );
  static readonly OR: FilterLogicOperator = new FilterLogicOperator('OU', 'OR');

  private label: string;
  private key: string;

  constructor(label: string, key: string) {
    this.label = label;
    this.key = key;
  }

  static getAll(): FilterLogicOperator[] {
    return [this.AND, this.OR];
  }

  getKey() {
    return this.key;
  }

  getLabel() {
    return this.label;
  }
}

export class FilterOperator {
  static readonly EQ: FilterOperator = new FilterOperator('Igual', 'EQ');
  static readonly NEQ: FilterOperator = new FilterOperator('Diferente', 'NEQ');
  static readonly GT: FilterOperator = new FilterOperator('Maior', 'GT');
  static readonly LT: FilterOperator = new FilterOperator('Menor', 'LT');
  static readonly GE: FilterOperator = new FilterOperator(
    'Maior ou Igual',
    'GE'
  );
  static readonly LE: FilterOperator = new FilterOperator(
    'Menor ou Igual',
    'LE'
  );
  static readonly CONTAINS: FilterOperator = new FilterOperator(
    'Contém',
    'CONTAINS'
  );
  static readonly NOT_CONTAINS: FilterOperator = new FilterOperator(
    'Não Contém',
    'NOT_CONTAINS'
  );
  static readonly IN: FilterOperator = new FilterOperator('Está em', 'IN');
  static readonly NOT_IN: FilterOperator = new FilterOperator(
    'Não está em',
    'NOT_IN'
  );
  static readonly BT: FilterOperator = new FilterOperator('Entre', 'BT');

  label: string;
  key: string;

  constructor(label: string, key: string) {
    this.label = label;
    this.key = key;
  }

  static getAll(): FilterOperator[] {
    return [
      FilterOperator.EQ,
      FilterOperator.NEQ,
      FilterOperator.GT,
      FilterOperator.LT,
      FilterOperator.GE,
      FilterOperator.LE,
      FilterOperator.CONTAINS,
      FilterOperator.NOT_CONTAINS,
      FilterOperator.IN,
      FilterOperator.BT,
    ];
  }
}
