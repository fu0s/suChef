import { Routes } from '@angular/router';
import { DataOverviewComponent } from './data-overview.component';

export const DATA_OVERVIEW_ROUTES: Routes = [
  {
    path: '',
    component: DataOverviewComponent,
    title: 'Data Overview - Pending Validation'
  }
];