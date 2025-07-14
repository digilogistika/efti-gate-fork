import { Component } from "@angular/core";
import { RouterLink } from "@angular/router";
import { environment } from "../../environments/environment";
import { AuthService } from "../authentication/auth.service";

@Component({
  selector: "app-header",
  standalone: true,
  imports: [RouterLink],
  templateUrl: "./header.html",
})
export class Header {
  gate = environment.gateId;
  isMobileMenuOpen = false;

  constructor(private authService: AuthService) {}

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
