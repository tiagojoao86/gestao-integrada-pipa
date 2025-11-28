import { CommonModule } from '@angular/common';
import { Component, HostListener, inject, Input } from '@angular/core';
import { Location } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { Toolbar } from 'primeng/toolbar';
import { BadgeModule } from 'primeng/badge';

@Component({
  selector: 'gi-app-base',
  imports: [CommonModule, ButtonModule, Toolbar, BadgeModule],
  templateUrl: './base.component.html',
  styleUrl: './base.component.css',
})
export class BaseComponent {
  private location: Location = inject(Location);

  @Input() title: string = $localize `Título`;
  @Input() actions: RegisterActionToolbar[] = [];
  @Input() hideFooter = false;
  @Input() hideToolbar = false;
  @Input() goBackFn: (() => void) | null = null;
  
  goBack() {
    if (this.goBackFn) {
      this.goBackFn();
      return;
    }

    this.location.back();
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {    
    const shortcut = this.buildShortcutString(event);

    const action = this.actions.find(a => a.shortcut?.toLowerCase() === shortcut);

    if (action) {
      event.preventDefault();
      action.action();
    }
  }

  private buildShortcutString(event: KeyboardEvent): string {
    const parts: string[] = [];
    if (event.ctrlKey) parts.push('control');
    if (event.altKey) parts.push('alt');
    if (event.shiftKey) parts.push('shift');
    
    parts.push(event.key.toLowerCase());
    return parts.join('.');
  }
}

export interface RegisterActionToolbar {
  action: () => void;
  icon: string;
  title: string;
  shortcut?: string;
  value?: string;
}
