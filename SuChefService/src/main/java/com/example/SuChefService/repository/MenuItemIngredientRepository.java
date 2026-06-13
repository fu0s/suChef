package com.example.SuChefService.repository;

import com.example.SuChefService.entity.MenuItemIngredient;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, String> {

    List<MenuItemIngredient> findByMenuItemId(String menuItemId);

    Optional<MenuItemIngredient> findByMenuItemIdAndIngredientName(String menuItemId, String ingredientName);

    void deleteByMenuItemId(String menuItemId);
}