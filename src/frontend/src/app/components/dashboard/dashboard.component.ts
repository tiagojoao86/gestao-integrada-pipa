import { Component } from '@angular/core';
import { BaseComponent } from '../base/base.component';

import { SystemModuleGroupComponent } from '../base/menu/system-module-group/system-module-group.component';
import { SystemModuleGroup } from '../base/menu/system-module-group/system-module-group';

@Component({
  selector: 'gi-dashboard',
  imports: [BaseComponent, SystemModuleGroupComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
  standalone: true,
})
export class DashboardComponent {
  title: string = $localize`Dashboards`;
  systemModuleGroups: SystemModuleGroup[] = [];
}
