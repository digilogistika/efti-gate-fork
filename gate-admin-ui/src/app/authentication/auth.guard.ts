import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';

export const authGuard: CanActivateFn = (): Observable<boolean | UrlTree> => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.isAuthenticated$.pipe(
    take(1), // Check the auth state once and complete.
    map(isAuthenticated => {
      if (isAuthenticated) {
        return true;
      }
      console.log('AuthGuard: Access denied. Redirecting to /login.');
      return router.createUrlTree(['/login']);
    })
  );
};
