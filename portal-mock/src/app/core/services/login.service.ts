import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environment/environment';
import { JwtResponse} from "../models/jwt-response.model";
import { AuthorityUserModel} from "../models/authority-user.model";
import { LocalStorageService } from './local-storage.service';

const baseUrl = environment.baseUrl;
const url = environment.apiUrl.authUserVerify;

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  constructor(
    private readonly http: HttpClient,
    private readonly localStorageService: LocalStorageService
  ) {}

  login(credentials: AuthorityUserModel): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${baseUrl}${url}`, credentials);
  }

  isLoggedIn(): boolean {
    return this.localStorageService.hasAuthToken();
  }

  logout(): void {
    this.localStorageService.removeAuthToken();
  }

  getToken(): string | null {
    return this.localStorageService.getAuthToken();
  }
}
