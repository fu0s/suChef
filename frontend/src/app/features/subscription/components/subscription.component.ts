import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { SubscriptionService, RestaurantSubscription, SubscriptionUsage } from '../../../core/services/subscription.service';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-subscription',
  standalone: true,
  imports: [CommonModule, TranslatePipe, DatePipe],
  templateUrl: './subscription.component.html'
})
export class SubscriptionComponent implements OnInit {
  isGuest: boolean = false;
  daysRemaining: number = 0;
  usage: SubscriptionUsage | null = null;
  subscription: RestaurantSubscription | null = null;
  loading: boolean = false;
  error: string | null = null;

  constructor(private subscriptionService: SubscriptionService) { }

  ngOnInit() {
    this.isGuest = localStorage.getItem('guest_mode') === 'true';
    if (!this.isGuest) {
      this.loadSubscriptionData();
    }
  }

  loadSubscriptionData() {
    this.loading = true;
    this.error = null;

    forkJoin({
      sub: this.subscriptionService.getCurrentSubscription(),
      usage: this.subscriptionService.getUsage()
    }).subscribe({
      next: (res) => {
        this.subscription = res.sub;
        this.usage = res.usage;
        this.calculateDaysRemaining();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load subscription data', err);
        this.error = err.message || 'Failed to load subscription details. Please try again.';
        this.loading = false;
      }
    });
  }

  calculateDaysRemaining() {
    if (!this.subscription?.endDate) {
      this.daysRemaining = 30; // Default placeholder
      return;
    }
    const today = new Date();
    const endDate = new Date(this.subscription.endDate);
    const timeDifference = endDate.getTime() - today.getTime();
    this.daysRemaining = Math.max(0, Math.ceil(timeDifference / (1000 * 3600 * 24)));
  }

  getStatusColor(status: string): string {
    return 'bg-lime-600'; // Simplifying for now
  }

  getDocSizeMb(bytes: number): number {
    return Math.round(bytes / (1024 * 1024) * 10) / 10;
  }

  dismissError() {
    this.error = null;
  }
}

