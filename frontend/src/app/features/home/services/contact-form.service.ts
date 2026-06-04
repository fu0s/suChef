import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

/**
 * Contact Form Data Interface
 */
export interface ContactFormData {
  name: string;
  email: string;
  restaurantName: string;
  restaurantType: string;
  numberOfLocations: number;
  message?: string;
  agreeToTerms: boolean;
}

/**
 * Contact Form Response Interface
 */
export interface ContactFormResponse {
  success: boolean;
  message?: string;
  error?: string;
}

/**
 * ContactFormService
 * Handles contact form submission to the backend API
 */
@Injectable({
  providedIn: 'root'
})
export class ContactFormService {
  private apiUrl = '/api/contact'; // API endpoint
  private submittingSubject = new BehaviorSubject<boolean>(false);

  /**
   * Observable for tracking submission state
   */
  isSubmitting$ = this.submittingSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Submit contact form data
   * @param data - The contact form data
   * @returns Observable with the response
   */
  submitForm(data: ContactFormData): Observable<ContactFormResponse> {
    this.submittingSubject.next(true);

    // Make HTTP POST request to backend
    return new Observable(observer => {
      this.http.post<ContactFormResponse>(this.apiUrl, data).subscribe({
        next: (response) => {
          this.submittingSubject.next(false);
          observer.next(response);
          observer.complete();
        },
        error: (error) => {
          this.submittingSubject.next(false);
          observer.error({
            success: false,
            error: error.error?.error || 'Failed to submit form. Please try again.'
          });
        }
      });
    });
  }

  /**
   * Submit contact form data (alias for submitForm)
   */
  submitContact(data: ContactFormData): Observable<ContactFormResponse> {
    return this.submitForm(data);
  }

  /**
   * Get current submitting state
   */
  isSubmitting(): boolean {
    return this.submittingSubject.value;
  }

  /**
   * Validate form data before submission (client-side)
   */
  validateForm(data: ContactFormData): { valid: boolean; errors: string[] } {
    const errors: string[] = [];

    if (!data.name || data.name.trim().length < 2) {
      errors.push('Name must be at least 2 characters');
    }

    if (!data.email || !this.isValidEmail(data.email)) {
      errors.push('Please provide a valid email address');
    }

    if (!data.restaurantName || data.restaurantName.trim().length < 2) {
      errors.push('Restaurant name must be at least 2 characters');
    }

    if (!data.restaurantType) {
      errors.push('Please select a restaurant type');
    }

    if (data.numberOfLocations < 1 || data.numberOfLocations > 1000) {
      errors.push('Number of locations must be between 1 and 1000');
    }

    if (data.message && data.message.length > 2000) {
      errors.push('Message must not exceed 2000 characters');
    }

    if (!data.agreeToTerms) {
      errors.push('You must agree to the terms');
    }

    return {
      valid: errors.length === 0,
      errors
    };
  }

  /**
   * Simple email validation
   */
  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}
