import { Component, OnInit } from '@angular/core';
import { HeroSectionComponent } from './hero-section/hero-section.component';
import { FeaturesGridComponent } from './features-grid/features-grid.component';
import { AiSectionComponent } from './ai-section/ai-section.component';
import { RecommendationsCarouselComponent } from './recommendations-carousel/recommendations-carousel.component';
import { PricingSectionComponent } from './pricing-section/pricing-section.component';
import { ContactFormComponent } from './contact-form/contact-form.component';

@Component({
  selector: 'app-landing-new',
  standalone: true,
  imports: [
    HeroSectionComponent,
    FeaturesGridComponent,
    AiSectionComponent,
    RecommendationsCarouselComponent,
    PricingSectionComponent,
    ContactFormComponent
  ],
  templateUrl: './landing-new.component.html',
  styleUrls: ['./landing-new.component.scss']
})
export class LandingNewComponent implements OnInit {
  constructor() {}

  ngOnInit(): void {
    // Initialize any global landing page logic
  }
}
