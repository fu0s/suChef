import { Routes } from '@angular/router';
import { ChatComponent } from './components/chat.component';

export const CHAT_ROUTES: Routes = [
  {
    path: '',
    component: ChatComponent,
    title: 'AI Chat Assistant - suChef'
  }
];
