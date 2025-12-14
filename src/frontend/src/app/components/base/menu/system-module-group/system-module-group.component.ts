import { Component, Input } from '@angular/core';
import { SystemModuleComponent } from '../system-module/system-module.component';
import { SystemModuleGroup } from './system-module-group';

@Component({
  selector: 'gi-system-module-group',
  imports: [SystemModuleComponent],
  templateUrl: './system-module-group.component.html',
  styleUrl: './system-module-group.component.css',
})
export class SystemModuleGroupComponent {
  @Input() systemModuleGroup: SystemModuleGroup | undefined;
}
