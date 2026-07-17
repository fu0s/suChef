import { Component, Input, Output, EventEmitter, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DocumentOverviewDTO } from '../../models/document-overview.model';
import { DocumentStatus, getDocumentStatusLabel, getDocumentStatusColor } from '../../../../shared/models/document-status.model';
import { DocumentClassification, getDocumentClassificationLabel, getDocumentClassificationColor } from '../../../../shared/models/document-classification.model';

@Component({
  selector: 'app-documents-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './documents-list.component.html',
  styleUrls: ['./documents-list.component.scss']
})
export class DocumentsListComponent {
  @Input() documents: DocumentOverviewDTO[] = [];
  @Input() selectedStatus: string = 'PENDING_VALIDATION';
  @Input() isLoading = false;
  @Output() onEditDocument = new EventEmitter<DocumentOverviewDTO>();
  @Output() onStatusChange = new EventEmitter<string>();

  // Local filter state
  localFilter = signal<string>('PENDING_VALIDATION');
  sortDirection = signal<'asc' | 'desc'>('desc');
  sortField = signal<keyof DocumentOverviewDTO>('createdAt');

  // Computed filtered documents
  filteredDocuments = computed(() => {
    let filtered = [...this.documents];
    
    if (this.localFilter()) {
      filtered = filtered.filter(d => d.status === this.localFilter());
    }
    
    // Sort
    filtered = [...filtered].sort((a, b) => {
      const aVal = a[this.sortField()];
      const bVal = b[this.sortField()];
      const direction = this.sortDirection() === 'asc' ? 1 : -1;
      
      if (aVal instanceof Date && bVal instanceof Date) {
        return (aVal.getTime() - bVal.getTime()) * direction;
      }
      if (typeof aVal === 'string' && typeof bVal === 'string') {
        return aVal.localeCompare(bVal) * direction;
      }
      return 0;
    });
    
    return filtered;
  });

  // Status options for filter dropdown
  statusOptions = [
    { value: 'PENDING_VALIDATION', label: 'Pending Validation', color: '#a855f7' },
    { value: 'RECEIVED', label: 'Received', color: '#3b82f6' },
    { value: 'PROCESSING', label: 'Processing', color: '#f59e0b' },
    { value: 'OCR_PROCESSING', label: 'OCR Processing', color: '#f97316' },
    { value: 'COMPLETED', label: 'Completed', color: '#22c55e' },
    { value: 'FAILED', label: 'Failed', color: '#ef4444' }
  ];

  ngOnChanges(): void {
    this.localFilter.set(this.selectedStatus);
  }

  onFilterChange(status: string): void {
    this.localFilter.set(status);
    this.onStatusChange.emit(status);
  }

  onSort(field: keyof DocumentOverviewDTO): void {
    if (this.sortField() === field) {
      this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortField.set(field);
      this.sortDirection.set('desc');
    }
  }

  getStatusLabel(status: DocumentStatus): string {
    return getDocumentStatusLabel(status);
  }

  getStatusColor(status: DocumentStatus): string {
    return getDocumentStatusColor(status);
  }

  getClassificationLabel(classification: DocumentClassification): string {
    return getDocumentClassificationLabel(classification);
  }

  getClassificationColor(classification: DocumentClassification): string {
    return getDocumentClassificationColor(classification);
  }

  getClassificationBg(classification: DocumentClassification): string {
    const colors: Record<string, string> = {
      'BILL': '#E0E7FF',
      'ORDER': '#CCFBF1',
      'MENU': '#D1FAE5'
    };
    return colors[classification] || '#F3F4F6';
  }

  getClassificationText(classification: DocumentClassification): string {
    const colors: Record<string, string> = {
      'BILL': '#4338CA',
      'ORDER': '#0F766E',
      'MENU': '#065F46'
    };
    return colors[classification] || '#374151';
  }

  formatDate(date: Date | string): string {
    const d = new Date(date);
    return d.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  onEdit(doc: DocumentOverviewDTO): void {
    this.onEditDocument.emit(doc);
  }
}