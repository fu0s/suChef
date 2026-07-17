import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ValidationBannerComponent } from './validation-banner.component';
import { DataOverviewService } from '../services/data-overview.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { DocumentOverviewDTO } from '../models/document-overview.model';

describe('ValidationBannerComponent', () => {
  let component: ValidationBannerComponent;
  let fixture: ComponentFixture<ValidationBannerComponent>;
  let mockService: jasmine.SpyObj<DataOverviewService>;

  const mockDocuments: DocumentOverviewDTO[] = [
    { id: '1', title: 'Doc 1', description: '', status: 'PENDING_VALIDATION', classification: 'BILL', createdAt: new Date() },
    { id: '2', title: 'Doc 2', description: '', status: 'COMPLETED', classification: 'ORDER', createdAt: new Date() }
  ];

  beforeEach(() => {
    mockService = jasmine.createSpyObj('DataOverviewService', ['getDocumentsByStatus']);
    mockService.getDocumentsByStatus.and.returnValue({
      subscribe: (observer: any) => observer.next(mockDocuments)
    });

    TestBed.configureTestingModule({
      imports: [ValidationBannerComponent],
      providers: [
        { provide: DataOverviewService, useValue: mockService },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    fixture = TestBed.createComponent(ValidationBannerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit filter event on click', () => {
    spyOn(component.onFilter, 'emit');
    component.onBannerClick();
    expect(component.onFilter.emit).toHaveBeenCalledWith('PENDING_VALIDATION');
  });

  it('should return correct aria label for loading state', () => {
    component.isLoading.set(true);
    expect(component.getAriaLabel()).toBe('Loading validation count');
  });

  it('should return correct aria label for zero pending', () => {
    component.isLoading.set(false);
    component.pendingCount.set(0);
    expect(component.getAriaLabel()).toBe('No documents pending validation');
  });

  it('should return correct aria label for single pending', () => {
    component.isLoading.set(false);
    component.pendingCount.set(1);
    expect(component.getAriaLabel()).toBe('1 document pending validation. Click to filter.');
  });

  it('should return correct aria label for multiple pending', () => {
    component.isLoading.set(false);
    component.pendingCount.set(5);
    expect(component.getAriaLabel()).toBe('5 documents pending validation. Click to filter.');
  });
});