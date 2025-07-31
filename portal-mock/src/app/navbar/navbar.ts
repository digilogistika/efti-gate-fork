import {Component, HostListener} from '@angular/core';
import {NavigationEnd, Router, RouterLink, RouterLinkActive} from '@angular/router';
import {AuthService} from '../auth/auth.service';
import {filter} from 'rxjs';
import {NgClass, NgOptimizedImage, UpperCasePipe} from '@angular/common';
import {TranslatePipe, TranslateService, LangChangeEvent} from '@ngx-translate/core';

interface LanguageAssetConfig {
  navbarClass: string;
  flagPath: string;
  policeLogoPath: string;
}

const LANGUAGE_ASSETS: { [key: string]: LanguageAssetConfig } = {
  'et': {
    navbarClass: 'bg-blue-100/70',
    flagPath: '/Flag_of_Estonia.svg.png',
    policeLogoPath: '/Estonian_Police.png'
  },
  'pl': {
    navbarClass: 'bg-red-100/70',
    flagPath: '/Flag_of_Poland.svg.png',
    policeLogoPath: '/Badge_of_Polish_Police.png'
  },
  'lv': {
    navbarClass: 'bg-red-100/70',
    flagPath: '/Flag_of_Latvia.svg.png',
    policeLogoPath: '/Latvia_Police.png'
  },
  'lt': {
    navbarClass: 'bg-green-100/70',
    flagPath: '/Flag_of_Lithuania.svg.png',
    policeLogoPath: '/Logo_of_the_Police_of_Lithuania.svg.png'
  }
};

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    RouterLink,
    NgOptimizedImage,
    TranslatePipe,
    RouterLinkActive,
    UpperCasePipe,
    NgClass
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

  // Properties to hold the current state
  protected navbarBgClass: string = 'bg-white/70';
  protected flagPath: string | null = null;
  protected policeLogoPath: string | null = null;

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
    this.updateVisualsForLanguage(this.currentLanguage);

    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      this.currentLanguage = event.lang;
      this.updateVisualsForLanguage(this.currentLanguage);
    });
  }
  protected readonly localStorage = localStorage;

  private updateVisualsForLanguage(lang: string): void {
    const assets = LANGUAGE_ASSETS[lang];
    if (assets) {
      this.navbarBgClass = assets.navbarClass;
      this.flagPath = assets.flagPath;
      this.policeLogoPath = assets.policeLogoPath;
    } else {
      // Default state for English or any other language
      this.navbarBgClass = 'bg-white/70';
      this.flagPath = null;
      this.policeLogoPath = null;
    }
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
