import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DocumentsListComponent } from '../documents-list.component';
import { DocumentOverviewDTO } from '../../models/document-overview.model';
import { DocumentStatus } from '../../../../shared/models/document-status.model';
import { DocumentClassification } from '../../../../shared/models/document-classification.model';

describe('DocumentsListComponent', () => {
  let component: DocumentsListComponent;
  let fixture: ComponentFixture<DocumentsListComponent>;

  const mockDocuments: DocumentOverviewDTO[] = [
    {
      id: '1',
      title: 'Invoice #123',
      description: 'Monthly bill',
      status: DocumentStatus.PENDING_VALIDATION,
      classification: DocumentClassification.BILL,
      createdAt: new Date('2024-01-15')
    },
    {
      id: '2',
      title: 'Order #456',
      description: 'Weekly order',
      status: DocumentStatus.COMPLETED,
      classification: DocumentClassification.ORDER,
      createdAt: new Date('2024-01-14')
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DocumentsListComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(DocumentsListComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('documents', mockDocuments);
    fixture.componentRef.setInput('selectedStatus', 'PENDING_VALIDATION');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should filter documents by status', () => {
    const filtered = component.filteredDocuments();
    expect(filtered.length).toBe(1);
    expect(filtered[0].id).toBe('1');
  });

  it('should format date correctly', () => {
    const date = new Date('2024-01-15T10:30:00');
    const formatted = component.formatDate(date);
    expect(formatted).toContain('Jan');
    expect(formatted).toContain('15');
    expect(formatted).toContain('2024');
  });

  it('should get correct status label', () => {
    expect(component.getStatusLabel(DocumentStatus.PENDING_VALIDATION)).toBe('Pending Validation');
    expect(component.getStatusLabel(DocumentStatus.COMPLETED)).toBe('Completed');
  });

  it('should get correct classification label', () => {
    expect(component.getClassificationLabel(DocumentClassification.BILL)).toBe('Bill / Invoice');
    expect(component.getClassificationLabel(DocumentClassification.ORDER)).toBe('Order');
  });

  it('should emit onEditDocument when edit is clicked', () => {
    spyOn(component.onEditDocument, 'emit');
    component.onEdit(mockDocuments[0]);
    expect(component.onEditDocument.emit).toHaveBeenCalledWith(mockDocuments[0]);
  });
});