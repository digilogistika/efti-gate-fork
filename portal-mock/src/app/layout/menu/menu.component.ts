import { Component, OnInit } from '@angular/core';
import { NgbNavModule, NgbDropdownModule } from "@ng-bootstrap/ng-bootstrap";
import {Router, RouterLink} from "@angular/router";
import {NgIf} from "@angular/common";
import {TranslateModule, TranslateService} from "@ngx-translate/core";
import {FormsModule} from "@angular/forms";
import {LocalStorageService} from "../../core/services/local-storage.service";
import {LoginService} from "../../core/services/login.service";

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [NgbNavModule, NgbDropdownModule, RouterLink, NgIf, TranslateModule, FormsModule],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent implements OnInit {

  autoPolling: boolean = true;
  currentLanguage: string = 'en';

  private readonly languageNames: { [key: string]: string } = {
    'en': 'English',
    'lt': 'Lietuvių',
    'lv': 'Latviešu',
    'pl': 'Polski'
  };

  constructor(
    private readonly loginService: LoginService,
    private readonly router: Router,
    private readonly localStorageService: LocalStorageService,
    private readonly translateService: TranslateService
  ) {
    this.localStorageService.saveAutoPolling(this.autoPolling);
  }

  ngOnInit() {
    const savedLanguage = this.localStorageService.getCurrentLanguage();
    if (savedLanguage) {
      this.currentLanguage = savedLanguage;
      this.translateService.use(savedLanguage);
    } else {
      this.translateService.setDefaultLang('en');
      this.translateService.use('en');
    }
  }

  public isAuthenticated() {
    return this.loginService.isLoggedIn();
  }

  public login() {
    this.router.navigate(["login"]).then(() => {
      location.reload();
    });
  }

  public logout() {
    this.loginService.logout();
  }

  public isLogged() {
    return this.loginService.isLoggedIn()
  }

  public updatePollingValue(value: any) {
    this.autoPolling = value.currentTarget.checked;
    this.localStorageService.saveAutoPolling(value.currentTarget.checked);
  }

  public changeLanguage(language: string) {
    this.currentLanguage = language;
    this.translateService.use(language);
    this.localStorageService.saveCurrentLanguage(language);
  }

  public getCurrentLanguageName(): string {
    return this.languageNames[this.currentLanguage] || 'English';
  }
}
