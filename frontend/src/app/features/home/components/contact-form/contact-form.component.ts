import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ContactFormService } from '../../services/contact-form.service';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

gsap.registerPlugin(ScrollTrigger);

@Component({
  selector: 'app-contact-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './contact-form.component.html',
  styleUrls: ['./contact-form.component.scss']
})
export class ContactFormComponent implements OnInit, OnDestroy {
  contactForm!: FormGroup;
  isSubmitting: boolean = false;
  showSuccess: boolean = false;
  errorMessage: string = '';

  restaurantTypes = [
    'Fine Dining',
    'Casual',
    'Fast Casual',
    'Fast Food',
    'Bakery',
    'Café',
    'Food Truck',
    'Other'
  ];

  constructor(
    private formBuilder: FormBuilder,
    private contactFormService: ContactFormService
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.initFormAnimation();
  }

  private initializeForm(): void {
    this.contactForm = this.formBuilder.group({
      fullName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      restaurantName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      restaurantType: ['', Validators.required],
      numberOfLocations: ['', [Validators.required, Validators.min(1), Validators.max(1000)]],
      message: ['', [Validators.maxLength(2000)]],
      agreeToTerms: [false, Validators.requiredTrue]
    });
  }

  private initFormAnimation(): void {
    const formFields = document.querySelectorAll('[data-form-field]');

    gsap.to(formFields, {
      opacity: 1,
      y: 0,
      duration: 0.6,
      stagger: {
        each: 0.08,
        from: 'start'
      },
      scrollTrigger: {
        trigger: '[data-contact-section]',
        start: 'top 85%'
      }
    });
  }

  onSubmit(): void {
    if (!this.contactForm.valid) {
      return;
    }

    this.isSubmitting = true;
    this.contactFormService.submitContact(this.contactForm.value).subscribe(
      () => {
        this.isSubmitting = false;
        this.showSuccess = true;
        this.triggerConfetti();
        this.contactForm.reset();

        setTimeout(() => {
          this.showSuccess = false;
        }, 5000);
      },
      (error) => {
        this.isSubmitting = false;
        this.errorMessage = 'Something went wrong. Please try again.';

        setTimeout(() => {
          this.errorMessage = '';
        }, 5000);
      }
    );
  }

  private triggerConfetti(): void {
    // This will trigger the Three.js confetti animation
    const event = new CustomEvent('formSuccess');
    window.dispatchEvent(event);
  }

  ngOnDestroy(): void {
    ScrollTrigger.getAll().forEach(trigger => trigger.kill());
  }
}
