import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { McpService, DocumentInfo } from './mcp.service';

export interface Document {
  id: string;
  name: string;
  type: string;
  date: string | Date;
  uploadedAt: string | Date;
  status: 'RECEIVED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  classification?: 'BILL' | 'ORDER' | 'MENU';
  file?: File;
}

export interface RestaurantMetrics {
  totalRevenue: number;
  totalExpenses: number;
  totalOrders: number;
  averageOrderValue: number;
  profitMargin: number;
  documentCount: number;
  
  // New Metrics
  bestOrder: number;
  highestNetMonth: string;
  stockWasteImprovement: string;
  costOptimization: number;
  wasteReduction: number;
  qualityTracking: string;
  
  // Graph Data
  revenueBreakdown: any[];
  costAnalysis: any[];
  profitMetrics: any[];
  performanceTracking: any[];
  topDishAnalytics: any[];
  
  marginOptimization: number;
}

@Injectable({
  providedIn: 'root'
})
export class DocumentsService {
  private apiUrl = `${environment.apiUrl}/api/documents`;
  private documentsSubject = new BehaviorSubject<Document[]>([]);
  documents$ = this.documentsSubject.asObservable();

  private metricsSubject = new BehaviorSubject<RestaurantMetrics>({
    totalRevenue: 0,
    totalExpenses: 0,
    totalOrders: 0,
    averageOrderValue: 0,
    profitMargin: 0,
    documentCount: 0,
    bestOrder: 0,
    highestNetMonth: 'N/A',
    stockWasteImprovement: '0%',
    costOptimization: 0,
    wasteReduction: 0,
    qualityTracking: 'N/A',
    revenueBreakdown: [],
    costAnalysis: [],
    profitMetrics: [],
    performanceTracking: [],
    topDishAnalytics: [],
    marginOptimization: 0
  });
  metrics$ = this.metricsSubject.asObservable();

  private loadingSubject = new BehaviorSubject<boolean>(false);
  loading$ = this.loadingSubject.asObservable();

  private errorSubject = new BehaviorSubject<string | null>(null);
  error$ = this.errorSubject.asObservable();

  private uploadLoadingSubject = new BehaviorSubject<boolean>(false);
  uploadLoading$ = this.uploadLoadingSubject.asObservable();

  private uploadErrorSubject = new BehaviorSubject<string | null>(null);
  uploadError$ = this.uploadErrorSubject.asObservable();

  private metricsLoadingSubject = new BehaviorSubject<boolean>(false);
  metricsLoading$ = this.metricsLoadingSubject.asObservable();

  private metricsErrorSubject = new BehaviorSubject<string | null>(null);
  metricsError$ = this.metricsErrorSubject.asObservable();

  private pollingInterval: any;

  constructor(
    private http: HttpClient,
    private mcpService: McpService
  ) {
    this.loadDocuments();
    this.startPolling();
    this.loadDashboardMetrics();
  }

  loadDashboardMetrics(): void {
    this.metricsLoadingSubject.next(true);
    this.metricsErrorSubject.next(null);
    // JWT is in HttpOnly cookie — HttpClient interceptor sends it with withCredentials: true
    this.http.get<RestaurantMetrics>(`${environment.apiUrl}/api/dashboard/metrics`).subscribe({
      next: (metrics) => {
        this.metricsSubject.next(metrics);
        this.metricsLoadingSubject.next(false);
      },
      error: (err) => {
        console.error('Failed to load dashboard metrics', err);
        this.metricsErrorSubject.next(err.message || 'Failed to load dashboard metrics');
        this.metricsLoadingSubject.next(false);
      }
    });
  }

  startPolling(): void {
    if (this.pollingInterval) return;
    this.pollingInterval = setInterval(() => {
      const docs = this.documentsSubject.value;
      const hasProcessing = docs.some(d => d.status === 'RECEIVED' || d.status === 'PROCESSING');
      if (hasProcessing) {
        this.loadDocuments();
      }
    }, 5000);
  }

  stopPolling(): void {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
  }

  loadDocuments(): void {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);
    this.mcpService.getUserDocuments().subscribe({
      next: (docs: DocumentInfo[]) => {
        const mapped: Document[] = docs.map(d => ({
          id: d.id,
          name: d.name,
          type: d.type,
          date: d.date,
          uploadedAt: d.uploadedAt,
          status: d.status as Document['status'],
          classification: d.classification as Document['classification']
        }));
        this.documentsSubject.next(mapped);
        this.loadingSubject.next(false);
      },
      error: (err) => {
        console.error('Failed to load documents', err);
        this.errorSubject.next(err.message || 'Failed to load documents');
        this.loadingSubject.next(false);
      }
    });
  }

  uploadDocument(file: File): void {
    this.uploadLoadingSubject.next(true);
    this.uploadErrorSubject.next(null);
    const formData = new FormData();
    formData.append('file', file);

    this.http.post<Document>(`${this.apiUrl}/upload`, formData).subscribe({
      next: (newDoc) => {
        const currentDocs = this.documentsSubject.value;
        const updatedDocs = [...currentDocs, newDoc];
        this.documentsSubject.next(updatedDocs);
        this.loadDashboardMetrics();
        this.startPolling();
        this.uploadLoadingSubject.next(false);
      },
      error: (err) => {
        console.error('Failed to upload document', err);
        this.uploadErrorSubject.next(err.message || 'Failed to upload document');
        this.uploadLoadingSubject.next(false);
      }
    });
  }

  deleteDocument(id: string): void {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);
    this.mcpService.deleteDocument(id).subscribe({
      next: () => {
        const currentDocs = this.documentsSubject.value;
        const updatedDocs = currentDocs.filter(doc => doc.id !== id);
        this.documentsSubject.next(updatedDocs);
        this.loadDashboardMetrics();
        this.loadingSubject.next(false);
      },
      error: (err) => {
        console.error('Failed to delete document', err);
        this.errorSubject.next(err.message || 'Failed to delete document');
        this.loadingSubject.next(false);
      }
    });
  }

  downloadDocument(id: string, fileName: string): void {
    this.http.get(`${this.apiUrl}/${id}/download`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Failed to download document', err)
    });
  }

  clearError(): void {
    this.errorSubject.next(null);
  }

  clearUploadError(): void {
    this.uploadErrorSubject.next(null);
  }

  clearMetricsError(): void {
    this.metricsErrorSubject.next(null);
  }

  private updateMetrics(documents: Document[]): void {
    // Legacy method, moved to backend computation dashboard metrics.
  }
}
