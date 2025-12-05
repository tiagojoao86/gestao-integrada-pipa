import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'gi-recurso',
  imports: [CommonModule],
  templateUrl: './recurso.component.html',
  styleUrl: './recurso.component.css',
})
export class RecursoComponent {
  @Input() recurso: Recurso | undefined;
  private router: Router = inject(Router);

  goTo(url: string) {
    this.router.navigate([url]);
  }

  getFontSizeClass(): string {
    if (!this.recurso?.nome) return '';

    const length = this.recurso.nome.length;

    if (length <= 10) return 'font-normal';
    if (length <= 15) return 'font-small';
    if (length <= 20) return 'font-smaller';
    return 'font-tiny';
  }
}

export interface Recurso {
  nome: string;
  icone: string;
  url: string;
}
