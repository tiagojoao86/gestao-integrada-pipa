import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from './auth-service';

export const moduleAuthorityGuard = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const moduleKey = route.data['moduleKey'];

  if (!moduleKey) {
    console.error('A "moduleKey" não foi definida na propriedade "data" da rota.');
    router.navigate(['/']);
    return false;
  }

  if (!authService.hasAuthorityListarToModulo(moduleKey)) {
    // Opcional: Adicionar uma notificação para o usuário informando a falta de permissão.
    router.navigate(['/']);
    return false;
  }

  return true;
};
