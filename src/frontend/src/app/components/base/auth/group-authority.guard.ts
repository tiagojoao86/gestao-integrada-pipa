import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth-service';

export const groupAuthorityGuard: CanActivateFn = (route, _state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const group = route.data['group'];

  if (authService.hasAuthorityToGrupo(group)) {
    return true;
  } else {
    router.navigate(['/']);
    return false;
  }
};
