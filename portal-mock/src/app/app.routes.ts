import { Routes } from '@angular/router';
import {Login} from './login/login';
import {Admin} from './admin/admin';

export const routes: Routes = [
  {
    path: "login",
    component: Login
  },
  {
    path: 'admin',
    component: Admin
  },
  {
    path: "**",
    redirectTo: "/login",
    pathMatch: "full"
  },
];
