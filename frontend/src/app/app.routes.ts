import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadChildren: () => import('./features/home/home.routes')
      .then(m => m.HOME_ROUTES)
  },
  {
    path: '',
    loadChildren: () => import('./features/auth/auth.routes')
      .then(m => m.AUTH_ROUTES)
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./features/dashboard/dashboard.routes')
      .then(m => m.DASHBOARD_ROUTES)
  },
  {
    path: 'documents',
    loadChildren: () => import('./features/documents/documents.routes')
      .then(m => m.DOCUMENTS_ROUTES)
  },
  {
    path: 'data-overview',
    loadChildren: () => import('./features/data-overview/data-overview.routes')
      .then(m => m.DATA_OVERVIEW_ROUTES)
  },
  {
    path: 'profile',
    loadChildren: () => import('./features/profile/profile.routes')
      .then(m => m.profileRoutes)
  },
  {
    path: 'chat',
    loadChildren: () => import('./features/chat/chat.routes')
      .then(m => m.CHAT_ROUTES)
  },
  {
    path: 'subscription',
    loadChildren: () => import('./features/subscription/subscription.routes')
      .then(m => m.SUBSCRIPTION_ROUTES)
  }
];