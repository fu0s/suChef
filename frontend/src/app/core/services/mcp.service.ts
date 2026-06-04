import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// ===================== Document Interfaces =====================

export interface DocumentInfo {
  id: string;
  name: string;
  type: string;
  size: number;
  date: string;
  uploadedAt: string;
  status: string;
  classification: string;
}

export interface DocumentCountResponse {
  count: number;
}

export interface DocumentCreateResponse {
  id: string;
  name: string;
  status: string;
}

export interface DocumentUpdateStatusResponse {
  id: string;
  name: string;
  status: string;
}

export interface DocumentDeleteResponse {
  deleted: boolean;
  documentId: string;
}

// ===================== Vendor Interfaces =====================

export interface VendorInfo {
  id: string;
  name: string;
  category: string;
  contactInfo: string;
}

export interface VendorCreateResponse {
  id: string;
  name: string;
  category: string;
}

export interface VendorUpdateResponse {
  id: string;
  name: string;
  category: string;
  contactInfo: string;
}

export interface VendorDeleteResponse {
  deleted: boolean;
  vendorId: string;
}

// ===================== Inventory Interfaces =====================

export interface InventoryItemInfo {
  id: string;
  name: string;
  currentStock: number;
  unit: string;
  unitPrice: number;
  minThreshold: number;
  category: string;
}

export interface InventoryItemCreateResponse {
  id: string;
  name: string;
  currentStock: number;
}

export interface InventoryItemUpdateStockResponse {
  id: string;
  name: string;
  previousStock: number;
  currentStock: number;
}

export interface StockTransactionResponse {
  transactionId: string;
  itemId: string;
  itemName: string;
  quantityChange: number;
  type: string;
  newStock: number;
  timestamp: string;
}

export interface InventoryItemDeleteResponse {
  deleted: boolean;
  itemId: string;
}

// ===================== Order Interfaces =====================

export interface OrderSummaryInfo {
  id: string;
  orderDate: string;
  totalAmount: number;
  status: string;
  itemCount: number;
}

export interface OrderItemInfo {
  id: string;
  menuItemName: string;
  quantity: number;
  price: number;
}

export interface OrderDetailInfo {
  id: string;
  orderDate: string;
  totalAmount: number;
  status: string;
  items: OrderItemInfo[];
}

export interface OrderItemInput {
  menuItemId: string;
  quantity: number;
  price: number;
}

export interface OrderCreateResponse {
  id: string;
  orderDate: string;
  totalAmount: number;
  status: string;
  itemCount: number;
}

export interface OrderStatusUpdateResponse {
  id: string;
  status: string;
  orderDate: string;
}

export interface OrderItemAddResponse {
  orderId: string;
  orderItemId: string;
  newTotalAmount: number;
}

// ===================== Menu Interfaces =====================

export interface MenuItemInfo {
  id: string;
  name: string;
  description: string;
  price: number;
  category: string;
}

// ===================== Metrics Interfaces =====================

export interface RestaurantMetricInfo {
  id: string;
  metricName: string;
  metricValue: number;
  periodStart: string;
  periodEnd: string;
}

// ===================== Subscription Interfaces =====================

export interface SubscriptionInfo {
  planName: string;
  planPrice: number;
  maxDocumentsSizeMb: number;
  maxChatsPerMonth: number;
  maxAccountsPerRestaurant: number;
  currentDocumentsSizeMb: number;
  currentChatsCount: number;
  currentNotificationsCount: number;
  startDate: string;
  endDate: string;
}

export interface SubscriptionPlanInfo {
  id: string;
  name: string;
  price: number;
  maxDocumentsSizeMb: number;
  maxChatsPerMonth: number;
  maxAccountsPerRestaurant: number;
  maxNotificationsPerMonth: number;
}

export interface SubscriptionLimitCheckResponse {
  allowed: boolean;
  actionType: string;
  currentUsage: number;
  limit: number;
  remaining: number;
}

export interface SubscriptionUsageIncrementResponse {
  actionType: string;
  newCount: number;
  monthYear: string;
}

export interface AccountCountResponse {
  count: number;
}

