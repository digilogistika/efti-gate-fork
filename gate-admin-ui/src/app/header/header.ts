import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';
import {environment} from '../../environments/environment';
import {FormsModule} from '@angular/forms';

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
  apiKeyValue: any;

  constructor() { }
 toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
  }
}
