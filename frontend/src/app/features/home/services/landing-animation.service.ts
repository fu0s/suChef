import { Injectable, NgZone } from '@angular/core';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

// Register ScrollTrigger plugin
gsap.registerPlugin(ScrollTrigger);

/**
 * LandingAnimationService
 * Manages all GSAP animations for the landing page
 * Includes scroll triggers, staggered reveals, and pinned animations
 */
@Injectable({
  providedIn: 'root'
})
export class LandingAnimationService {
  private activeTriggers: ScrollTrigger[] = [];

  constructor(private ngZone: NgZone) {}

  /**
   * Create a staggered reveal animation for multiple elements
   * Typically used for feature cards, pricing tiers, etc.
   */
  createStaggerReveal(
    elements: HTMLElement[],
    options: {
      duration?: number;
      stagger?: number;
      delay?: number;
      startTrigger?: string;
      fromTop?: boolean;
    } = {}
  ): void {
    const {
      duration = 0.75,
      stagger = 0.12,
      delay = 0,
      startTrigger = 'top 82%',
      fromTop = true
    } = options;

    if (elements.length === 0) return;

    this.ngZone.runOutsideAngular(() => {
      // Create scroll trigger for staggered animation
      ScrollTrigger.create({
        trigger: elements[0].parentElement,
        start: startTrigger,
        onEnter: () => {
          gsap.to(elements, {
            opacity: 1,
            y: 0,
            duration,
            stagger,
            delay,
            ease: 'power2.out'
          });
        },
        once: true // Trigger only once
      });

      // Store trigger for cleanup
      const trigger = ScrollTrigger.getById(elements[0].parentElement?.id || '');
      if (trigger) {
        this.activeTriggers.push(trigger);
      }
    });
  }

  /**
   * Animate sticky header (fade in/out based on scroll position)
   */
  createStickyHeaderAnimation(headerElement: HTMLElement): void {
    if (!headerElement) return;

    this.ngZone.runOutsideAngular(() => {
      let isAnimating = false;

      window.addEventListener('scroll', () => {
        if (isAnimating) return;

        const scrollY = window.scrollY;
        isAnimating = true;

        if (scrollY > 100) {
          // Fade in
          gsap.to(headerElement, {
            opacity: 1,
            backdropFilter: 'blur(12px)',
            duration: 0.4,
            ease: 'power2.out',
            onComplete: () => { isAnimating = false; }
          });
        } else if (scrollY < 50) {
          // Fade out
          gsap.to(headerElement, {
            opacity: 0,
            backdropFilter: 'blur(0px)',
            duration: 0.4,
            onComplete: () => { isAnimating = false; }
          });
        }
      });
    });
  }

  /**
   * Create a pinned scroll animation (element stays fixed during scroll)
   * Returns progress value (0-1) for driving animations
   */
  createPinnedScroll(
    element: HTMLElement,
    triggerElement: HTMLElement,
    duration: number = 2000,
    onUpdate?: (progress: number) => void
  ): void {
    if (!element || !triggerElement) return;

    this.ngZone.runOutsideAngular(() => {
      const trigger = ScrollTrigger.create({
        trigger: triggerElement,
        pin: true,
        start: 'top top',
        end: `+=${duration}`,
        markers: false,
        onUpdate: (self) => {
          if (onUpdate) {
            this.ngZone.run(() => {
              onUpdate(self.progress);
            });
          }
        }
      });

      this.activeTriggers.push(trigger);
    });
  }

  /**
   * Create hero headline scroll morph animation
   */
  createHeroMorphAnimation(
    headlineElement: HTMLElement,
    triggerElement: HTMLElement
  ): void {
    if (!headlineElement || !triggerElement) return;

    this.ngZone.runOutsideAngular(() => {
      ScrollTrigger.create({
        trigger: triggerElement,
        start: 'top 75%',
        onEnter: () => {
          gsap.from(headlineElement, {
            opacity: 0,
            y: 50,
            scale: 0.95,
            duration: 0.8,
            ease: 'power2.out'
          });
        },
        once: true
      });
    });
  }

