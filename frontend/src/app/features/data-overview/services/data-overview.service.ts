import { Injectable, signal, computed, inject } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { DocumentOverviewDTO } from '../models/document-overview.model';
import { MenuOverviewDTO } from '../models/menu-overview.model';
import { IngredientOverviewDTO } from '../models/ingredient-overview.model';
import { DocumentDetailsDTO } from '../models/document-details.model';
import { EditableMenuItem } from '../models/editable-menu-item.model';
import { AddMenuItemRequest } from '../models/add-menu-item-request.model';
import { EditMenuDetailsRequest } from '../models/edit-menu-details-request.model';

@Injectable({
  providedIn: 'root'
})
export class DataOverviewService {
  private readonly apiUrl = environment.apiUrl;
  private http = inject(HttpClient);

  // Signals for reactive state
  private _documents = signal<DocumentOverviewDTO[]>([]);
  private _menus = signal<MenuOverviewDTO[]>([]);
  private _ingredients = signal<IngredientOverviewDTO[]>([]);
  private _isLoading = signal(false);
  private _pendingValidationCount = signal(0);
  private _lastUpdated = signal<Date | null>(null);

  // Public readonly signals
  documents = this._documents.asReadonly();
  menus = this._menus.asReadonly();
  ingredients = this._ingredients.asReadonly();
  isLoading = this._isLoading.asReadonly();
  pendingValidationCount = this._pendingValidationCount.asReadonly();
  lastUpdated = this._lastUpdated.asReadonly();

  // Computed signals
  documentsByStatus = computed(() => {
    const docs = this._documents();
    return docs.reduce((acc, doc) => {
      const status = doc.status || 'UNKNOWN';
      if (!acc[status]) acc[status] = [];
      acc[status].push(doc);
      return acc;
    }, {} as Record<string, DocumentOverviewDTO[]>);
  });

  menusByRestaurant = computed(() => {
    const menus = this._menus();
    return menus.reduce((acc, menu) => {
      const key = menu.restaurantId;
      if (!acc[key]) acc[key] = [];
      acc[key].push(menu);
      return acc;
    }, {} as Record<string, MenuOverviewDTO[]>);
  });

  ingredientsByMenuItem = computed(() => {
    const ingredients = this._ingredients();
    return ingredients.reduce((acc, ing) => {
      const key = ing.menuItemId;
      if (!acc[key]) acc[key] = [];
      acc[key].push(ing);
      return acc;
    }, {} as Record<string, IngredientOverviewDTO[]>);
  });

  // Load all data
  loadAll(): void {
    this._isLoading.set(true);
    
    this.getDocumentsOverview().subscribe({
      next: (documents) => {
        this._documents.set(documents || []);
        this._updatePendingCount(documents);
        this._isLoading.set(false);
        this._lastUpdated.set(new Date());
      },
      error: (error) => {
        console.error('Error loading documents:', error);
        this._isLoading.set(false);
      }
    });

    this.getMenusOverview().subscribe({
      next: (menus) => {
        this._menus.set(menus || []);
      },
      error: (error) => {
        console.error('Error loading menus:', error);
      }
    });

    this.getIngredientsOverview().subscribe({
      next: (ingredients) => {
        this._ingredients.set(ingredients || []);
      },
      error: (error) => {
        console.error('Error loading ingredients:', error);
      }
    });
  }

  // Refresh specific data
  refreshDocuments(): void {
    this.getDocumentsOverview().subscribe({
      next: (documents) => {
        this._documents.set(documents || []);
        this._updatePendingCount(documents);
        this._lastUpdated.set(new Date());
      },
      error: (error) => {
        console.error('Error refreshing documents:', error);
      }
    });
  }

  refreshMenus(): void {
    this.getMenusOverview().subscribe({
      next: (menus) => {
        this._menus.set(menus || []);
        this._lastUpdated.set(new Date());
      },
      error: (error) => {
        console.error('Error refreshing menus:', error);
      }
    });
  }

  refreshIngredients(): void {
    this.getIngredientsOverview().subscribe({
      next: (ingredients) => {
        this._ingredients.set(ingredients || []);
        this._lastUpdated.set(new Date());
      },
      error: (error) => {
        console.error('Error refreshing ingredients:', error);
      }
    });
  }

  // Update pending validation count
  private _updatePendingCount(documents: DocumentOverviewDTO[]): void {
    const count = documents.filter(d => d.status === 'PENDING_VALIDATION').length;
    this._pendingValidationCount.set(count);
  }

  // API Methods
  getDocumentsOverview(): Observable<DocumentOverviewDTO[]> {
    return this.http.get<DocumentOverviewDTO[]>(`${this.apiUrl}/api/data-overview/documents`)
      .pipe(map(documents => documents || []));
  }

  getMenusOverview(): Observable<MenuOverviewDTO[]> {
    return this.http.get<MenuOverviewDTO[]>(`${this.apiUrl}/api/data-overview/menus`)
      .pipe(map(menus => menus || []));
  }

  getIngredientsOverview(): Observable<IngredientOverviewDTO[]> {
    return this.http.get<IngredientOverviewDTO[]>(`${this.apiUrl}/api/data-overview/ingredients`)
      .pipe(map(ingredients => ingredients || []));
  }

  getDocumentDetails(documentId: string): Observable<DocumentDetailsDTO> {
    return this.http.get<DocumentDetailsDTO>(`${this.apiUrl}/api/data-overview/documents/${documentId}/details`);
  }

  getMenusByRestaurant(restaurantId: string): Observable<MenuOverviewDTO[]> {
    return this.http.get<MenuOverviewDTO[]>(`${this.apiUrl}/api/data-overview/menus/${restaurantId}`);
  }

  getIngredientsByMenuItem(menuItemId: string): Observable<IngredientOverviewDTO[]> {
    return this.http.get<IngredientOverviewDTO[]>(`${this.apiUrl}/api/data-overview/menus/${menuItemId}/ingredients`);
  }

  updateMenuItem(menuItemId: string, editableMenuItem: EditableMenuItem): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/api/data-overview/menus/${menuItemId}`, editableMenuItem);
  }

  updateMenuDetails(request: EditMenuDetailsRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/api/data-overview/menus`, request);
  }

  addNewMenuItem(request: AddMenuItemRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/api/data-overview/menus`, request);
  }

  getDocumentsByStatus(status: string): Observable<DocumentOverviewDTO[]> {
    return this.http.get<DocumentOverviewDTO[]>(`${this.apiUrl}/api/data-overview/documents/${status}`);
  }

  // Optimistic updates for better UX
  optimisticUpdateDocument(documentId: string, updates: Partial<DocumentOverviewDTO>): void {
    this._documents.update(docs => 
      docs.map(doc => doc.id === documentId ? { ...doc, ...updates } : doc)
    );
    const pendingDocs = this._documents().filter(d => d.status === 'PENDING_VALIDATION');
    this._pendingValidationCount.set(pendingDocs.length);
  }

  optimisticUpdateMenu(menuId: string, updates: Partial<MenuOverviewDTO>): void {
    this._menus.update(menus => 
      menus.map(menu => menu.id === menuId ? { ...menu, ...updates } : menu)
    );
  }
}