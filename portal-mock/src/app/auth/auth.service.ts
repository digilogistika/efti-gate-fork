import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {catchError, map, Observable, of, tap} from "rxjs";

interface JwtDto {
    token: string
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private isAuthenticated = false;
    private readonly LOCALSTORAGE_KEY = 'jwt-token';

    constructor(private readonly http: HttpClient) {
        this.isAuthenticated = !!localStorage.getItem(this.LOCALSTORAGE_KEY);
    }

    login(email: string, password: string) {
        this.http
            .post<JwtDto>("/api/public/authority-user/verify", {email, password})
            .subscribe(value => {
                localStorage.setItem(this.LOCALSTORAGE_KEY, value.token)
                this.isAuthenticated = true;
            })
    }

    isAuthenticatedUser(): Observable<boolean> {
        const jwt = localStorage.getItem(this.LOCALSTORAGE_KEY);

        if (!jwt) {
            this.logout();
            return of(false); // Immediately return false if no JWT is found
        }

        return this.http.post("/api/public/authority-user/validate", jwt, { observe: "response" }).pipe(
            map(response => {
                // If the request succeeds (status 2xx), the JWT is considered valid
                this.isAuthenticated = true;
                return true;
            }),
            catchError(error => {
                // If the request errors (e.g., 401, 403), the JWT is invalid
                this.logout();
                this.isAuthenticated = false; // Update internal state
                return of(false); // Return an observable of false
            }),
            // Optional: If you want to update the internal isAuthenticated state
            // but still return the observable without affecting the map/catchError
            tap(isValid => {
                this.isAuthenticated = isValid;
            })
        );
    }

    logout(): void {
        localStorage.removeItem(this.LOCALSTORAGE_KEY);
        this.isAuthenticated = false;
    }
}
