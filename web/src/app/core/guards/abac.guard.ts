import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AbacService } from '../services/abac.service';

export const abacGuard = (elementId: string): CanActivateFn => () => {
  const abacService = inject(AbacService);
  const router = inject(Router);

  return abacService.isPermitted(elementId)
    ? true
    : router.createUrlTree(['/admin/dashboard']);
};
