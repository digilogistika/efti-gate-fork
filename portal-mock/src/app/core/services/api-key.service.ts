import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable, throwError} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiKeyService {
  private readonly apiKeySubject = new BehaviorSubject<string>('');
  public apiKey$: Observable<string> = this.apiKeySubject.asObservable();

  constructor() {
    // Load API key from sessionStorage on service initialization
    const savedApiKey = sessionStorage.getItem('userApiKey');
    if (savedApiKey) {
      this.apiKeySubject.next(savedApiKey);
    }
  }

  setApiKey(apiKey: string): void {
    this.apiKeySubject.next(apiKey);
    // Store in sessionStorage (will be cleared when browser tab is closed)
    sessionStorage.setItem('userApiKey', apiKey);
  }

  getApiKey(): string {
    return this.apiKeySubject.value;
  }

  clearApiKey(): void {
    this.apiKeySubject.next('');
    sessionStorage.removeItem('userApiKey');
  }

  private hasApiKey(): boolean {
    return this.getApiKey().length > 0;
  }

  checkApiKey(): Observable<never> | null {
    if (!this.hasApiKey()) {
      return throwError(() => new Error('API Key is required. Please configure your API key in Settings first.'));
    }
    return null;
  }
}
