import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { AuthContext } from './core/services/auth-context.service';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { SidebarService } from './core/services/sidebar.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    SidebarComponent,
    HeaderComponent,
    TranslateModule
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  title = 'suChef';

  constructor(
    public authContext: AuthContext,
    private translateService: TranslateService,
    public sidebarService: SidebarService
  ) {
    // Initialize i18n
    this.translateService.setDefaultLang('en');
    const savedLanguage = localStorage.getItem('language') || 'en';
    this.translateService.use(savedLanguage);
  }
}

