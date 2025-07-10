import { Routes } from '@angular/router';
import { HomePage } from './home-page/home-page';
import { Gates } from './gates/gates';
import { Platforms } from './platforms/platforms';
import { Authorities } from './authorities/authorities';
import { LoginComponent } from './login/login.service';
import { authGuard } from './authentication/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        component: HomePage,
        pathMatch: 'full'
      },
      {
        path: 'gate',
        component: Gates
      },
      {
        path: 'platform',
        component: Platforms
      },
      {
        path: 'authority',
        component: Authorities
      }
    ]
  },
  // Wildcard route to redirect any unknown paths to the home page.
  // The guard on the parent route will handle redirecting to login if necessary.
  {
    path: '**',
    redirectTo: 'login'
  }
];
