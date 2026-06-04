import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DocumentsService, Document } from '../../../core/services/documents.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-documents-upload',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './documents-upload.component.html'
})
export class DocumentsUploadComponent implements OnInit {
  selectedFile: File | null = null;
  documents: Document[] = [];

  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  uploadLoading$: Observable<boolean>;
  uploadError$: Observable<string | null>;

  constructor(
    private documentsService: DocumentsService,
    private translateService: TranslateService
  ) {
    this.loading$ = this.documentsService.loading$;
    this.error$ = this.documentsService.error$;
    this.uploadLoading$ = this.documentsService.uploadLoading$;
    this.uploadError$ = this.documentsService.uploadError$;
  }

  ngOnInit(): void {
    this.documentsService.documents$.subscribe(docs => {
      this.documents = docs;
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  uploadDocument(): void {
    if (this.selectedFile) {
      this.documentsService.uploadDocument(this.selectedFile);
      // Reset form
      this.selectedFile = null;
    }
  }

  deleteDocument(id: string): void {
    const confirmMessage = this.translateService.instant('documents.deleteConfirm');
    if (confirm(confirmMessage)) {
      this.documentsService.deleteDocument(id);
    }
  }

  downloadDocument(id: string, fileName: string): void {
    this.documentsService.downloadDocument(id, fileName);
  }

  dismissError(): void {
    this.documentsService.clearError();
  }

  dismissUploadError(): void {
    this.documentsService.clearUploadError();
  }
}
