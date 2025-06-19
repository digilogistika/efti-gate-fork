import { ErrorHandler, Injectable, NgZone } from "@angular/core";
import { NotificationService } from "../notification/notification.service";

@Injectable({
  providedIn: "root",
})
export class GlobalErrorHandler implements ErrorHandler {
  constructor(
    private readonly notificationService: NotificationService,
    private readonly ngZone: NgZone,
  ) {}

  handleError(error: Error): void {
    this.ngZone.run(() => {
      console.error("An error occurred:", error);

      this.notificationService.showError(
        error.message || "An unexpected error occurred",
      );
    });
  }
}
