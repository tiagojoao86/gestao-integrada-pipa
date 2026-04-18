import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CepResponse {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
}

export interface LogradouroResponse {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
}

@Injectable({ providedIn: 'root' })
export class CepService {
  private readonly baseUrl = '/api/cep';
  private http = inject(HttpClient);

  consultar(cep: string): Observable<CepResponse> {
    const cepNormalizado = cep.replace(/\D/g, '');
    return this.http.get<CepResponse>(`${this.baseUrl}/${cepNormalizado}`);
  }

  listarCidades(uf: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/cidades/${uf}`);
  }

  buscarLogradouros(uf: string, cidade: string, q: string): Observable<LogradouroResponse[]> {
    const params = { cidade, q };
    return this.http.get<LogradouroResponse[]>(`${this.baseUrl}/logradouros/${uf}`, { params });
  }
}
