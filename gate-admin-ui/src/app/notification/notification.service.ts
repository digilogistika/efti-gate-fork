import { Injectable } from "@angular/core";
import { Subject, Observable } from "rxjs";
import { NotificationModel } from "./notification.model";

@Injectable({
  providedIn: "root",
})
export class NotificationService {
  private notificationSubject = new Subject<NotificationModel | null>();

  public notification$: Observable<NotificationModel | null> =
    this.notificationSubject.asObservable();

  constructor() {}

  showSuccess(message: string): void {
    this.show({ message, type: "success" });
  }

  showError(message: string): void {
    this.show({ message, type: "error" });
  }

  showInfo(message: string): void {
    this.show({ message, type: "info" });
  }

  private show(notification: NotificationModel): void {
    this.notificationSubject.next(notification);
  }

  clear(): void {
    this.notificationSubject.next(null);
  }
}
