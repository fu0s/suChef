import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Testimonial, TESTIMONIALS } from '../models/testimonial.model';

/**
 * TestimonialsService
 * Provides testimonial data for the recommendations carousel section
 */
@Injectable({
  providedIn: 'root'
})
export class TestimonialsService {
  private testimonialsSubject = new BehaviorSubject<Testimonial[]>(TESTIMONIALS);

  /**
   * Get all testimonials as an observable
   */
  getTestimonials(): Observable<Testimonial[]> {
    return this.testimonialsSubject.asObservable();
  }

  /**
   * Get testimonials shuffled for carousel variety
   */
  getTestimonialsShuffled(): Observable<Testimonial[]> {
    return new Observable(observer => {
      const shuffled = [...TESTIMONIALS].sort(() => Math.random() - 0.5);
      observer.next(shuffled);
      observer.complete();
    });
  }

  /**
   * Get a single testimonial by id
   */
  getTestimonialById(id: string): Observable<Testimonial | undefined> {
    return new Observable(observer => {
      const testimonial = TESTIMONIALS.find(t => t.id === id);
      observer.next(testimonial);
      observer.complete();
    });
  }

  /**
   * Get testimonials count
   */
  getTestimonialsCount(): number {
    return TESTIMONIALS.length;
  }

  /**
   * Get average rating from all testimonials
   */
  getAverageRating(): number {
    const sum = TESTIMONIALS.reduce((acc, t) => acc + t.rating, 0);
    return Math.round((sum / TESTIMONIALS.length) * 10) / 10;
  }
}
