import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { PricingService } from '../../services/pricing.service';
import { PricingTier } from '../../models/pricing-tier.model';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

gsap.registerPlugin(ScrollTrigger);

@Component({
  selector: 'app-pricing-section',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './pricing-section.component.html',
  styleUrls: ['./pricing-section.component.scss']
})
export class PricingSectionComponent implements OnInit, OnDestroy {
  pricingTiers: PricingTier[] = [];
  isAnnualBilling: boolean = false;

  constructor(private pricingService: PricingService) {}

  ngOnInit(): void {
    this.pricingService.getPricingTiers().subscribe(tiers => {
      this.pricingTiers = tiers;
      // Wait for DOM to render before animating
      setTimeout(() => {
        this.initPricingAnimation();
      }, 100);
    });
  }

  toggleBilling(): void {
    this.isAnnualBilling = !this.isAnnualBilling;
  }

  getPrice(tier: PricingTier): string {
    if (this.isAnnualBilling) {
      return (tier.monthlyPrice * 10).toFixed(0);
    }
    return tier.monthlyPrice.toFixed(0);
  }

  private initPricingAnimation(): void {
    const cards = document.querySelectorAll('[data-pricing-card]');
    const section = document.querySelector('[data-pricing-section]');

    if (!cards || cards.length === 0 || !section) return;

    // Set initial state
    gsap.set(cards, { opacity: 0, y: 50 });

    // Animate cards on scroll
    gsap.to(cards, {
      opacity: 1,
      y: 0,
      duration: 0.75,
      stagger: {
        each: 0.12,
        from: 'center'
      },
      scrollTrigger: {
        trigger: section,
        start: 'top 80%',
        toggleActions: 'play none none none'
      }
    });
  }

  ngOnDestroy(): void {
    ScrollTrigger.getAll().forEach(trigger => trigger.kill());
  }
}
