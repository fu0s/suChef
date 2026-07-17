import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { DataOverviewService } from './services/data-overview.service';
import { DocumentOverviewDTO } from '../../../shared/models/document-overview.model';
import { MenuOverviewDTO } from '../../../shared/models/menu-overview.model';
import { IngredientOverviewDTO } from '../../../shared/models/ingredient-overview.model';
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

@Component({
  selector: 'app-data-overview',
  standalone: true,
  imports: [
    CommonModule, 
    RouterOutlet,
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
  selectedDocument = signal<DocumentOverviewDTO | null>(null);

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.dataOverviewService.loadAll();
  }

  filterByStatus(status: string): void {
    this.selectedStatus.set(status);
  }

  openDocumentDetails(doc: DocumentOverviewDTO): void {
    this.selectedDocument.set(doc);
  }

  closeDocumentDetails(): void {
    this.selectedDocument.set(null);
  }

  onDocumentSaved(doc: DocumentOverviewDTO): void {
    // Optimistic update via service
    this.dataOverviewService.optimisticUpdateDocument(doc.id, doc);
    this.showToast('Document updated successfully', 'success');
    this.closeDocumentDetails();
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