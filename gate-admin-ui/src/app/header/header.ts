import {Component} from "@angular/core";
import {RouterLink} from "@angular/router";
import {environment} from "../../environments/environment";
import {AuthService} from "../authentication/auth.service";
import {HttpClient} from '@angular/common/http';

@Component({
  selector: "app-header",
  standalone: true,
  imports: [RouterLink],
  templateUrl: "./header.html",
})
export class Header {
  gate = environment.gateId;
  isMobileMenuOpen = false;
  version: string = ""

  constructor(private authService: AuthService, private http: HttpClient) {
    http.get<any>("/v3/api-docs").subscribe(apiDocs => {
      this.version = apiDocs.info.version
    })
  }

  logout(): void {
    this.authService.logout();
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
  }
}
