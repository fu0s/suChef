import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost';
type ButtonSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './button.component.html'
})
export class ButtonComponent {
  @Input() variant: ButtonVariant = 'primary';
  @Input() size: ButtonSize = 'md';
  @Input() disabled = false;
  @Input() type: 'button' | 'submit' | 'reset' = 'button';

  get variantClasses(): string {
    const variants = {
      primary: 'bg-gradient-to-r text-white',
      secondary: 'bg-gradient-to-r text-white',
      outline: 'border-2 text-primary-600',
      ghost: 'text-primary-600 hover:bg-stone-50'
    };
    return variants[this.variant];
  }

  get variantStyles(): string {
    const styles = {
      primary: 'background: var(--gradient-1); box-shadow: 0 10px 25px rgba(22, 82, 176, 0.18);',
      secondary: 'background: var(--gradient-2);',
      outline: 'border-color: var(--primary-600); background: transparent;',
      ghost: 'background: transparent;'
    };
    return styles[this.variant];
  }

  get sizeClasses(): string {
    const sizes = {
      sm: 'text-sm px-3 py-1.5',
      md: 'text-base px-4 py-2',
      lg: 'text-lg px-6 py-3'
    };
    return sizes[this.size];
  }
}