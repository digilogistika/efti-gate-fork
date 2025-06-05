import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthorityUserDto, JwtDto } from '../models/auth.model';
import { LocalStorageService } from './local-storage.service';

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private apiUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private localStorageService: LocalStorageService
  ) {}

  /**
   * Logs in the user by verifying credentials with the authority app
   * @param credentials User credentials (email and password)
   * @returns Observable with JWT token
   */
  login(credentials: AuthorityUserDto): Observable<JwtDto> {
    return this.http.post<JwtDto>(`${this.apiUrl}/public/authority-user/verify`, credentials);
  }

  /**
   * Checks if the user is currently logged in
   * @returns Boolean indicating if user is logged in
   */
  isLoggedIn(): boolean {
    return this.localStorageService.hasAuthToken();
  }

  /**
   * Logs out the user by removing the auth token
   */
  logout(): void {
    this.localStorageService.removeAuthToken();
  }

  /**
   * Gets the authentication token
   * @returns The JWT token or null if not logged in
   */
  getToken(): string | null {
    return this.localStorageService.getAuthToken();
  }
}
