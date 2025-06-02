import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { CanActivateFn } from '@angular/router';
import { SessionService } from '../services/session.service';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const sessionService = inject(SessionService);

  if (!sessionService.isAuthenticated()) {
    router.navigate(['login']).then(() => {
      location.reload();
    });
    return false;
  }

  return true;
};
