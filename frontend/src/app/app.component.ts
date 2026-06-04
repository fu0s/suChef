import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { HeaderComponent } from './shared/components/header/header.component';
import { filter } from 'rxjs/operators';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit, OnDestroy {
  showHeader = true;
  private routerSub!: Subscription;
  private scrollTimeout: any;

  constructor(private router: Router) {}

  @HostListener('window:scroll', [])
  onWindowScroll() {
    document.body.classList.remove('scrolling-stopped');
    clearTimeout(this.scrollTimeout);
    this.scrollTimeout = setTimeout(() => {
      document.body.classList.add('scrolling-stopped');
    }, 1500);
  }

  ngOnInit() {
    document.body.classList.add('scrolling-stopped');
    this.routerSub = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      // Global header is now always shown
      this.showHeader = true;
    });
  }

  ngOnDestroy() {
    if (this.routerSub) {
      this.routerSub.unsubscribe();
    }
  }
}
