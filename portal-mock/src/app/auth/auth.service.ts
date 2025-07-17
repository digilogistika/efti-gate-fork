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
    private readonly API_KEY_STORAGE = 'api-key';

    constructor(
      private readonly http: HttpClient,
      private readonly router: Router
    ) {
      this.isAuthenticated = !!localStorage.getItem(this.LOCALSTORAGE_KEY) ||
        !!localStorage.getItem(this.API_KEY_STORAGE);

      this.checkForApiKey();
    }

    private checkForApiKey() {
      const searchParams = new URLSearchParams(window.location.search);

      const apiKey = searchParams.get('key');

      console.log('Current URL:', window.location.href);

      if (apiKey) {
        console.log('API Key found using URLSearchParams:', apiKey);
        localStorage.setItem(this.API_KEY_STORAGE, apiKey);
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

    getApiKey(): string | null {
      return localStorage.getItem(this.API_KEY_STORAGE);
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
      const apiKey = this.getApiKey();
      const jwt = this.getJwtToken();

      if (apiKey) {
        return this.http.get("/api/validate-session", { observe: "response" }).pipe(
          map(response => {
            // If the request succeeds (status 2xx), the API key is valid.
            this.isAuthenticated = true;
            return true;
          }),
          catchError(error => {
            console.error('API Key validation failed! The key is likely invalid or expired.', error);
            localStorage.removeItem(this.API_KEY_STORAGE);
            this.isAuthenticated = false;
            return of(false);
          })
        );
      }

      if (jwt) {
        return this.http.post("/api/public/authority-user/validate", jwt, { observe: "response" }).pipe(
          map(response => {
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
        localStorage.removeItem(this.API_KEY_STORAGE);
        this.router.navigate(['/login'], { replaceUrl: true });
    }
}
