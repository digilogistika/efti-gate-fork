import { Injectable } from '@angular/core';
import {CanActivate, Router, UrlTree} from '@angular/router';
import {AuthService} from './auth.service';
import {catchError, map, Observable, of} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate  {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.isAuthenticatedUser().pipe(
      map(isAuthenticated => {
        if (isAuthenticated) {
          return true; // User is authenticated, allow navigation
        } else {
          return this.router.createUrlTree(['/login']);
        }
      }),
      catchError(error => {
        console.error('Authentication check failed:', error);
        return of(this.router.createUrlTree(['/login']));
      })
    );
  }
}
