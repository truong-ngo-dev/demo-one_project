import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./landing/landing').then(m => m.LandingComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./register/register').then(m => m.RegisterComponent),
  },
  {
    path: 'admin',
    loadComponent: () => import('./dashboard/dashboard').then(m => m.DashboardComponent),
    canActivate: [authGuard, adminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/home/home').then(m => m.HomeComponent),
      },
      {
        path: 'roles',
        loadComponent: () => import('./dashboard/roles/roles').then(m => m.RolesComponent),
      },
      {
        path: 'users',
        loadComponent: () => import('./dashboard/users/users').then(m => m.UsersComponent),
      },
      {
        path: 'users/:id',
        loadComponent: () => import('./dashboard/users/user-detail/user-detail').then(m => m.UserDetailComponent),
      },
      {
        path: 'active-sessions',
        loadComponent: () => import('./dashboard/active-sessions/active-sessions').then(m => m.ActiveSessionsComponent),
      },
      {
        path: 'login-activities',
        loadComponent: () => import('./dashboard/login-activities/login-activities').then(m => m.LoginActivitiesComponent),
      },
    ],
  },
  {
    path: 'app',
    loadComponent: () => import('./portal/portal').then(m => m.PortalComponent),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./portal/home/home').then(m => m.PortalHomeComponent),
      },
      {
        path: 'profile',
        loadComponent: () => import('./portal/profile/profile').then(m => m.ProfileComponent),
      },
      {
        path: 'profile/password',
        loadComponent: () => import('./portal/profile/change-password/change-password').then(m => m.ChangePasswordComponent),
      },
      {
        path: 'devices',
        loadComponent: () => import('./portal/devices/devices').then(m => m.DevicesComponent),
      },
      {
        path: 'login-history',
        loadComponent: () => import('./portal/login-history/login-history').then(m => m.LoginHistoryComponent),
      },
    ],
  },
  { path: '**', redirectTo: '/' },
];
