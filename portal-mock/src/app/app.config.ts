import {
  ApplicationConfig,
  LOCALE_ID,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {requestInterceptor} from './request-interceptor';
import {registerLocaleData} from '@angular/common';

import localeEt from '@angular/common/locales/et';

registerLocaleData(localeEt);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([
        requestInterceptor
      ])
    ),
    {provide: LOCALE_ID, useValue: 'et' }
  ]
};
