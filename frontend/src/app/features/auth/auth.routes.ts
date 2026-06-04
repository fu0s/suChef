import { Routes } from '@angular/router';
import { LoginComponent } from './components/login.component';
import { RegisterComponent } from './components/register.component';

export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    title: 'Login - suChef'
  },
  {
    path: 'register',
    component: RegisterComponent,
    title: 'Register - suChef'
  }
];