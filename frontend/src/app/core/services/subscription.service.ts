import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { McpService, SubscriptionInfo, SubscriptionPlanInfo } from './mcp.service';

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

    constructor(private mcpService: McpService) { }

    getUsage(): Observable<SubscriptionUsage> {
        return this.mcpService.getSubscriptionInfo().pipe(
            map((info: SubscriptionInfo) => ({
                id: 'mcp-delegated',
                monthYear: new Date().toISOString().substring(0, 7),
                currentDocumentsSizeBytes: (info.currentDocumentsSizeMb || 0) * 1024 * 1024,
                chatsCount: info.currentChatsCount || 0,
                notificationsCount: info.currentNotificationsCount || 0
            }))
        );
    }

    getCurrentSubscription(): Observable<RestaurantSubscription> {
        return this.mcpService.getSubscriptionInfo().pipe(
            map((info: SubscriptionInfo) => ({
                id: 'mcp-delegated',
                plan: {
                    id: 'mcp-plan',
                    name: info.planName,
                    price: info.planPrice,
                    maxDocumentsSizeMb: info.maxDocumentsSizeMb,
                    maxChatsPerMonth: info.maxChatsPerMonth,
                    maxAccountsPerRestaurant: info.maxAccountsPerRestaurant,
                    maxNotificationsPerMonth: 0
                },
                startDate: info.startDate,
                endDate: info.endDate
            }))
        );
    }

    getAllPlans(): Observable<SubscriptionPlan[]> {
        return this.mcpService.getAllPlans().pipe(
            map((plans: SubscriptionPlanInfo[]) => plans.map(p => ({
                id: p.id,
                name: p.name,
                price: p.price,
                maxDocumentsSizeMb: p.maxDocumentsSizeMb,
                maxChatsPerMonth: p.maxChatsPerMonth,
                maxAccountsPerRestaurant: p.maxAccountsPerRestaurant,
                maxNotificationsPerMonth: p.maxNotificationsPerMonth
            })))
        );
    }
}
