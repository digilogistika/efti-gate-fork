import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SessionService {

  constructor() {}

  isAuthenticated(): boolean {
    return true;
  }

  logout(): void {
    window.open('/redirect_uri?logout=', '_self');
  }
}
