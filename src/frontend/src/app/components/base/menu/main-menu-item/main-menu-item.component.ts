import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { Router } from '@angular/router';
import { GroupMenu } from './group-menu';

@Component({
  selector: 'gi-main-menu-item',
  imports: [],
  templateUrl: './main-menu-item.component.html',
  styleUrl: './main-menu-item.component.css',
})
export class MainMenuItemComponent {
  private router: Router = inject(Router);

  @Input() groupMenu: GroupMenu | undefined;
  @Input() onlyIcon = false;
  @Output() goToEvent = new EventEmitter<string>();

  goTo(url?: string) {
    if (url) {
      this.goToEvent.emit(url);
      this.router.navigate([url]);
    }
  }
}
