import { Injectable } from '@angular/core';
import {Identifiers} from "../models/identifiers.model";

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {

  private readonly autoPolling: string = "AUTO-POLLING";
  private readonly authToken: string = "AUTH-TOKEN";
  private readonly currentLanguage: string = "CURRENT-LANGUAGE";

  addIdentifiers(identifiers: Identifiers) {
    localStorage.setItem(identifiers.datasetId, JSON.stringify(identifiers));
  }

  getIdentifiers(id: string) : Identifiers {
    return JSON.parse(localStorage.getItem(id)!);
  }

  saveAutoPolling(value: boolean) {
    localStorage.setItem(this.autoPolling, JSON.stringify(value))
  }

  getAutoPolling() : boolean {
    return JSON.parse(localStorage.getItem(this.autoPolling)!);
  }

  saveAuthToken(token: string): void {
    localStorage.setItem(this.authToken, token);
  }

  getAuthToken(): string | null {
    return localStorage.getItem(this.authToken);
  }

  removeAuthToken(): void {
    localStorage.removeItem(this.authToken);
  }

  hasAuthToken(): boolean {
    return !!this.getAuthToken();
  }

  saveCurrentLanguage(language: string): void {
    localStorage.setItem(this.currentLanguage, language);
  }

  getCurrentLanguage(): string | null {
    return localStorage.getItem(this.currentLanguage);
  }

  removeCurrentLanguage(): void {
    localStorage.removeItem(this.currentLanguage);
  }
}
