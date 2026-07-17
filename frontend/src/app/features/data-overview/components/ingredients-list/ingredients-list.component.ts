import { Component, Input, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IngredientOverviewDTO } from '../../models/ingredient-overview.model';

@Component({
  selector: 'app-ingredients-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ingredients-list.component.html',
  styleUrls: ['./ingredients-list.component.scss']
})
export class IngredientsListComponent {
  @Input() ingredients: IngredientOverviewDTO[] = [];
  @Input() isLoading = false;

  // Group ingredients by menu item
  groupedIngredients = computed(() => {
    const groups = new Map<string, { menuItemId: string; menuItemName: string; ingredients: IngredientOverviewDTO[] }>();
    
    for (const ing of this.ingredients) {
      const key = ing.menuItemId;
      if (groups.has(key)) {
        groups.get(key)!.ingredients.push(ing);
      } else {
        groups.set(key, {
          menuItemId: ing.menuItemId,
          menuItemName: ing.menuItemId,
          ingredients: [ing]
        });
      }
    }
    
    return Array.from(groups.values())
      .sort((a, b) => a.menuItemName.localeCompare(b.menuItemName));
  });

  // Unique ingredients across all menu items
  uniqueIngredients = computed(() => {
    const map = new Map<string, { name: string; totalQuantity: number; unit: string; menuItems: Set<string> }>();
    
    for (const ing of this.ingredients) {
      const key = ing.ingredientName.toLowerCase();
      if (map.has(key)) {
        const existing = map.get(key)!;
        existing.totalQuantity += ing.quantity;
        existing.menuItems.add(ing.menuItemId);
      } else {
        map.set(key, {
          name: ing.ingredientName,
          totalQuantity: ing.quantity,
          unit: ing.unit,
          menuItems: new Set([ing.menuItemId])
        });
      }
    }
    
    return Array.from(map.values())
      .sort((a, b) => a.name.localeCompare(b.name));
  });
}