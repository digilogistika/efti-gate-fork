import { Component } from '@angular/core';
import { NgbNavModule } from "@ng-bootstrap/ng-bootstrap";
import {Router, RouterLink} from "@angular/router";
import {NgIf} from "@angular/common";
import {TranslateModule} from "@ngx-translate/core";
import {FormsModule} from "@angular/forms";
import {LocalStorageService} from "../../core/services/local-storage.service";
import {LoginService} from "../../core/services/login.service";

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [NgbNavModule, RouterLink, NgIf, TranslateModule, FormsModule],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent {

  autoPolling: boolean = true;
  constructor(private readonly loginService: LoginService, private readonly router: Router, private readonly localStorageService: LocalStorageService) {
    this.localStorageService.saveAutoPolling(this.autoPolling);
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
}
