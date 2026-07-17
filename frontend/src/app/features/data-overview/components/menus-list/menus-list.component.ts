import { Component, Input, Output, EventEmitter, signal, computed, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { MenuOverviewDTO } from '../../models/menu-overview.model';
import { DataOverviewService } from '../../services/data-overview.service';

interface RestaurantGroup {
  restaurantId: string;
  restaurantName: string;
  menus: MenuOverviewDTO[];
}

@Component({
  selector: 'app-menus-list',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './menus-list.component.html',
  styleUrls: ['./menus-list.component.scss']
})
export class MenusListComponent {
  private dataOverviewService = inject(DataOverviewService);

  @Input() menus: MenuOverviewDTO[] = [];
  @Input() isLoading = false;
  @Output() onViewIngredients = new EventEmitter<string>();

  // Signal for expanded restaurants
  expandedRestaurants = signal<Set<string>>(new Set());

  // Computed restaurant groups
  restaurantGroups = computed<RestaurantGroup[]>(() => {
    const groups = new Map<string, RestaurantGroup>();
    
    for (const menu of this.menus) {
      const existing = groups.get(menu.restaurantId);
      if (existing) {
        existing.menus.push(menu);
      } else {
        groups.set(menu.restaurantId, {
          restaurantId: menu.restaurantId,
          restaurantName: menu.restaurantName,
          menus: [menu]
        });
      }
    }
    
    return Array.from(groups.values())
      .sort((a, b) => a.restaurantName.localeCompare(b.restaurantName));
  });

  // Alias for template compatibility
  groupedMenus = this.restaurantGroups;

  isExpanded(restaurantId: string): boolean {
    return this.expandedRestaurants().has(restaurantId);
  }

  toggleRestaurant(restaurantId: string): void {
    const current = this.expandedRestaurants();
    const next = new Set(current);
    if (next.has(restaurantId)) {
      next.delete(restaurantId);
    } else {
      next.add(restaurantId);
    }
    this.expandedRestaurants.set(next);
  }

  getTotalPrice(menus: MenuOverviewDTO[]): number {
    return menus.reduce((sum, m) => sum + m.price, 0);
  }

  onViewIngredientsClick(menuId: string): void {
    this.onViewIngredients.emit(menuId);
  }
}