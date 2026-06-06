import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AuthContext } from '../../../core/services/auth-context.service';
import { LanguageSwitcherComponent } from '../language-switcher/language-switcher.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, LanguageSwitcherComponent],
  templateUrl: './navbar.component.html',
  styles: [`
    .logout-btn:hover {
      background: rgba(255, 255, 255, 0.8) !important;
    }

    .cta-btn:hover {
      background: rgba(255, 255, 255, 0.9) !important;
      transform: translateY(-2px);
    }

    :host {
      .nav-link-header {
        position: relative;
        border-radius: 8px;
      }

      .nav-link-header::after {
        content: '';
        position: absolute;
        width: 0;
        height: 3px;
        bottom: -8px;
        left: 50%;
        transform: translateX(-50%);
        background: rgba(255, 255, 255, 0.8);
        transition: width 0.3s ease;
        border-radius: 2px;
      }

      .nav-link-header:hover {
        background: rgba(255, 255, 255, 0.1);
      }

      .nav-link-header:hover::after {
        width: 60%;
      }

      /* Use global .gradient-text defined in app.scss; remove duplicate local gradient definition */

      /* Header color modes */
      :host .is-dark .logo-text,
      :host .is-dark .nav-link-header,
      :host .is-dark .auth-link {
        color: white !important;
        background: none !important;
        -webkit-text-fill-color: white !important;
        -webkit-background-clip: unset !important;
      }

      :host .is-light .logo-text,
      :host .is-light .nav-link-header,
      :host .is-light .auth-link {
        color: var(--primary-600) !important;
        background: none !important;
        -webkit-text-fill-color: var(--primary-600) !important;
        -webkit-background-clip: unset !important;
      }
    }
  `]
})
export class NavbarComponent {
  isMobileMenuOpen = false;

  constructor(
    private authService: AuthService,
    public authContext: AuthContext
  ) {}

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  logout() {
    this.authService.logout();
  }
}
