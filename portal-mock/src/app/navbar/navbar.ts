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
    navbarClass: 'bg-blue-400/70',
    flagPath: '/Flag_of_Estonia.svg.png',
    policeLogoPath: '/Estonian_Police.png'
  },
  'pl': {
    navbarClass: 'bg-red-500/70',
    flagPath: '/Flag_of_Poland.svg.png',
    policeLogoPath: '/Badge_of_Polish_Police.png'
  },
  'lv': {
    navbarClass: 'bg-red-800/60',
    flagPath: '/Flag_of_Latvia.svg.png',
    policeLogoPath: '/Latvia_Police.png'
  },
  'lt': {
    navbarClass: 'bg-yellow-600/80',
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

  // Dynamic state for visuals
  protected navbarBgClass: string = 'bg-white/70';
  protected flagPath: string | null = null;
  protected policeLogoPath: string | null = null;

  // Dynamic classes for DESKTOP navbar elements
  protected navLinkClass: string = '';
  protected navLinkActiveClass: string = '';
  protected logoutBtnClass: string = '';
  protected langBtnClass: string = '';
  protected langBtnIconClass: string = '';
  protected mobileBurgerClass: string = '';

  // Dynamic classes for MOBILE menu elements
  protected mobileMenuBgClass: string = '';
  protected mobileNavLinkClass: string = '';
  protected mobileNavLinkActiveClass: string = '';
  protected mobileLogoutBtnClass: string = '';
  protected mobileDividerClass: string = ''; // New property for the divider

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

    this.currentLanguage = this.translate.currentLang || 'en';
    this.updateVisualsForLanguage(this.currentLanguage);

    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      this.currentLanguage = event.lang;
      this.updateVisualsForLanguage(this.currentLanguage);
    });
  }
  protected readonly localStorage = localStorage;

  private updateVisualsForLanguage(lang: string): void {
    const assets = LANGUAGE_ASSETS[lang];
    const baseLinkLayout = 'flex items-center px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 hover:shadow-lg';
    const mobileBaseLinkLayout = 'flex items-center px-4 py-2 rounded-full text-sm font-medium transition-all duration-300';

    if (assets) {
      // THEMED styles for Estonian, Latvian, etc.
      this.navbarBgClass = assets.navbarClass;
      this.flagPath = assets.flagPath;
      this.policeLogoPath = assets.policeLogoPath;
      this.navLinkClass = `${baseLinkLayout} text-white hover:bg-white/20`;
      this.navLinkActiveClass = 'bg-white/25 shadow-md';
      this.logoutBtnClass = `${baseLinkLayout} text-white hover:bg-red-500/75`;
      this.langBtnClass = 'flex items-center px-3 py-2 rounded-full text-sm font-medium text-white bg-black/10 hover:bg-white/20 transition-all duration-300 group';
      this.langBtnIconClass = 'w-5 h-5 mr-2 text-white transition-all duration-300';
      this.mobileBurgerClass = 'inline-flex items-center justify-center p-2 rounded-full text-white hover:bg-white/20 transition-all duration-300';

      // Themed mobile menu styles
      this.mobileMenuBgClass = 'bg-transparent backdrop-blur-xl';
      this.mobileNavLinkClass = `${mobileBaseLinkLayout} text-white hover:bg-white/20`;
      this.mobileNavLinkActiveClass = 'bg-white/25 shadow-md';
      this.mobileLogoutBtnClass = `${mobileBaseLinkLayout} w-full text-white hover:bg-red-500/75`;
      this.mobileDividerClass = 'border-white/20'; // Themed divider color

    } else {
      // DEFAULT styles for English
      this.navbarBgClass = 'bg-white/70';
      this.flagPath = null;
      this.policeLogoPath = null;
      this.navLinkClass = `${baseLinkLayout} text-gray-700 hover:bg-white/80 hover:text-indigo-600`;
      this.navLinkActiveClass = 'bg-gradient-to-r from-indigo-500/20 to-purple-500/20 text-indigo-700';
      this.logoutBtnClass = `${baseLinkLayout} text-gray-700 hover:bg-red-50/80 hover:text-red-600`;
      this.langBtnClass = 'flex items-center px-3 py-2 rounded-full text-sm font-medium text-gray-700 bg-white/80 hover:bg-indigo-50/80 hover:text-indigo-600 transition-all duration-300 group';
      this.langBtnIconClass = 'w-5 h-5 mr-2 text-gray-700 group-hover:text-indigo-600 transition-all duration-300';
      this.mobileBurgerClass = 'inline-flex items-center justify-center p-2 rounded-full text-gray-700 hover:text-indigo-600 hover:bg-white/80 transition-all duration-300';

      // Default mobile menu styles
      this.mobileMenuBgClass = 'bg-white/90 backdrop-blur-xl';
      this.mobileNavLinkClass = `${mobileBaseLinkLayout} text-gray-700 hover:bg-white/80 hover:text-indigo-600`;
      this.mobileNavLinkActiveClass = 'bg-gradient-to-r from-indigo-500/20 to-purple-500/20 text-indigo-700';
      this.mobileLogoutBtnClass = `${mobileBaseLinkLayout} w-full text-gray-700 hover:bg-red-50/80 hover:text-red-600`;
      this.mobileDividerClass = 'border-gray-200/70'; // Default divider color
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
