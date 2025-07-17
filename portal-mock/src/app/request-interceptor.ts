import {HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from './auth/auth.service';
import {catchError, throwError} from 'rxjs';

export const requestInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Allow public endpoints, admin endpoints, and static assets (like translation files)
  if (req.url.startsWith("/api/public/authority-user/verify") ||
    req.url.startsWith("/api/admin") ||
    req.url.startsWith("/i18n/")) {
    return next(req);
  }

  const jwtToken = authService.getJwtToken();
  let authReq = req;

  if (jwtToken) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${jwtToken}`,
      },
    }
    );
    console.log(authReq);
  }

  return next(authReq).pipe(
    catchError(error => {
      if (error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403)) {
        console.error('Authentication error from interceptor, logging out.', error);
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};
