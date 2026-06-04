import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from '../button/button.component';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule, ButtonComponent],
  templateUrl: './modal.component.html',
  styles: [`
    @keyframes modalIn {
      from {
        opacity: 0;
        transform: scale(0.95);
      }
      to {
        opacity: 1;
        transform: scale(1);
      }
    }

    .animate-modal-in {
      animation: modalIn 0.2s ease-out;
    }
  `]
})
export class ModalComponent {
  @Input() isOpen = false;
  @Input() title = '';
  @Input() showCloseButton = true;
  @Input() closeOnBackdrop = true;
  @Input() showFooter = true;
  @Input() showCancelButton = true;
  @Input() showConfirmButton = true;
  @Input() cancelText = 'Cancel';
  @Input() confirmText = 'Confirm';
  @Input() confirmButtonVariant: 'primary' | 'secondary' | 'outline' = 'primary';

  @Output() onClose = new EventEmitter<void>();
  @Output() onConfirm = new EventEmitter<void>();
}