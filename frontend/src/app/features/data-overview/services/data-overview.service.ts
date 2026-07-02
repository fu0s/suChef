import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { DocumentOverviewDTO } from '../../shared/models/document-overview.model';
import { MenuOverviewDTO } from '../../shared/models/menu-overview.model';
import { IngredientOverviewDTO } from '../../shared/models/ingredient-overview.model';
import { DocumentDetailsDTO } from '../../shared/models/document-details.model';
import { EditableMenuItem } from '../../shared/models/editable-menu-item.model';
import { AddMenuItemRequest } from '../../shared/models/add-menu-item-request.model';
import { EditMenuDetailsRequest } from '../../shared/models/edit-menu-details-request.model';

@Injectable({
  providedIn: 'root'
})
export class DataOverviewService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

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
}