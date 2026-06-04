import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { FeaturesService } from '../../services/features.service';
import { Feature } from '../../models/feature.model';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

gsap.registerPlugin(ScrollTrigger);

@Component({
  selector: 'app-features-grid',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './features-grid.component.html',
  styleUrls: ['./features-grid.component.scss']
})
export class FeaturesGridComponent implements OnInit, AfterViewInit, OnDestroy {
  features: Feature[] = [];
  private destroy$ = new Subject<void>();
  private animationInitiated = false;

  constructor(private featuresService: FeaturesService) {}

  ngOnInit(): void {
    this.featuresService.getFeaturesOrdered()
      .pipe(takeUntil(this.destroy$))
      .subscribe(features => {
        this.features = features;
      });
  }

  ngAfterViewInit(): void {
    // Wait for next tick to ensure DOM is fully rendered
    setTimeout(() => {
      this.initStaggeredAnimation();
    }, 0);
  }

  private initStaggeredAnimation(): void {
    if (this.animationInitiated) return;
    this.animationInitiated = true;

    const cards = document.querySelectorAll('[data-feature-card]');
    if (cards.length === 0) return;

    const section = document.querySelector('[data-features-section]');
    if (!section) return;

    gsap.to(cards, {
      opacity: 1,
      y: 0,
      duration: 0.6,
      stagger: {
        each: 0.1,
        from: 'start'
      },
      scrollTrigger: {
        trigger: section,
        start: 'top 75%',
        once: true
      }
    });
  }

  ngOnDestroy(): void {
    this.animationInitiated = false;
    ScrollTrigger.getAll().forEach(trigger => trigger.kill());
    this.destroy$.next();
    this.destroy$.complete();
  }
}
