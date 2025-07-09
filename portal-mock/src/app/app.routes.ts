import { Routes } from '@angular/router';
import {Login} from './login/login';
import {Admin} from './admin/admin';
import {IdentifiersSearch} from './identifiers-search/identifiers-search';
import {AuthGuard} from './auth/auth.guard';
import {DatasetQuery} from './dataset-query/dataset-query';

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
    component: IdentifiersSearch,
    canActivate: [AuthGuard]
  },
  {
    path: "dataset-query/:gateId/:platformId/:datasetId",
    component: DatasetQuery,
    canActivate: [AuthGuard]
  },
  {
    path: "dataset-query",
    component: DatasetQuery,
    canActivate: [AuthGuard]
  },
  {
    path: "**",
    redirectTo: "/login",
    pathMatch: "full"
  },
];
