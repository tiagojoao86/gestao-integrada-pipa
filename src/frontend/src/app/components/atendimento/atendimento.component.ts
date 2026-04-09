import { Component, OnInit, inject } from '@angular/core';
import { BaseComponent } from '../base/base.component';
import { SystemModuleGroupComponent } from '../base/menu/system-module-group/system-module-group.component';
import { AuthService } from '../base/auth/auth-service';
import { SystemModuleGroup } from '../base/menu/system-module-group/system-module-group';
import { SystemModule } from '../base/menu/system-module/system-module';
import { SystemModuleKey } from '../base/enum/system-module-key.enum';

@Component({
  selector: 'gi-atendimento',
  standalone: true,
  imports: [BaseComponent, SystemModuleGroupComponent],
  templateUrl: './atendimento.component.html',
  styleUrl: './atendimento.component.css',
})
export class AtendimentoComponent implements OnInit {
  title: string = $localize`Atendimento`;
  systemModules: SystemModuleGroup[] = [];

  private authService: AuthService = inject(AuthService);

  ngOnInit(): void {
    const modulos: SystemModule[] = [];

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.ATENDIMENTO_PROFISSIONAL)) {
      modulos.push({
        name: $localize`Profissionais`,
        icon: 'medical_services',
        url: '/atendimento/profissional',
      });
    }

    if (modulos.length > 0) {
      this.systemModules.push({
        name: $localize`Cadastros`,
        systemModules: modulos,
      });
    }
  }
}