@Injectable({
  providedIn: 'root'
})
export class McpService {
  private apiUrl = `${environment.apiUrl}/api/mcp`;

  constructor(private http: HttpClient) {}

  // ===================== Document Methods =====================

  getUserDocuments(): Observable<DocumentInfo[]> {
    return this.http.get<DocumentInfo[]>(`${this.apiUrl}/documents`);
  }

  getDocumentCount(): Observable<DocumentCountResponse> {
    return this.http.get<DocumentCountResponse>(`${this.apiUrl}/documents/count`);
  }

  getDocumentById(id: string): Observable<DocumentInfo> {
    return this.http.get<DocumentInfo>(`${this.apiUrl}/documents/${id}`);
  }

  getDocumentsByStatus(status: string): Observable<DocumentInfo[]> {
    return this.http.get<DocumentInfo[]>(`${this.apiUrl}/documents/status/${status}`);
  }

  getLastUploadedDocument(): Observable<DocumentInfo> {
    return this.http.get<DocumentInfo>(`${this.apiUrl}/documents/last`);
  }

  searchDocuments(nameQuery: string): Observable<DocumentInfo[]> {
    return this.http.get<DocumentInfo[]>(`${this.apiUrl}/documents/search`, {
      params: { name: nameQuery }
    });
  }

  createDocument(doc: { name: string; type: string; size?: number; status?: string; classification?: string }): Observable<DocumentCreateResponse> {
    return this.http.post<DocumentCreateResponse>(`${this.apiUrl}/documents`, doc);
  }

  updateDocumentStatus(id: string, status: string): Observable<DocumentUpdateStatusResponse> {
    return this.http.put<DocumentUpdateStatusResponse>(`${this.apiUrl}/documents/${id}/status`, { status });
  }

  deleteDocument(id: string): Observable<DocumentDeleteResponse> {
    return this.http.delete<DocumentDeleteResponse>(`${this.apiUrl}/documents/${id}`);
  }

  // ===================== Vendor Methods =====================

  getVendors(): Observable<VendorInfo[]> {
    return this.http.get<VendorInfo[]>(`${this.apiUrl}/vendors`);
  }

  getVendorById(id: string): Observable<VendorInfo> {
    return this.http.get<VendorInfo>(`${this.apiUrl}/vendors/${id}`);
  }

  getVendorsByCategory(category: string): Observable<VendorInfo[]> {
    return this.http.get<VendorInfo[]>(`${this.apiUrl}/vendors/category/${category}`);
  }

  createVendor(vendor: { name: string; category: string; contactInfo?: string }): Observable<VendorCreateResponse> {
    return this.http.post<VendorCreateResponse>(`${this.apiUrl}/vendors`, vendor);
  }

  updateVendor(id: string, vendor: { name?: string; category?: string; contactInfo?: string }): Observable<VendorUpdateResponse> {
    return this.http.put<VendorUpdateResponse>(`${this.apiUrl}/vendors/${id}`, vendor);
  }

  deleteVendor(id: string): Observable<VendorDeleteResponse> {
    return this.http.delete<VendorDeleteResponse>(`${this.apiUrl}/vendors/${id}`);
  }

  // ===================== Inventory Methods =====================

  getInventoryItems(): Observable<InventoryItemInfo[]> {
    return this.http.get<InventoryItemInfo[]>(`${this.apiUrl}/inventory`);
  }

  getLowStockItems(): Observable<InventoryItemInfo[]> {
    return this.http.get<InventoryItemInfo[]>(`${this.apiUrl}/inventory/low-stock`);
  }

  getInventoryItemById(id: string): Observable<InventoryItemInfo> {
    return this.http.get<InventoryItemInfo>(`${this.apiUrl}/inventory/${id}`);
  }

  getInventoryByCategory(category: string): Observable<InventoryItemInfo[]> {
    return this.http.get<InventoryItemInfo[]>(`${this.apiUrl}/inventory/category/${category}`);
  }

  createInventoryItem(item: {
    name: string;
    currentStock?: number;
    unit?: string;
    unitPrice?: number;
    minThreshold?: number;
    category?: string;
  }): Observable<InventoryItemCreateResponse> {
    return this.http.post<InventoryItemCreateResponse>(`${this.apiUrl}/inventory`, item);
  }

