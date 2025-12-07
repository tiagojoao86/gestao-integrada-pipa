import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Response } from "../../base/model/response"; // Importar o DTO de Response

@Injectable({
    providedIn: 'root',
})
export class ModuloService {
    
    private http: HttpClient = inject(HttpClient);

    getGroupedModules(): Observable<Response> { // Tipo de retorno corrigido (sem o genérico no Response)
        return this.http.get<Response>('/api/modulo/grouped');
    }
}
