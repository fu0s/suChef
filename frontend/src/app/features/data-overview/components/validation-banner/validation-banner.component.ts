import { Component, Input, Output, EventEmitter, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DataOverviewService } from '../../services/data-overview.service';

@Component({
  selector: 'app-validation-banner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './validation-banner.component.html',
  styleUrls: ['./validation-banner.component.scss']
})
export class ValidationBannerComponent {
  private dataOverviewService = inject(DataOverviewService);

  @Input() isVisible = true;
  @Output() onFilter = new EventEmitter<string>();

  // Use service signals directly for reactive state
  pendingCount = this.dataOverviewService.pendingValidationCount;
  isLoading = this.dataOverviewService.isLoading;

  onBannerClick(): void {
    this.onFilter.emit('PENDING_VALIDATION');
  }

  getAriaLabel(): string {
    const count = this.pendingCount();
    if (this.isLoading()) return 'Loading validation count';
    if (count === 0) return 'No documents pending validation';
    if (count === 1) return '1 document pending validation. Click to filter.';
    return `${count} documents pending validation. Click to filter.`;
  }
}