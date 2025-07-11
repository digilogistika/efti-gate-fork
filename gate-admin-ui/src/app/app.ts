import { Component, inject } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { Header } from "./header/header";
import { AuthService } from './authentication/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterOutlet, Header, CommonModule],
  templateUrl: './app.html',
})
export class App {
  public authService = inject(AuthService);
}
