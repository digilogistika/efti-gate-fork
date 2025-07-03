import {Component, HostListener} from '@angular/core';
import {NavigationEnd, Router, RouterLink} from '@angular/router';
import {AuthService} from '../auth/auth.service';
import {filter, Observable} from 'rxjs';
import {AsyncPipe} from '@angular/common';

@Component({
  selector: 'app-navbar',
  imports: [
    RouterLink,
    AsyncPipe
  ],
  templateUrl: './navbar.html'
})
export class Navbar {
  protected isAdminSecretActivated: boolean = false;
  protected isAuthenticated = false;
  private keySequence: string[] = [];
  private readonly adminSequence = ['a', 'd', 'm', 'i', 'n'];

  constructor(private readonly authService: AuthService, private readonly router: Router) {
    this.updateAuthStatus();
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.updateAuthStatus();
    });
  }

  updateAuthStatus(): void {
    this.authService.isAuthenticatedUser().subscribe({
        next: (isAuthenticated) => this.isAuthenticated = isAuthenticated,
        error: (error) => console.error('Error checking authentication status:', error)
      }
    );
  }

  logout() {
    this.authService.logout();
  }

  @HostListener('document:keydown', ['$event'])
  activateAdminSecret(event: KeyboardEvent): void {
    if (event.ctrlKey || event.altKey) return; // ignore modifier combos

    this.keySequence.push(event.key.toLowerCase());

    if (this.keySequence.length > this.adminSequence.length) {
      this.keySequence.shift(); // remove oldest key
    }

    if (JSON.stringify(this.keySequence) === JSON.stringify(this.adminSequence)) {
      this.isAdminSecretActivated = true;
      this.keySequence = []; // reset
    }
  }
}
