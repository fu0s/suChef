import { Component, Input, signal, computed, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { MenuOverviewDTO } from '../../models/menu-overview.model';

interface RestaurantGroup {
  restaurantId: string;
  restaurantName: string;
  menus: MenuOverviewDTO[];
  totalItems: number;
  totalValue: number;
}

@Component({
  selector: 'app-restaurant-menus',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './restaurant-menus.component.html',
  styleUrls: ['./restaurant-menus.component.scss']
})
export class RestaurantMenusComponent {
  @Input() menus: MenuOverviewDTO[] = [];
  @Input() isLoading = false;

  // Signal for view mode: 'grid' | 'table' | 'cards'
  viewMode = signal<'grid' | 'table' | 'cards'>('cards');
  sortField = signal<'name' | 'price' | 'category'>('name');
  sortDirection = signal<'asc' | 'desc'>('asc');

  restaurantGroups = computed<RestaurantGroup[]>(() => {
    const groups = new Map<string, RestaurantGroup>();

    for (const menu of this.menus) {
      const existing = groups.get(menu.restaurantId);
      if (existing) {
        existing.menus.push(menu);
        existing.totalItems += 1;
        existing.totalValue += menu.price;
      } else {
        groups.set(menu.restaurantId, {
          restaurantId: menu.restaurantId,
          restaurantName: menu.restaurantName,
          menus: [menu],
          totalItems: 1,
          totalValue: menu.price
        });
      }
    }

    return Array.from(groups.values())
      .sort((a, b) => a.restaurantName.localeCompare(b.restaurantName));
  });

  setViewMode(mode: 'grid' | 'table' | 'cards'): void {
    this.viewMode.set(mode);
  }

  setSort(field: 'name' | 'price' | 'category'): void {
    if (this.sortField() === field) {
      this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortField.set(field);
      this.sortDirection.set('asc');
    }
  }

  trackByMenuId(index: number, menu: MenuOverviewDTO): string {
    return menu.id;
  }

  // Sort menus based on current sort field and direction
  sortedMenus(menus: MenuOverviewDTO[]): MenuOverviewDTO[] {
    const field = this.sortField();
    const direction = this.sortDirection();
    
    return [...menus].sort((a, b) => {
      let aVal: string | number = a[field];
      let bVal: string | number = b[field];
      
      if (typeof aVal === 'string' && typeof bVal === 'string') {
        aVal = aVal.toLowerCase();
        bVal = bVal.toLowerCase();
      }
      
      const result = aVal < bVal ? -1 : aVal > bVal ? 1 : 0;
      return direction === 'asc' ? result : -result;
    });
  }
}