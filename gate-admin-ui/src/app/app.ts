import { Component } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { Header } from "./header/header";
import { Notification } from "./notification/notification";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterOutlet, Header, Notification],
  templateUrl: "./app.html",
})
export class App {
  protected title = "gate-admin-ui";
}
