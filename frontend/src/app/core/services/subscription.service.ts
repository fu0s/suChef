import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SubscriptionPlan {
    id: string;
    name: string;
    price: number;
    maxDocumentsSizeMb: number;
    maxChatsPerMonth: number;
    maxAccountsPerRestaurant: number;
    maxNotificationsPerMonth: number;
}

export interface RestaurantSubscription {
    id: string;
    plan: SubscriptionPlan;
    startDate: string;
    endDate: string;
}

export interface SubscriptionUsage {
    id: string;
    monthYear: string;
    currentDocumentsSizeBytes: number;
    chatsCount: number;
    notificationsCount: number;
}

@Injectable({
    providedIn: 'root'
})
export class SubscriptionService {
    private apiUrl = `${environment.apiUrl}/api/subscription`;

    constructor(private http: HttpClient) { }

    getUsage(): Observable<SubscriptionUsage> {
        return this.http.get<SubscriptionUsage>(`${this.apiUrl}/usage`);
    }

    getCurrentSubscription(): Observable<RestaurantSubscription> {
        return this.http.get<RestaurantSubscription>(`${this.apiUrl}/current`);
    }

    getAllPlans(): Observable<SubscriptionPlan[]> {
        return this.http.get<SubscriptionPlan[]>(`${this.apiUrl}/plans`);
    }
}
