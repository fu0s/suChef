import { Component, OnInit, ChangeDetectorRef, NgZone, ElementRef, Renderer2, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { LanguageSwitcherComponent } from '../language-switcher/language-switcher.component';
import { AuthService } from '../../../core/services/auth.service';
import { AuthContext } from '../../../core/services/auth-context.service';
import { SidebarService } from '../../../core/services/sidebar.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, LanguageSwitcherComponent, RouterModule, TranslateModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  isAuthenticated: boolean = false;
  isSidebarCollapsed: boolean = true;
  isScrolled: boolean = false;
  private destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    private authService: AuthService,
    private authContext: AuthContext,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone,
    private el: ElementRef,
    private renderer: Renderer2,
    private sidebarService: SidebarService,
    private translateService: TranslateService
  ) { }

  @HostListener('window:scroll', [])
  onWindowScroll() {
    // Hero section is pinned with end:'+=200%', meaning it holds the viewport
    // for an extra 2× viewport-heights of scroll. Show the header only after
    // the user has scrolled fully past that pinned zone.
    const heroScrollEnd = window.innerHeight * 2;
    this.isScrolled = window.scrollY > heroScrollEnd;
  }

  ngOnInit(): void {
    // Check authentication state
    this.isAuthenticated = this.authContext.isAuthenticated();

    // Subscribe to auth state changes
    this.authContext.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe((user: any) => {
        this.isAuthenticated = user !== null;
        if (!this.isAuthenticated) {
          this.updateHeaderOffset(0);
        } else {
          // Re-apply current sidebar state if authenticated
          this.updateHeaderOffset(this.sidebarService.isCollapsed ? 80 : 256);
        }
        this.cdr.detectChanges();
      });

    // Subscribe to sidebar state
    this.sidebarService.isCollapsed$
      .pipe(takeUntil(this.destroy$))
      .subscribe(collapsed => {
        this.isSidebarCollapsed = collapsed;
        if (this.isAuthenticated) {
          this.updateHeaderOffset(collapsed ? 80 : 256);
        } else {
          this.updateHeaderOffset(0);
        }
        this.cdr.markForCheck();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  private updateHeaderOffset(px: number) {
    try {
      const host = this.el.nativeElement as HTMLElement;
      this.renderer.setStyle(host, 'margin-left', `${px}px`);
      this.renderer.setStyle(host, 'width', `calc(100% - ${px}px)`);
      this.renderer.setStyle(host, 'transition', 'margin-left 0.3s ease, width 0.3s ease');
      this.renderer.setStyle(host, 'display', 'block');
      this.renderer.setStyle(host, 'position', 'sticky');
      this.renderer.setStyle(host, 'top', '0');
      this.renderer.setStyle(host, 'z-index', '30');
    } catch (e) {
      // silently fail
    }
  }

  scrollTo(sectionId: string): void {
    if (this.router.url !== '/' && !this.router.url.startsWith('/home')) {
      this.router.navigate(['/']).then(() => {
        setTimeout(() => {
          const element = document.getElementById(sectionId);
          if (element) {
            element.scrollIntoView({ behavior: 'smooth' });
          }
        }, 100);
      });
    } else {
      const element = document.getElementById(sectionId);
      if (element) {
        element.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }
}
