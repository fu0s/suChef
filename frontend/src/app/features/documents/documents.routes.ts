import { Routes } from '@angular/router';
import { DocumentsUploadComponent } from './components/documents-upload.component';

export const DOCUMENTS_ROUTES: Routes = [
  {
    path: '',
    component: DocumentsUploadComponent,
    title: 'Documents - suChef'
  }
];