  updateStock(id: string, newStock: number): Observable<InventoryItemUpdateStockResponse> {
    return this.http.put<InventoryItemUpdateStockResponse>(`${this.apiUrl}/inventory/${id}/stock`, { newStock });
  }

  createStockTransaction(tx: {
    itemId: string;
    quantityChange: number;
    type: string;
    documentId?: string;
  }): Observable<StockTransactionResponse> {
    return this.http.post<StockTransactionResponse>(`${this.apiUrl}/inventory/transactions`, tx);
  }

  deleteInventoryItem(id: string): Observable<InventoryItemDeleteResponse> {
    return this.http.delete<InventoryItemDeleteResponse>(`${this.apiUrl}/inventory/${id}`);
  }

  // ===================== Order Methods =====================

  getRecentOrders(): Observable<OrderSummaryInfo[]> {
    return this.http.get<OrderSummaryInfo[]>(`${this.apiUrl}/orders/recent`);
  }

  getOrderById(id: string): Observable<OrderDetailInfo> {
    return this.http.get<OrderDetailInfo>(`${this.apiUrl}/orders/${id}`);
  }

  getOrdersByDateRange(startDate: string, endDate: string): Observable<OrderSummaryInfo[]> {
    return this.http.get<OrderSummaryInfo[]>(`${this.apiUrl}/orders/date-range`, {
      params: { startDate, endDate }
    });
  }

  getOrdersByStatus(status: string): Observable<OrderSummaryInfo[]> {
    return this.http.get<OrderSummaryInfo[]>(`${this.apiUrl}/orders/status/${status}`);
  }

  createOrder(order: { totalAmount: number; status?: string; items?: OrderItemInput[] }): Observable<OrderCreateResponse> {
    return this.http.post<OrderCreateResponse>(`${this.apiUrl}/orders`, order);
  }

  updateOrderStatus(id: string, status: string): Observable<OrderStatusUpdateResponse> {
    return this.http.put<OrderStatusUpdateResponse>(`${this.apiUrl}/orders/${id}/status`, { status });
  }

  addOrderItem(orderId: string, item: { menuItemId: string; quantity: number; price: number }): Observable<OrderItemAddResponse> {
    return this.http.post<OrderItemAddResponse>(`${this.apiUrl}/orders/${orderId}/items`, item);
  }

  // ===================== Menu Methods =====================

  getMenuItems(): Observable<MenuItemInfo[]> {
    return this.http.get<MenuItemInfo[]>(`${this.apiUrl}/menu`);
  }

  getMenuItemsByCategory(category: string): Observable<MenuItemInfo[]> {
    return this.http.get<MenuItemInfo[]>(`${this.apiUrl}/menu/category/${category}`);
  }

  // ===================== Metrics Methods =====================

  getRestaurantMetrics(): Observable<RestaurantMetricInfo> {
    return this.http.get<RestaurantMetricInfo>(`${this.apiUrl}/metrics`);
  }

  // ===================== Subscription Methods =====================

  getSubscriptionInfo(): Observable<SubscriptionInfo> {
    return this.http.get<SubscriptionInfo>(`${this.apiUrl}/subscription/info`);
  }

  checkSubscriptionLimit(actionType: string): Observable<SubscriptionLimitCheckResponse> {
    return this.http.post<SubscriptionLimitCheckResponse>(
      `${this.apiUrl}/subscription/check-limit`, null,
      { params: { limitType: actionType } }
    );
  }

  incrementUsage(actionType: string, amount?: number): Observable<SubscriptionUsageIncrementResponse> {
    return this.http.post<SubscriptionUsageIncrementResponse>(
      `${this.apiUrl}/subscription/increment-usage`,
      { actionType, amount }
    );
  }

  getAllPlans(): Observable<SubscriptionPlanInfo[]> {
    return this.http.get<SubscriptionPlanInfo[]>(`${this.apiUrl}/subscription/plans`);
  }

  getAccountCount(): Observable<AccountCountResponse> {
    return this.http.get<AccountCountResponse>(`${this.apiUrl}/subscription/account-count`);
  }
}
