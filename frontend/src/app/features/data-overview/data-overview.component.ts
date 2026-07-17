import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DataOverviewService } from './services/data-overview.service';
import { DocumentOverviewDTO } from './models/document-overview.model';
import { DocumentDetailsDTO } from './models/document-details.model';
import { MenuOverviewDTO } from './models/menu-overview.model';
import { IngredientOverviewDTO } from './models/ingredient-overview.model';
import { ValidationBannerComponent } from './components/validation-banner/validation-banner.component';
import { DocumentsListComponent } from './components/documents-list/documents-list.component';
import { MenusListComponent } from './components/menus-list/menus-list.component';
import { IngredientsListComponent } from './components/ingredients-list/ingredients-list.component';
import { RestaurantMenusComponent } from './components/restaurant-menus/restaurant-menus.component';
import { DocumentDetailsModalComponent } from './components/document-details-modal/document-details-modal.component';

interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error';
}

interface StatusOption {
  value: string;
  label: string;
  color: string;
}

@Component({
  selector: 'app-data-overview',
  standalone: true,
  imports: [
    CommonModule, 
    ValidationBannerComponent,
    DocumentsListComponent,
    MenusListComponent,
    IngredientsListComponent,
    RestaurantMenusComponent,
    DocumentDetailsModalComponent
  ],
  templateUrl: './data-overview.component.html',
  styleUrls: ['./data-overview.component.scss']
})
export class DataOverviewComponent implements OnInit {
  private dataOverviewService = inject(DataOverviewService);

  // Use service signals directly for reactive state
  documents = this.dataOverviewService.documents;
  menus = this.dataOverviewService.menus;
  ingredients = this.dataOverviewService.ingredients;
  isLoading = this.dataOverviewService.isLoading;
  pendingValidationCount = this.dataOverviewService.pendingValidationCount;
  
  // Local component state
  selectedStatus = signal<string>('PENDING_VALIDATION');
  toasts = signal<Toast[]>([]);
  selectedDocument = signal<DocumentDetailsDTO | null>(null);
  isLoadingDetails = signal(false);

  // Status options for filter tabs
  statusOptions: StatusOption[] = [
    { value: 'PENDING_VALIDATION', label: 'Pending Validation', color: '#a855f7' },
    { value: 'RECEIVED', label: 'Received', color: '#3b82f6' },
    { value: 'PROCESSING', label: 'Processing', color: '#f59e0b' },
    { value: 'OCR_PROCESSING', label: 'OCR Processing', color: '#f97316' },
    { value: 'COMPLETED', label: 'Completed', color: '#22c55e' },
    { value: 'FAILED', label: 'Failed', color: '#ef4444' }
  ];

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.dataOverviewService.loadAll();
  }

  filterByStatus(status: string): void {
    this.selectedStatus.set(status);
  }

  setStatus(status: string): void {
    this.selectedStatus.set(status);
  }

  openDocumentDetails(doc: DocumentOverviewDTO): void {
    this.isLoadingDetails.set(true);
    this.dataOverviewService.getDocumentDetails(doc.id).subscribe({
      next: (details) => {
        this.selectedDocument.set(details);
        this.isLoadingDetails.set(false);
      },
      error: (error) => {
        console.error('Error loading document details:', error);
        this.isLoadingDetails.set(false);
        this.showToast('Failed to load document details', 'error');
      }
    });
  }

  closeDocumentDetails(): void {
    this.selectedDocument.set(null);
  }

  onDocumentSaved(doc: DocumentDetailsDTO): void {
    // Optimistic update via service
    this.dataOverviewService.optimisticUpdateDocument(doc.id, doc);
    this.showToast('Document updated successfully', 'success');
    this.closeDocumentDetails();
  }

  loadIngredientsForMenu(menuId: string): void {
    this.dataOverviewService.getIngredientsByMenuItem(menuId).subscribe({
      next: (ingredients) => {
        // The service handles the signal update
        console.log('Ingredients loaded for menu:', menuId, ingredients);
      },
      error: (error) => {
        console.error('Error loading ingredients:', error);
        this.showToast('Failed to load ingredients', 'error');
      }
    });
  }

  refreshData(): void {
    this.dataOverviewService.refreshDocuments();
    this.dataOverviewService.refreshMenus();
    this.dataOverviewService.refreshIngredients();
    this.showToast('Data refreshed', 'success');
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    const toast: Toast = { id: Date.now(), message, type };
    this.toasts.update(toasts => [...toasts, toast]);
    setTimeout(() => {
      this.toasts.update(toasts => toasts.filter(t => t.id !== toast.id));
    }, 3000);
  }

  dismissToast(id: number): void {
    this.toasts.update(toasts => toasts.filter(t => t.id !== id));
  }
}