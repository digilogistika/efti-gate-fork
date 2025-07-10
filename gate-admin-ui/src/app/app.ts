import { Component, inject } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { Header } from "./header/header";
import { AuthService } from './authentication/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterOutlet, Header, CommonModule],
  template: `
    @if (authService.isAuthenticated$ | async) {
      <app-header />
    }
    <main class="flex-grow">
      <router-outlet />
    </main>
  `,
  styles: [`
    :host {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }
  `]
})
export class App {
  protected title = "gate-admin-ui";
  public authService = inject(AuthService);
}
