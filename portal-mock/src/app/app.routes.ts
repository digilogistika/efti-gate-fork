import { Routes } from '@angular/router';
import {Login} from './login/login';
import {Admin} from './admin/admin';
import {IdentifiersSearch} from './identifiers-search/identifiers-search';

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
    path: "identifiers-search",
    component: IdentifiersSearch
  },
  {
    path: "**",
    redirectTo: "/login",
    pathMatch: "full"
  },
];
