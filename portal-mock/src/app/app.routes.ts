import { Routes } from '@angular/router';
import {IdentifiersSearchComponent} from "./pages/identifiers-search/identifiers-search.component";
import {authGuard} from "./core/guard/auth.guard";
import {postLoginGuard} from "./core/guard/post-login.guard";
import {LoginComponent} from "./pages/login/login.component";
import {IdentifiersDisplayComponent} from "./pages/identifiers-display/identifiers-display.component";
import {UilSearchComponent} from "./pages/uil-search/uil-search.component";
import {ApiKeyComponent} from "./pages/api-key/api-key.component";

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'identifiers',
    pathMatch: 'full'
  },
  {
    path: 'uil',
    component: UilSearchComponent,
    canActivate: [authGuard]
  },
  {
    path: 'identifiers',
    component: IdentifiersSearchComponent,
    canActivate: [authGuard]
  },
  {
    path: 'identifiers-display/:id',
    component: IdentifiersDisplayComponent,
    canActivate: [authGuard]
  },
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [postLoginGuard]
  },
  {
    path: 'api-settings',
    component: ApiKeyComponent,
    canActivate: [authGuard]
  }
];
