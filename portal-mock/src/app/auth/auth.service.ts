import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {catchError, map, Observable, of, throwError} from "rxjs";
import {Router} from '@angular/router';

interface JwtDto {
    token: string
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private isAuthenticated = false;
    private readonly LOCALSTORAGE_KEY = 'jwt-token';

    constructor(
      private readonly http: HttpClient,
      private readonly router: Router
    ) {
      this.isAuthenticated = !!localStorage.getItem(this.LOCALSTORAGE_KEY)
      this.storeKeyAsJwtIfPresent();
    }

    private storeKeyAsJwtIfPresent() {
      const searchParams = new URLSearchParams(window.location.search);
      const keyFromUrl = searchParams.get('key');
      if (keyFromUrl) {
        console.log('API Key found using URLSearchParams:', keyFromUrl);
        localStorage.setItem(this.LOCALSTORAGE_KEY, keyFromUrl);
        this.isAuthenticated = true;

        const allParams = Object.fromEntries(searchParams.entries());
        delete allParams['key'];

        const currentPath = window.location.pathname;

        this.router.navigate([currentPath], {
          replaceUrl: true,
          queryParams: allParams
        });
      } else {
        console.log('No API Key found in URL.');
      }
    }

    login(email: string, password: string) {
      return this.http
        .post<JwtDto>("/api/public/authority-user/verify", {email, password})
        .pipe(
          map((value) => {
            localStorage.setItem(this.LOCALSTORAGE_KEY, value.token);
            this.isAuthenticated = true;
            return true;
          }),
          catchError((error) => {
            console.error('Login failed:', error);

            let errorMessage = 'Error when logging in user. Please try again.';

            this.isAuthenticated = false;
            return throwError(() => new Error(errorMessage));
          })
        );
    }

    isAuthenticatedUser(): Observable<boolean> {
      const jwt = this.getJwtToken();

      if (jwt) {
        return this.http.post("/api/public/authority-user/validate", jwt).pipe(
          map(() => {
            this.isAuthenticated = true;
            return true;
          }),
          catchError(error => {
            this.isAuthenticated = false;
            localStorage.removeItem(this.LOCALSTORAGE_KEY);
            return of(false);
          })
        );
      }

      // If no API key and no JWT, the user is not authenticated.
      return of(false);
    }

    getJwtToken(): string | null {
      return localStorage.getItem(this.LOCALSTORAGE_KEY)
    }

    logout(): void {
        localStorage.removeItem(this.LOCALSTORAGE_KEY);
        this.isAuthenticated = false;
        this.router.navigate(['/login'], { replaceUrl: true });
    }
}
