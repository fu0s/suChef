import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { TestimonialsService } from '../../services/testimonials.service';
import { Testimonial } from '../../models/testimonial.model';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

gsap.registerPlugin(ScrollTrigger);

@Component({
  selector: 'app-recommendations-carousel',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './recommendations-carousel.component.html',
  styleUrls: ['./recommendations-carousel.component.scss']
})
export class RecommendationsCarouselComponent implements OnInit, OnDestroy {
  testimonials: Testimonial[] = [];
  displayTestimonials: Testimonial[] = [];

  constructor(private testimonialsService: TestimonialsService) {}

  ngOnInit(): void {
    this.testimonialsService.getTestimonials().subscribe(testimonials => {
      this.testimonials = testimonials;
      // Clone for infinite loop
      this.displayTestimonials = [...testimonials, ...testimonials];
      
      // Wait for Angular to update the DOM with *ngFor before animating
      setTimeout(() => this.initCarouselAnimation(), 0);
    });
  }

  private initCarouselAnimation(): void {
    const carousel = document.querySelector('[data-carousel-container]') as HTMLElement;
    if (!carousel) return;

    // Use -50% to shift the container by exactly half its width (the original sequence length).
    // repeat: -1 will make it an infinite loop.
    gsap.to(carousel, {
      x: '-50%',
      // Duration scaled based on number of items for consistent speed
      duration: this.testimonials.length * 5, 
      ease: 'none',
      repeat: -1
    });
  }

  ngOnDestroy(): void {
    ScrollTrigger.getAll().forEach(trigger => trigger.kill());
  }
}
