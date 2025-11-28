import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'gi-menu-principal-item',
    imports: [CommonModule],
    templateUrl: './menu-principal-item.component.html',
    styleUrl: './menu-principal-item.component.css'
})
export class MenuPrincipalItemComponent {
  private router: Router = inject(Router);

  @Input() grupo: GrupoMenu | undefined;
  @Input() somenteIcone = false;
  @Output() goToEvent = new EventEmitter<string>();

  goTo(url?: string) {
    if (url) {
      this.goToEvent.emit(url);
      this.router.navigate([url]);
    }    
  }
}

export interface GrupoMenu {
  nome: string;  
  icone: string;
  url?: string;
}