import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {AuthService} from './auth.service';
import {catchError, map, Observable, of} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate  {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    return this.authService.isAuthenticatedUser().pipe(
      map(isAuthenticated => {
        if (isAuthenticated) {
          return true; // User is authenticated, allow navigation
        } else {
          // User is not authenticated, redirect to login and prevent navigation
          // Return a UrlTree to trigger redirection
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
