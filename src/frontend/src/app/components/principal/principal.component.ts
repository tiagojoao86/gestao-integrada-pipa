import { Component, inject, OnInit, ViewEncapsulation } from '@angular/core';

import { Router, RouterModule } from '@angular/router';
import { MainMenuItemComponent } from '../base/menu/main-menu-item/main-menu-item.component';
import {
  animate,
  state,
  style,
  transition,
  trigger,
} from '@angular/animations';
import { Toolbar } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { MessagesComponent } from '../base/messages/messages.component';
import { AuthService } from '../base/auth/auth-service';
import { GroupMenu } from '../base/menu/main-menu-item/group-menu';
import { SystemModuleKey } from '../base/enum/system-module-key.enum';

@Component({
  selector: 'gi-principal-component',
  imports: [
    RouterModule,
    MainMenuItemComponent,
    Toolbar,
    ButtonModule,
    MessagesComponent,
  ],
  templateUrl: './principal.component.html',
  styleUrl: './principal.component.css',
  encapsulation: ViewEncapsulation.None,
  animations: [
    trigger('openClose', [
      state(
        'open',
        style({
          transform: 'translateY(0)',
        })
      ),
      state(
        'closed',
        style({
          transform: 'translateY(-400%)',
        })
      ),
      transition('open => closed', [animate('0.2s')]),
      transition('closed => open', [animate('0.2s')]),
    ]),
  ],
})
export class PrincipalComponent implements OnInit {
  showDrawer = false;
  private router: Router = inject(Router);
  private authService: AuthService = inject(AuthService);
  tituloApp: string = $localize`Gestão Integrada`;
  labelBotaoSair: string = $localize`Sair`;

  openDrawerMenu = {
    nome: $localize`Menu`,
    icone: 'menu',
  };

  menu: GroupMenu[] = [];

  ngOnInit(): void {
    this.buildMenu();
  }

  buildMenu() {
    if (this.authService.hasAuthorityToGrupo(SystemModuleKey.CADASTROS)) {
      this.menu.push({
        name: $localize`Cadastros`,
        icon: 'widgets',
        url: '/cadastro',
      });
    }

    if (this.authService.hasAuthorityToGrupo(SystemModuleKey.FINANCEIRO)) {
      this.menu.push({
        name: $localize`Financeiro`,
        icon: 'attach_money',
        url: '/financeiro',
      });
    }

    if (this.authService.hasAuthorityToGrupo(SystemModuleKey.DASHBOARDS)) {
      this.menu.push({
        name: $localize`Dashboards`,
        icon: 'dashboard',
        url: '/dashboard',
      });
    }

    if (this.authService.hasAuthorityToGrupo('ATENDIMENTO')) {
      this.menu.push({
        name: $localize`Atendimento`,
        icon: 'child_care',
        url: '/atendimento',
      });
    }
  }

  toogleDrawer() {
    this.showDrawer = !this.showDrawer;
  }

  onGoToEvent() {
    this.toogleDrawer();
  }

  goToHome() {
    this.router.navigate(['/']);
  }

  logout() {
    this.authService.logout();
  }

  getUsername() {
    return this.authService.getUsername();
  }

  getName() {
    return this.authService.getNome();
  }

  getTenantId() {
    return this.authService.getTenantId();
  }
}
