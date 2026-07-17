import { Component, OnInit, OnDestroy, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AuthContext } from '../../../core/services/auth-context.service';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SidebarService } from '../../../core/services/sidebar.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styles: [`
    :host {
      --sidebar-width: 256px;
      --sidebar-collapsed: 80px;
    }

    :host ::ng-deep .active-link {
      background: rgba(22, 82, 176, 0.08);
      border-left-color: var(--primary-600);
      color: var(--primary-600);
    }

    /* Uses global .gradient-text class defined in app.scss */
  `]
})
export class SidebarComponent implements OnInit, OnDestroy {
  isCollapsed = true; // Start collapsed
  isHovering = false;
  currentUser: any = null;

  // Translation labels
  menuLabel = '';
  expandLabel = '';
  collapseLabel = '';
  dashboardLabel = '';
  documentsLabel = '';
  dataOverviewLabel = '';
  chatLabel = '';
  subscriptionLabel = '';
  profileLabel = '';
  logoutLabel = '';

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    public authContext: AuthContext,
    private translateService: TranslateService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone,
    private sidebarService: SidebarService
  ) {
    this.updateLabels();

    // Sync isCollapsed with service
    this.sidebarService.isCollapsed$
      .pipe(takeUntil(this.destroy$))
      .subscribe(collapsed => {
        this.isCollapsed = collapsed;
        this.cdr.markForCheck();
      });

    // Subscribe to language changes to update labels
    this.translateService.onLangChange
      .pipe(takeUntil(this.destroy$))
      .subscribe((event) => {
        console.log('Sidebar: Language change event received:', event.lang);
        this.ngZone.run(() => {
          this.updateLabels();
        });
      });

    // Subscribe to current user changes via AuthContext
    this.authContext.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe((user) => {
        this.currentUser = user;
        this.cdr.markForCheck();
      });
  }

  ngOnInit() {
    // Sidebar always starts collapsed, expand only on hover/click
    this.isCollapsed = true;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private updateLabels() {
    this.menuLabel = this.translateService.instant('common.menu');
    this.expandLabel = this.translateService.instant('common.expand');
    this.collapseLabel = this.translateService.instant('common.collapse');
    this.dashboardLabel = this.translateService.instant('sidebar.dashboard');
    this.documentsLabel = this.translateService.instant('sidebar.documents');
    this.dataOverviewLabel = this.translateService.instant('sidebar.dataOverview');
    this.chatLabel = this.translateService.instant('sidebar.chat');
    this.subscriptionLabel = this.translateService.instant('sidebar.subscription');
    this.profileLabel = this.translateService.instant('sidebar.profile');
    this.logoutLabel = this.translateService.instant('common.logout');
    console.log('Sidebar labels updated:', {
      menu: this.menuLabel,
      dashboard: this.dashboardLabel,
      documents: this.documentsLabel,
      dataOverview: this.dataOverviewLabel
    });
    this.cdr.markForCheck();
  }

  toggleSidebar() {
    this.sidebarService.toggle();
  }

  onSidebarMouseEnter(): void {
    this.isHovering = true;
    this.sidebarService.setCollapsed(false);
  }

  onSidebarMouseLeave(): void {
    this.isHovering = false;
    this.sidebarService.setCollapsed(true);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/home']);
  }

  getInitials(): string {
    if (!this.currentUser?.name) return '?';
    return this.currentUser.name
      .split(' ')
      .map((n: string) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  getFirstName(): string {
    if (!this.currentUser?.name) return '?';
    const parts = this.currentUser.name.split(' ').filter((p: string) => p && p.length > 0);
    return parts[0];
  }
}