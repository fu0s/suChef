import { MenuItemIngredientDto } from './menu-item-ingredient-dto.model';

export interface AddMenuItemRequest {
  name: string;
  price: number;
  category: string;
  restaurantId: string;
  ingredients?: MenuItemIngredientDto[];
}