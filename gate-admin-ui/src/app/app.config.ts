import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
  ErrorHandler,
} from "@angular/core";
import { provideRouter } from "@angular/router";

import { routes } from "./app.routes";
import { provideHttpClient, withInterceptors } from "@angular/common/http";
import { apiKeyInterceptor } from "./authentication/api-key.interceptor";
import { httpErrorInterceptor } from "./error/http-error.interceptor";
import { GlobalErrorHandler } from "./error/error-handler.service";

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideHttpClient(
      withInterceptors([apiKeyInterceptor, httpErrorInterceptor])
    ),
    provideRouter(routes),
    {
      provide: ErrorHandler,
      useClass: GlobalErrorHandler
    }
  ],
};
