import { Injectable, inject } from "@angular/core";
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
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly apiKeySubject: BehaviorSubject<string | null>;
  public readonly isAuthenticated$: Observable<boolean>;

  constructor() {
    const storedApiKey = sessionStorage.getItem(API_KEY_STORAGE_KEY);
    this.apiKeySubject = new BehaviorSubject<string | null>(storedApiKey);
    this.isAuthenticated$ = this.apiKeySubject.pipe(map(key => !!key));
  }

  getApiKey(): string | null {
    return this.apiKeySubject.value;
  }

  /**
   * Attempts to log in by validating the API key against a real, protected endpoint.
   * @param apiKey The API key to validate.
   * @returns An observable of `true` for success, `false` for failure.
   */
  login(apiKey: string): Observable<boolean> {
    const validationUrl = environment.apiUrl.getMetaData;

    console.log(`Validating API key against the endpoint: ${validationUrl}`);

    // We manually create the headers here for this one-time validation call.
    const headers = { 'X-API-Key': apiKey };

    return this.http.get(validationUrl, { headers, observe: 'response' }).pipe(
      map(response => {
        if (response.ok) {
          console.log("API Key validation successful.");
          // If the key is valid, store it in sessionStorage and update the app state.
          this.setAndStoreApiKey(apiKey);
          return true;
        }
        return false;
      }),
      catchError(error => {
        // This will now catch real errors from your backend (like 401 Unauthorized)
        // if the key is actually invalid.
        console.error("API Key validation failed:", error);
        this.clearApiKey(); // Clear any bad key.
        return of(false); // Signal that the login failed.
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

  setApiKey(apiKey: string) {
    console.log("Setting api key to: ", apiKey);
    this.apiKeySubject.next(apiKey);
  }

}
