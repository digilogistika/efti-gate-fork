import { Routes } from '@angular/router';
import {HomePage} from './home-page/home-page';
import {Gates} from './gates/gates';
import {Platforms} from './platforms/platforms';
import {Authorities} from './authorities/authorities';

export const routes: Routes = [
  {
    path: '',
    component: HomePage
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
];
