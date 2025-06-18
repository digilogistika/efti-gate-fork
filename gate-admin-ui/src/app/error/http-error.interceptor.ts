import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { catchError, EMPTY, throwError } from "rxjs";
import { NotificationService } from "../notification/notification.service";

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const notificationService = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        notificationService.showError(
          "Unauthorized access, have you set API key?",
        );
        return EMPTY;
      }

      return throwError(() => error);
    }),
  );
};
