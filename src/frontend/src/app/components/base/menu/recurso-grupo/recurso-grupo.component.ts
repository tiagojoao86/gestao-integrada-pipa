import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { Recurso, RecursoComponent } from '../recurso/recurso.component';

@Component({
    selector: 'gi-recurso-grupo',
    imports: [CommonModule, RecursoComponent],
    templateUrl: './recurso-grupo.component.html',
    styleUrl: './recurso-grupo.component.css'
})
export class RecursoGrupoComponent {
  
  @Input() recursoGrupo: GrupoRecurso | undefined;

}

export interface GrupoRecurso {
    nome: string;
    recursos: Recurso[];
}
