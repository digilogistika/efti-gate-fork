import { Component, OnInit, OnDestroy, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { environment } from "../../environments/environment";
import { RouterLink } from "@angular/router";
import { interval, Subscription } from "rxjs";
import { CommonModule } from "@angular/common";

// Import what we need
import { GateService } from "../gates/gate.service";
import { Gate } from "../gates/gate.model";

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

  // Gate List Properties
  gates: Gate[] = [];
  gateCount = 0;
  isLoadingGates = true;
  fetchError: string | null = null;

  private http = inject(HttpClient);
  private gateService = inject(GateService);

  ngOnInit() {
    this.fetchGates();
    this.startHealthCheck();
  }

  ngOnDestroy() {
    this.stopHealthCheck();
  }

  fetchGates(): void {
    this.isLoadingGates = true;
    this.fetchError = null;
    this.gateService.getGates().subscribe({
      next: (data: Gate[]) => {
        this.gates = data;
        this.gateCount = data.length;
        this.isLoadingGates = false;
      },
      error: (err: any) => {
        this.fetchError = "Failed to load gates.";
        this.isLoadingGates = false;
        console.error("Error fetching gates:", err);
      },
    });
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
