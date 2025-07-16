import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

export const languageRedirectGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const router = inject(Router);
  const translateService = inject(TranslateService);
  const supportedLanguages = ['en', 'et', 'lt', 'lv', 'pl'];

  const lang = route.params['lang'];
  if (supportedLanguages.includes(lang)) {
    localStorage.setItem('language', lang);
    translateService.use(lang);

    const remainingSegments = route.url.slice(1);

    let newPath = '';

    if (remainingSegments.length > 0) {
      newPath = remainingSegments.map(segment => segment.path).join('/');
    } else if (route.params['0']) {
      newPath = route.params['0'];
    }

    router.navigate([`/${newPath}`], {
      replaceUrl: true,
      queryParams: route.queryParams
    });

    return false;
  }

  return true;
};
