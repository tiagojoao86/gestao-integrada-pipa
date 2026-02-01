import { BaseService } from '../base-service';

/**
 * Campo de pesquisa/filtro
 */
export interface SearchField {
    key: string;
    label: string;
}

/**
 * Campo a ser exibido nos resultados
 */
export interface ResultField {
    key: string;
    label: string;
}

/**
 * Configuração da busca de entidade
 */
export interface EntitySearchConfig<T> {
    service: BaseService<T, unknown>;
    searchFields: SearchField[];
    resultFields: ResultField[];
    title?: string;
    searchPlaceholder?: string;
    pageSize?: number;
}

/**
 * Resultado da busca (entidade selecionada pelo usuário)
 */
export interface EntitySearchResult<T> {
    entity: T;
    cancelled: boolean;
}
