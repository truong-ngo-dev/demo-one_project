import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.loadCurrentUser().pipe(
    map(user => {
      if (!user) return router.createUrlTree(['/']);
      const isAdmin = user.roles.some(r => r.name === 'ADMIN');
      return isAdmin ? true : router.createUrlTree(['/app/dashboard']);
    }),
  );
};
