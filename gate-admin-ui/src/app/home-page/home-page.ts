import { Component, OnInit, OnDestroy } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { environment } from "../../environments/environment";
import { RouterLink } from "@angular/router";
import { interval, Subscription } from "rxjs";
import { CommonModule } from "@angular/common";

@Component({
  selector: "app-home-page",
  imports: [RouterLink, CommonModule],
  templateUrl: "./home-page.html",
})
export class HomePage implements OnInit, OnDestroy {
  gate = environment.gateId;
  systemStatus = "Offline";
  isSystemOnline = false;
  private healthCheckSubscription?: Subscription;
  private readonly HEALTH_CHECK_INTERVAL = 5000;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.startHealthCheck();
  }

  ngOnDestroy() {
    this.stopHealthCheck();
  }

  private startHealthCheck() {
    this.performHealthCheck();

    this.healthCheckSubscription = interval(
      this.HEALTH_CHECK_INTERVAL,
    ).subscribe(() => {
      this.performHealthCheck();
    });
  }

  private stopHealthCheck() {
    if (this.healthCheckSubscription) {
      this.healthCheckSubscription.unsubscribe();
    }
  }

  private performHealthCheck() {
    const healthUrl = environment.apiUrl.health;

    this.http.get(healthUrl).subscribe({
      next: () => {
        this.isSystemOnline = true;
        this.systemStatus = "Online";
      },
      error: () => {
        this.isSystemOnline = false;
        this.systemStatus = "Offline";
      },
    });
  }
}
