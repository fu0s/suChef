import { Component, Input, Output, EventEmitter, signal, inject, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, Validators, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { DocumentDetailsDTO } from '../../models/document-details.model';
import { DocumentStatus, getDocumentStatusLabel, getDocumentStatusColor } from '../../../shared/models/document-status.model';
import { DocumentClassification, getDocumentClassificationLabel, getDocumentClassificationColor } from '../../../shared/models/document-classification.model';
import { DataOverviewService } from '../../services/data-overview.service';
import { EditMenuDetailsRequest } from '../../models/edit-menu-details-request.model';

interface DocumentFormValue {
  title: string;
  description: string;
  status: DocumentStatus;
  classification: DocumentClassification;
}

@Component({
  selector: 'app-document-details-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './document-details-modal.component.html',
  styleUrls: ['./document-details-modal.component.scss']
})
export class DocumentDetailsModalComponent implements OnChanges {
  private dataOverviewService = inject(DataOverviewService);
  private fb = inject(FormBuilder);

  @Input() document: DocumentDetailsDTO | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<DocumentDetailsDTO>();

  isSaving = signal(false);
  saveError = signal<string | null>(null);
  editMode = signal(false);

  // Form for editing
  editForm: FormGroup = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    description: ['', Validators.maxLength(1000)],
    status: [DocumentStatus.PENDING_VALIDATION, Validators.required],
    classification: [DocumentClassification.BILL, Validators.required]
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['document'] && this.document) {
      this.patchForm();
      this.editMode.set(false);
      this.saveError.set(null);
    }
  }

  private patchForm(): void {
    if (this.document) {
      this.editForm.patchValue({
        title: this.document.title,
        description: this.document.description || '',
        status: this.document.status,
        classification: this.document.classification
      });
    }
  }

  toggleEditMode(): void {
    if (this.editMode()) {
      this.patchForm();
    }
    this.editMode.update(v => !v);
    this.saveError.set(null);
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

  formatDate(date: Date | string): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  onSubmit(): void {
    if (this.editForm.invalid || !this.document || this.isSaving()) {
      this.editForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.saveError.set(null);

    const formValue = this.editForm.value as DocumentFormValue;
    
    const request: EditMenuDetailsRequest = {
      documentId: this.document.id,
      title: formValue.title,
      description: formValue.description,
      status: formValue.status,
      classification: formValue.classification
    };

    this.dataOverviewService.updateMenuDetails(request).subscribe({
      next: () => {
        const updatedDoc: DocumentDetailsDTO = {
          ...this.document!,
          title: formValue.title,
          description: formValue.description,
          status: formValue.status,
          classification: formValue.classification
        };
        this.isSaving.set(false);
        this.editMode.set(false);
        this.save.emit(updatedDoc);
      },
      error: (error: Error) => {
        this.isSaving.set(false);
        this.saveError.set(error.message || 'Failed to save changes. Please try again.');
      }
    });
  }

  onClose(): void {
    this.close.emit();
  }
}