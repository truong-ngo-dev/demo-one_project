import { Routes } from '@angular/router';
import { abacGuard } from './core/guards/abac.guard';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';

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
        canActivate: [abacGuard('route:roles')],
        loadComponent: () => import('./dashboard/roles/roles').then(m => m.RolesComponent),
      },
      {
        path: 'users',
        canActivate: [abacGuard('route:users')],
        loadComponent: () => import('./dashboard/users/users').then(m => m.UsersComponent),
      },
      {
        path: 'users/:id',
        canActivate: [abacGuard('route:users')],
        loadComponent: () => import('./dashboard/users/user-detail/user-detail').then(m => m.UserDetailComponent),
      },
      {
        path: 'active-sessions',
        canActivate: [abacGuard('route:active-sessions')],
        loadComponent: () => import('./dashboard/active-sessions/active-sessions').then(m => m.ActiveSessionsComponent),
      },
      {
        path: 'login-activities',
        canActivate: [abacGuard('route:login-activities')],
        loadComponent: () => import('./dashboard/login-activities/login-activities').then(m => m.LoginActivitiesComponent),
      },
      {
        path: 'abac/resources',
        canActivate: [abacGuard('route:abac:resources')],
        loadComponent: () => import('./dashboard/abac/resources/resources').then(m => m.ResourcesComponent),
      },
      {
        path: 'abac/resources/:id',
        canActivate: [abacGuard('route:abac:resources')],
        loadComponent: () => import('./dashboard/abac/resources/resource-detail/resource-detail').then(m => m.ResourceDetailComponent),
      },
      {
        path: 'abac/policy-sets',
        canActivate: [abacGuard('route:abac:policy-sets')],
        loadComponent: () => import('./dashboard/abac/policy-sets/policy-sets').then(m => m.PolicySetsComponent),
      },
      {
        path: 'abac/policy-sets/:id',
        canActivate: [abacGuard('route:abac:policy-sets')],
        loadComponent: () => import('./dashboard/abac/policy-sets/policy-set-detail/policy-set-detail').then(m => m.PolicySetDetailComponent),
      },
      {
        path: 'abac/policies/:id',
        canActivate: [abacGuard('route:abac:policy-sets')],
        loadComponent: () => import('./dashboard/abac/policies/policy-detail/policy-detail').then(m => m.PolicyDetailComponent),
      },
      {
        path: 'abac/ui-elements',
        canActivate: [abacGuard('route:abac:ui-elements')],
        loadComponent: () => import('./dashboard/abac/ui-elements/ui-elements').then(m => m.UIElementsComponent),
      },
      {
        path: 'abac/ui-elements/:id',
        canActivate: [abacGuard('route:abac:ui-elements')],
        loadComponent: () => import('./dashboard/abac/ui-elements/ui-element-detail/ui-element-detail').then(m => m.UIElementDetailComponent),
      },
      {
        path: 'abac/simulator',
        canActivate: [abacGuard('route:abac:simulator')],
        loadComponent: () => import('./dashboard/abac/simulator/simulator').then(m => m.SimulatorComponent),
      },
      {
        path: 'abac/expressions',
        canActivate: [abacGuard('route:abac:expressions')],
        loadComponent: () => import('./dashboard/abac/expressions/expressions').then(m => m.ExpressionsComponent),
      },
      {
        path: 'abac/audit-log',
        canActivate: [abacGuard('route:abac:audit-log')],
        loadComponent: () => import('./dashboard/abac/audit-log/audit-log').then(m => m.AuditLogComponent),
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
