import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DocumentDetailsModalComponent } from './document-details-modal.component';
import { DataOverviewService } from '../services/data-overview.service';
import { DocumentDetailsDTO } from '../models/document-details.model';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { DocumentStatus } from '../../../../shared/models/document-status.model';
import { DocumentClassification } from '../../../../shared/models/document-classification.model';

describe('DocumentDetailsModalComponent', () => {
  let component: DocumentDetailsModalComponent;
  let fixture: ComponentFixture<DocumentDetailsModalComponent>;
  let mockService: jasmine.SpyObj<DataOverviewService>;

  const mockDocument: DocumentDetailsDTO = {
    id: '1',
    title: 'Test Document',
    description: 'Test Description',
    status: DocumentStatus.PENDING_VALIDATION,
    classification: DocumentClassification.BILL,
    details: 'Extracted details',
    createdAt: new Date('2024-01-15'),
    userId: 'user1',
    restaurantName: 'Test Restaurant'
  };

  beforeEach(async () => {
    mockService = jasmine.createSpyObj('DataOverviewService', ['updateMenuDetails']);
    mockService.updateMenuDetails.and.returnValue({
      subscribe: (observer: any) => observer.next()
    });

    await TestBed.configureTestingModule({
      imports: [DocumentDetailsModalComponent, ReactiveFormsModule],
      providers: [
        { provide: DataOverviewService, useValue: mockService },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DocumentDetailsModalComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('document', mockDocument);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should populate form when document input changes', () => {
    expect(component.editForm.value.title).toBe('Test Document');
    expect(component.editForm.value.description).toBe('Test Description');
    expect(component.editForm.value.status).toBe(DocumentStatus.PENDING_VALIDATION);
    expect(component.editForm.value.classification).toBe(DocumentClassification.BILL);
  });

  it('should format date correctly', () => {
    const date = new Date('2024-01-15T10:30:00');
    const formatted = component.formatDate(date);
    expect(formatted).toContain('January');
    expect(formatted).toContain('15');
    expect(formatted).toContain('2024');
  });

  it('should return correct status label', () => {
    expect(component.getStatusLabel(DocumentStatus.PENDING_VALIDATION)).toBe('Pending Validation');
    expect(component.getStatusLabel(DocumentStatus.COMPLETED)).toBe('Completed');
  });

  it('should return correct classification label', () => {
    expect(component.getClassificationLabel(DocumentClassification.BILL)).toBe('Bill / Invoice');
    expect(component.getClassificationLabel(DocumentClassification.ORDER)).toBe('Order');
  });

  it('should close modal on close emit', () => {
    spyOn(component.close, 'emit');
    component.onClose();
    expect(component.close.emit).toHaveBeenCalled();
  });

  it('should emit save with updated document on successful save', () => {
    spyOn(component.save, 'emit');
    component.editForm.patchValue({
      title: 'Updated Title',
      description: 'Updated Description',
      status: DocumentStatus.COMPLETED,
      classification: DocumentClassification.ORDER
    });
    
    component.onSubmit();
    
    expect(component.save.emit).toHaveBeenCalledWith(jasmine.objectContaining({
      id: '1',
      title: 'Updated Title',
      description: 'Updated Description',
      status: DocumentStatus.COMPLETED,
      classification: DocumentClassification.ORDER
    }));
  });
});