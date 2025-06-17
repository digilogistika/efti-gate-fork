import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';
import {environment} from '../../environments/environment';
import {FormsModule} from '@angular/forms';
import {AuthService} from '../authentication/auth.service';

@Component({
  selector: 'app-header',
  imports: [
    RouterLink,
    FormsModule
  ],
  templateUrl: './header.html'
})
export class Header {
  gate = environment.gateId
  isMobileMenuOpen = false;
  apiKeyValue: string = '';

  constructor(private authService: AuthService) {}

  onApiKeyChange() {
    this.authService.setApiKey(this.apiKeyValue);
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
  }
}
