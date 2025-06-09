import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiKeyService {
  private readonly apiKeySubject = new BehaviorSubject<string | null>(null);

  setApiKey(apiKey: string): void {
    this.apiKeySubject.next(apiKey);
  }

  getApiKey(): string | null {
    return this.apiKeySubject.value;
  }

  clearApiKey(): void {
    this.apiKeySubject.next(null);
  }
}
