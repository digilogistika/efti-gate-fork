import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import {LoginService} from "../services/login.service";

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const loginService = inject(LoginService);

  if (!loginService.isLoggedIn()) {
    router.navigate(['login']).then(() => {
      location.reload();
    });
    return false;
  }

  return true;
};
