import { Routes } from '@angular/router';
import {Login} from './login/login';
import {Admin} from './admin/admin';
import {IdentifiersSearch} from './identifiers-search/identifiers-search';
import {AuthGuard} from './auth/auth.guard';
import {DatasetQuery} from './dataset-query/dataset-query';
import { languageRedirectGuard } from './guards/language-redirect.guard';
import { Redirector } from './guards/redirector';

export const routes: Routes = [
  {
    path: ':lang/dataset-query/:gateId/:platformId/:datasetId',
    canActivate: [languageRedirectGuard],
    component: Redirector
  },
  {
    path: ':lang/:navigation',
    canActivate: [languageRedirectGuard],
    component: Redirector
  },
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
