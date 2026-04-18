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

@Injectable({ providedIn: 'root' })
export class CepService {
  private readonly baseUrl = '/api/cep';
  private http = inject(HttpClient);

  consultar(cep: string): Observable<CepResponse> {
    const cepNormalizado = cep.replace(/\D/g, '');
    return this.http.get<CepResponse>(`${this.baseUrl}/${cepNormalizado}`);
  }
}
