import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DataOverviewService } from './data-overview.service';
import { provideHttpClient } from '@angular/common/http';
import { DocumentOverviewDTO, MenuOverviewDTO, IngredientOverviewDTO } from '../models';
import { environment } from '../../../../environments/environment';

describe('DataOverviewService', () => {
  let service: DataOverviewService;
  let httpMock: HttpTestingController;

  const mockDocuments: DocumentOverviewDTO[] = [
    { id: '1', title: 'Doc 1', description: 'Desc 1', status: 'PENDING_VALIDATION', classification: 'BILL', createdAt: new Date() },
    { id: '2', title: 'Doc 2', description: 'Desc 2', status: 'COMPLETED', classification: 'ORDER', createdAt: new Date() }
  ];

  const mockMenus: MenuOverviewDTO[] = [
    { id: '1', name: 'Menu 1', price: 10, category: 'Main', restaurantId: 'r1', restaurantName: 'Restaurant 1' }
  ];

  const mockIngredients: IngredientOverviewDTO[] = [
    { id: '1', menuItemId: '1', ingredientName: 'Tomato', quantity: 5, unit: 'kg' }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DataOverviewService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(DataOverviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should load documents and update signals', () => {
    service.loadAll();
    
    const req = httpMock.expectOne(`${environment.apiUrl}/api/data-overview/documents`);
    expect(req.request.method).toBe('GET');
    req.flush(mockDocuments);

    expect(service.documents()).toEqual(mockDocuments);
    expect(service.pendingValidationCount()).toBe(1);
  });

  it('should load menus', () => {
    service.refreshMenus();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/data-overview/menus`);
    req.flush(mockMenus);

    expect(service.menus()).toEqual(mockMenus);
  });

  it('should load ingredients', () => {
    service.refreshIngredients();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/data-overview/ingredients`);
    req.flush(mockIngredients);

    expect(service.ingredients()).toEqual(mockIngredients);
  });

  it('should compute documents by status', () => {
    service._documents.set(mockDocuments);
    
    const byStatus = service.documentsByStatus();
    expect(byStatus['PENDING_VALIDATION']).toHaveLength(1);
    expect(byStatus['COMPLETED']).toHaveLength(1);
  });

  it('should compute menus by restaurant', () => {
    service._menus.set(mockMenus);
    
    const byRestaurant = service.menusByRestaurant();
    expect(byRestaurant['r1']).toHaveLength(1);
  });

  it('should optimistic update document', () => {
    service._documents.set(mockDocuments);
    service.optimisticUpdateDocument('1', { status: 'COMPLETED' });
    
    expect(service.documents()[0].status).toBe('COMPLETED');
    expect(service.pendingValidationCount()).toBe(0);
  });

  it('should get document details', () => {
    const mockDetail = { id: '1', title: 'Doc 1', description: 'Desc', status: 'PENDING_VALIDATION', classification: 'BILL', details: 'Details', createdAt: new Date(), userId: 'u1', restaurantName: 'Rest 1' };
    
    service.getDocumentDetails('1').subscribe(detail => {
      expect(detail).toEqual(mockDetail);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/data-overview/documents/1/details`);
    req.flush(mockDetail);
  });
});