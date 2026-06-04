import { Routes } from '@angular/router';
import { LandingNewComponent } from './components/landing-new.component';
import { HomeComponent } from './home.component';

export const HOME_ROUTES: Routes = [
  {
    path: '',
    component: HomeComponent,
    children: [
      {
        path: '',
        component: LandingNewComponent,
        title: 'suChef - Restaurant Management Made Simple'
      }
    ]
  }
];
