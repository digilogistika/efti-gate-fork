import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";

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

    isAuthenticatedUser(): boolean {
        return this.isAuthenticated;
    }

    logout(): void {
        localStorage.removeItem(this.LOCALSTORAGE_KEY);
        this.isAuthenticated = false;
    }
}
