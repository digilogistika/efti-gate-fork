import { Component, OnInit, OnDestroy, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { environment } from "../../environments/environment";
import { RouterLink } from "@angular/router";
import { interval, Subscription } from "rxjs";
import { CommonModule } from "@angular/common";

// Import what we need
import { GateService } from "../gates/gate.service";
import { MetaData } from "../gates/metadata.model";

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

  fetchError: string | null = null;
  gateCount = 0;
  platformCount = 0;
  authorityCount = 0;
  isLoadingStats = false;

  private http = inject(HttpClient);
  private gateService = inject(GateService);

  fetchStats(): void {
    this.isLoadingStats = true;
    this.gateService.getMetaData().subscribe({
      next: (data: MetaData) => {
        this.gateCount = data.gateIds.length;
        this.platformCount = data.platformIds.length;
        this.authorityCount = data.authorityNames.length;
        this.isLoadingStats = false;
      },
      error: (err) => {
        console.error("Error fetching metadata stats:", err);
        this.gateCount = 0;
        this.platformCount = 0;
        this.authorityCount = 0;
        this.isLoadingStats = false;      }
    });
  }

  ngOnInit() {
    this.fetchStats();
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
