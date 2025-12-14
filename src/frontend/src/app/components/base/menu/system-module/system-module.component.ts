import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { Router } from '@angular/router';
import { SystemModule } from './system-module';

@Component({
  selector: 'gi-system-module',
  imports: [CommonModule],
  templateUrl: './system-module.component.html',
  styleUrl: './system-module.component.css',
})
export class SystemModuleComponent {
  @Input() systemModule: SystemModule | undefined;
  private router: Router = inject(Router);

  goTo(url: string) {
    this.router.navigate([url]);
  }

  getFontSizeClass(): string {
    if (!this.systemModule?.name) return '';

    const length = this.systemModule.name.length;

    if (length <= 10) return 'font-normal';
    if (length <= 15) return 'font-small';
    if (length <= 20) return 'font-smaller';
    return 'font-tiny';
  }
}
