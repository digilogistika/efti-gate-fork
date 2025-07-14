import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable, of } from "rxjs";
import { map, catchError } from 'rxjs/operators';
import { HttpClient } from "@angular/common/http";
import { Router } from "@angular/router";
import { environment } from "../../environments/environment";

const API_KEY_STORAGE_KEY = "gate_admin_api_key";

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private readonly apiKeySubject: BehaviorSubject<string | null>;
  public readonly isAuthenticated$: Observable<boolean>;

  constructor(private http: HttpClient, private router: Router) {
    const storedApiKey = sessionStorage.getItem(API_KEY_STORAGE_KEY);
    this.apiKeySubject = new BehaviorSubject<string | null>(storedApiKey);
    this.isAuthenticated$ = this.apiKeySubject.pipe(map(key => !!key));
  }

  getApiKey(): string | null {
    return this.apiKeySubject.value;
  }

  login(apiKey: string): Observable<boolean> {
    const validationUrl = environment.apiUrl.getMetaData;

    console.log(`Validating API key against the endpoint: ${validationUrl}`);

    const headers = { 'X-API-Key': apiKey };

    return this.http.get(validationUrl, { headers, observe: 'response' }).pipe(
      map(response => {
        if (response.ok) {
          console.log("API Key validation successful.");
          this.setAndStoreApiKey(apiKey);
          return true;
        }
        return false;
      }),
      catchError(error => {
        console.error("API Key validation failed:", error);
        this.clearApiKey();
        return of(false);
      })
    );
  }

  logout(): void {
    this.clearApiKey();
    this.router.navigate(['/login']);
  }

  private setAndStoreApiKey(apiKey: string): void {
    sessionStorage.setItem(API_KEY_STORAGE_KEY, apiKey);
    this.apiKeySubject.next(apiKey);
  }

  private clearApiKey(): void {
    sessionStorage.removeItem(API_KEY_STORAGE_KEY);
    this.apiKeySubject.next(null);
  }
}
