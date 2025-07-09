import {Component, HostListener} from '@angular/core';
import {NavigationEnd, Router, RouterLink, RouterLinkActive} from '@angular/router';
import {AuthService} from '../auth/auth.service';
import {filter} from 'rxjs';
import {NgOptimizedImage, UpperCasePipe} from '@angular/common';
import {TranslatePipe, TranslateService, LangChangeEvent} from '@ngx-translate/core';

@Component({
  selector: 'app-navbar',
  imports: [
    RouterLink,
    NgOptimizedImage,
    TranslatePipe,
    RouterLinkActive,
    UpperCasePipe
  ],
  templateUrl: './navbar.html'
})
export class Navbar {
  protected isAdminSecretActivated: boolean = false;
  protected isAuthenticated = false;
  protected burgerMenuOpen: boolean = false;
  protected languageDropdownOpen: boolean = false;
  private keySequence: string[] = [];
  private readonly adminSequence = ['a', 'd', 'm', 'i', 'n'];
  public currentLanguage: string;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    protected readonly translate: TranslateService
  ) {
    this.updateAuthStatus();
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.updateAuthStatus();
    });
    this.currentLanguage = this.translate.currentLang;
    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      this.currentLanguage = event.lang;
    });
  }
  protected readonly localStorage = localStorage;

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
    if (event.ctrlKey || event.altKey) return;
    this.keySequence.push(event.key.toLowerCase());
    if (this.keySequence.length > this.adminSequence.length) {
      this.keySequence.shift();
    }
    if (JSON.stringify(this.keySequence) === JSON.stringify(this.adminSequence)) {
      this.isAdminSecretActivated = true;
      this.keySequence = [];
    }
  }

  burgerMenuToggle() {
    this.burgerMenuOpen = !this.burgerMenuOpen;
    if (this.burgerMenuOpen) {
      this.languageDropdownOpen = false;
    }
  }

  toggleLanguageDropdown() {
    this.languageDropdownOpen = !this.languageDropdownOpen;
    if (this.languageDropdownOpen) {
      this.burgerMenuOpen = false;
    }
  }

  selectLanguage(lang: string) {
    this.translate.use(lang);
    this.localStorage.setItem('language', lang);
    this.languageDropdownOpen = false;
  }
}
