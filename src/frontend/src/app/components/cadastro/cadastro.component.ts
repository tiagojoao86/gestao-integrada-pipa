import { Component, OnInit, inject } from "@angular/core";
import { BaseComponent } from "../base/base.component";
import { CommonModule } from "@angular/common";
import { GrupoRecurso, RecursoGrupoComponent } from "../base/menu/recurso-grupo/recurso-grupo.component";
import { AuthService } from "../base/auth/auth-service";
import { Recurso } from "../base/menu/recurso/recurso.component";

@Component({
    selector: 'gi-cadastro',
    imports: [BaseComponent, CommonModule, RecursoGrupoComponent],
    templateUrl: './cadastro.component.html',
    styleUrl: './cadastro.component.css',
    standalone: true
})
export class CadastroComponent implements OnInit {

    titulo: string = $localize `Cadastros`;
    recursos: GrupoRecurso[] = [];

    private authService: AuthService = inject(AuthService);
    
    ngOnInit(): void {
        const recursosGeral: Recurso[] = [];
        if (this.authService.hasAuthorityListarToModulo('CADASTRO_USUARIO')) {
            recursosGeral.push({nome: $localize `Usuários`, icone: 'person', url: '/cadastro/usuario'});
        }

        if (this.authService.hasAuthorityListarToModulo('CADASTRO_PERFIL')) {
            recursosGeral.push({nome: $localize `Perfis`, icone: 'badge', url: '/cadastro/perfil'});
        }

        if (recursosGeral.length > 0) {
            this.recursos.push({nome: $localize `Geral`, recursos: recursosGeral});
        }
    }
}