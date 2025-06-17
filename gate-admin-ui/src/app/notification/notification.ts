import { Component } from "@angular/core";
import { Subscription } from "rxjs";
import { NotificationService } from "./notification.service";
import { NotificationModel } from "./notification.model";
import { CommonModule } from "@angular/common";

@Component({
  selector: "app-notification",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./notification.html",
})
export class Notification {
  notification: NotificationModel | null = null;
  private subscription!: Subscription;
  private timer: any;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.subscription = this.notificationService.notification$.subscribe(
      (notification) => {
        this.notification = notification;

        if (this.timer) {
          clearTimeout(this.timer);
        }

        if (notification) {
          this.timer = setTimeout(() => {
            this.close();
          }, 5000);
        }
      },
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
    if (this.timer) {
      clearTimeout(this.timer);
    }
  }

  close(): void {
    this.notificationService.clear();
  }

  getNotificationClasses(): object {
    if (!this.notification) {
      return {};
    }
    return {
      "bg-green-500": this.notification.type === "success",
      "bg-red-500": this.notification.type === "error",
      "bg-blue-500": this.notification.type === "info",
    };
  }
}
