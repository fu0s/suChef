import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { PricingTier, PRICING_TIERS, calculateAnnualSavings } from '../models/pricing-tier.model';

/**
 * PricingService
 * Provides pricing tier data and billing period management
 */
@Injectable({
  providedIn: 'root'
})
export class PricingService {
  private pricingTiersSubject = new BehaviorSubject<PricingTier[]>(PRICING_TIERS);
  private billingPeriodSubject = new BehaviorSubject<'monthly' | 'annual'>('monthly');

  /**
   * Get current billing period
   */
  getBillingPeriod(): Observable<'monthly' | 'annual'> {
    return this.billingPeriodSubject.asObservable();
  }

  /**
   * Set billing period
   */
  setBillingPeriod(period: 'monthly' | 'annual'): void {
    this.billingPeriodSubject.next(period);
  }

  /**
   * Get all pricing tiers
   */
  getPricingTiers(): Observable<PricingTier[]> {
    return this.pricingTiersSubject.asObservable();
  }

  /**
   * Get a single pricing tier by id
   */
  getPricingTierById(id: string): Observable<PricingTier | undefined> {
    return new Observable(observer => {
      const tier = PRICING_TIERS.find(t => t.id === id);
      observer.next(tier);
      observer.complete();
    });
  }

  /**
   * Get the highlighted (pro) tier
   */
  getHighlightedTier(): Observable<PricingTier | undefined> {
    return new Observable(observer => {
      const tier = PRICING_TIERS.find(t => t.highlighted);
      observer.next(tier);
      observer.complete();
    });
  }

  /**
   * Get current price for a tier
   */
  getCurrentPrice(tierId: string, period: 'monthly' | 'annual'): number {
    const tier = PRICING_TIERS.find(t => t.id === tierId);
    if (!tier) return 0;
    return period === 'monthly' ? tier.monthlyPrice : tier.monthlyPrice * 10;
  }

  /**
   * Calculate savings for annual billing
   */
  getAnnualSavings(monthlyPrice: number): number {
    return calculateAnnualSavings(monthlyPrice);
  }

  /**
   * Get all pricing tiers sorted by price
   */
  getPricingTiersSorted(): PricingTier[] {
    return [...PRICING_TIERS].sort((a, b) => a.monthlyPrice - b.monthlyPrice);
  }
}