  /**
   * Create carousel scroll scrub animation
   * Ties carousel movement to scroll position
   */
  createCarouselScrubAnimation(
    carouselElement: HTMLElement,
    triggerElement: HTMLElement,
    totalScroll: number = 100
  ): void {
    if (!carouselElement || !triggerElement) return;

    this.ngZone.runOutsideAngular(() => {
      const trigger = ScrollTrigger.create({
        trigger: triggerElement,
        start: 'top center',
        end: `bottom center`,
        scrub: 1, // Smooth 1 second scrub
        onUpdate: (self) => {
          // Calculate carousel position based on scroll progress
          const progress = self.progress;
          const xPercent = -progress * totalScroll;
          gsap.to(carouselElement, {
            x: xPercent,
            ease: 'none',
            overwrite: 'auto'
          });
        }
      });

      this.activeTriggers.push(trigger);
    });
  }

  /**
   * Create card lift animation on hover
   */
  createCardHoverAnimation(cardElement: HTMLElement): void {
    if (!cardElement) return;

    cardElement.addEventListener('mouseenter', () => {
      gsap.to(cardElement, {
        y: -8,
        boxShadow: '0 16px 40px rgba(0, 0, 0, 0.16)',
        duration: 0.3,
        ease: 'power2.out'
      });
    });

    cardElement.addEventListener('mouseleave', () => {
      gsap.to(cardElement, {
        y: 0,
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
        duration: 0.3,
        ease: 'power2.out'
      });
    });
  }

  /**
   * Create confetti particle animation (celebratory)
   * Returns animation instance for control
   */
  createConfettiAnimation(
    originElement: HTMLElement,
    duration: number = 2500
  ): gsap.core.Tween {
    // Create temporary confetti container
    const confettiContainer = document.createElement('div');
    confettiContainer.style.position = 'fixed';
    confettiContainer.style.top = '0';
    confettiContainer.style.left = '0';
    confettiContainer.style.width = '100%';
    confettiContainer.style.height = '100%';
    confettiContainer.style.pointerEvents = 'none';
    confettiContainer.style.zIndex = '9999';
    document.body.appendChild(confettiContainer);

    // Create confetti particles
    const particleCount = 50;
    const particles = [];

    for (let i = 0; i < particleCount; i++) {
      const particle = document.createElement('div');
      particle.style.position = 'absolute';
      particle.style.width = '8px';
      particle.style.height = '8px';
      particle.style.backgroundColor = [
        '#D4AF37',
        '#8B4513',
        '#6B8E71',
        '#F5D76E'
      ][Math.floor(Math.random() * 4)];
      particle.style.borderRadius = '50%';
      particle.style.left = originElement.offsetLeft + originElement.offsetWidth / 2 + 'px';
      particle.style.top = originElement.offsetTop + originElement.offsetHeight / 2 + 'px';
      confettiContainer.appendChild(particle);
      particles.push(particle);
    }

    // Animate particles
    const animation = gsap.to(particles, {
      duration: duration / 1000,
      x: () => gsap.utils.random(-200, 200),
      y: () => gsap.utils.random(-300, -100),
      opacity: 0,
      scale: 0,
      ease: 'power2.out',
      onComplete: () => {
        confettiContainer.remove();
      }
    });

    return animation;
  }

  /**
   * Kill all active scroll triggers
   * Call on component destroy to prevent memory leaks
   */
  killAllTriggers(): void {
    this.activeTriggers.forEach(trigger => trigger.kill());
    this.activeTriggers = [];
    ScrollTrigger.getAll().forEach(trigger => trigger.kill());
  }

  /**
   * Refresh all scroll triggers
   * Useful after layout changes
   */
  refreshTriggers(): void {
    ScrollTrigger.refresh();
  }

  /**
   * Get GSAP instance for custom tweens
   */
  getGSAP() {
    return gsap;
  }

  /**
   * Get ScrollTrigger plugin for advanced usage
   */
  getScrollTrigger() {
    return ScrollTrigger;
  }
}
