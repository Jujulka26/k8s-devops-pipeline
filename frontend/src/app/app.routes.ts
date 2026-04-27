import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Register } from './register/register';
import { Main } from './main/main';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'main', component: Main },
];
