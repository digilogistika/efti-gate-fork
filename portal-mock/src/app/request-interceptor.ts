import { HttpInterceptorFn } from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from './auth/auth.service';
import {catchError, switchMap, throwError} from 'rxjs';
import {Router} from '@angular/router';

export const requestInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Allow public endpoints, admin endpoints, and static assets (like translation files)
  if (req.url.startsWith("/api/public") ||
    req.url.startsWith("/api/admin") ||
    req.url.startsWith("/i18n/")) {
    return next(req);
  }

  return authService.isAuthenticatedUser().pipe(
    switchMap(isAuthenticated => {
      const jwtToken = authService.getJwtToken();

      if (!isAuthenticated || jwtToken === null) {
        router.navigate(['/login']);
        return throwError(() => new Error('User is not authenticated'));
      }

      const authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${jwtToken}`,
        },
      });

      return next(authReq);
    }),
    catchError(error => {
      return throwError(() => error);
    })
  );
};
