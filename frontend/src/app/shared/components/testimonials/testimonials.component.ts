import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Testimonial {
  quote: string;
  name: string;
  title: string;
  rating: number;
}

@Component({
  selector: 'app-testimonials',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './testimonials.component.html'
})
export class TestimonialsComponent {
  testimonials: Testimonial[] = [
    {
      quote: "suChef reduced our food waste by 40% in the first month. The AI inventory insights are game-changing.",
      name: "Maria Sanchez",
      title: "Owner, Casa Maria",
      rating: 5
    },
    {
      quote: "Our profits increased by 22% after implementing suChef's menu optimization recommendations.",
      name: "James Wilson",
      title: "Executive Chef, The Bistro",
      rating: 5
    },
    {
      quote: "The real-time analytics dashboard helps me make better decisions instantly. Customer satisfaction is up!",
      name: "Sarah Johnson",
      title: "Manager, Urban Eats",
      rating: 5
    }
  ];

  getStars(count: number): string {
    return '★'.repeat(count) + '☆'.repeat(5 - count);
  }
}
