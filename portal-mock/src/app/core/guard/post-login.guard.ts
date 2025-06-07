import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import {LoginService} from "../services/login.service";

export const postLoginGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const loginService = inject(LoginService);

  if (loginService.isLoggedIn()) {
    router.navigate(['']);
    return false;
  }

  return true;
};
