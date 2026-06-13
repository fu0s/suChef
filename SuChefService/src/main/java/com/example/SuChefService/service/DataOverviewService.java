package com.example.SuChefService.service;

import com.example.SuChefService.dto.EditableMenuDetails;
import com.example.SuChefService.dto.EditableMenuItem;
import com.example.SuChefService.entity.Document;
import com.example.SuChefService.entity.DocumentStatus;
import com.example.SuChefService.entity.MenuItem;
import com.example.SuChefService.entity.MenuItemIngredient;
import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.repository.DocumentRepository;
import com.example.SuChefService.repository.MenuItemIngredientRepository;
import com.example.SuChefService.repository.MenuItemRepository;
import com.example.SuChefService.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataOverviewService {

    private final DocumentRepository documentRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemIngredientRepository menuItemIngredientRepository;

    public List<DocumentOverviewDTO> getDocumentsOverview() {
        List<DocumentOverviewDTO> results = documentRepository.findAll().stream()
                .filter(doc -> DocumentStatus.PENDING_VALIDATION.equals(doc.getStatus()))
                .map(this::convertToOverviewDTO)
                .collect(Collectors.toList());
        return results;
    }

    public List<MenuOverviewDTO> getMenusOverview() {
        List<MenuOverviewDTO> results = menuItemRepository.findAll().stream()
                .filter(menuItem -> menuItem.getRestaurant() != null)
                .map(this::convertToMenuOverviewDTO)
                .collect(Collectors.toList());
        return results;
    }

    public List<IngredientOverviewDTO> getIngredientsOverview() {
        List<IngredientOverviewDTO> results = menuItemIngredientRepository.findAll().stream()
                .map(this::convertToIngredientOverviewDTO)
                .collect(Collectors.toList());
        return results;
    }

    public DocumentDetailsDTO getDocumentDetails(String documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        return convertToDetailsDTO(document);
    }

    public void updateMenuItem(String menuItemId, EditableMenuItem editableMenuItem) {
        MenuItem existingMenuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + menuItemId));

        existingMenuItem.setName(editableMenuItem.getName());
        existingMenuItem.setPrice(editableMenuItem.getPrice());
        existingMenuItem.setCategory(editableMenuItem.getCategory());

        menuItemRepository.save(existingMenuItem);
    }

    public void updateMenuDetails(EditMenuDetailsRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + request.getRestaurantId()));

        List<MenuItem> menuItems = menuItemRepository.findByRestaurant(restaurant);
        for (MenuItem menuItem : menuItems) {
            menuItem.setName(request.getName());
            menuItem.setPrice(request.getPrice());
            menuItem.setCategory(request.getCategory());
            menuItemRepository.save(menuItem);
        }
    }

    public void addNewMenuItem(AddMenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + request.getRestaurantId()));

        MenuItem menuItem = MenuItem.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .price(request.getPrice())
                .category(request.getCategory())
                .restaurant(restaurant)
                .build();
        menuItemRepository.save(menuItem);

        if (request.getIngredients() != null) {
            for (MenuItemIngredientDto ingredient : request.getIngredients()) {
                MenuItemIngredient menuItemIngredient = MenuItemIngredient.builder()
                        .id(UUID.randomUUID().toString())
                        .menuItemId(menuItem.getId())
                        .ingredientName(ingredient.getIngredientName())
                        .quantity(ingredient.getQuantity())
                        .unit(ingredient.getUnit())
                        .build();
                menuItemIngredientRepository.save(menuItemIngredient);
            }
        }
    }

    public List<DocumentOverviewDTO> getDocumentsByStatus(DocumentStatus status) {
        List<DocumentOverviewDTO> results = documentRepository.findAll().stream()
                .filter(doc -> status.equals(doc.getStatus()))
                .map(this::convertToOverviewDTO)
                .collect(Collectors.toList());
        return results;
    }

    public List<MenuOverviewDTO> getMenusByRestaurant(String restaurantId) {
        List<MenuOverviewDTO> results = menuItemRepository.findAll().stream()
                .filter(menuItem -> menuItem.getRestaurant() != null &&
                           menuItem.getRestaurant().getId().equals(restaurantId))
                .map(this::convertToMenuOverviewDTO)
                .collect(Collectors.toList());
        return results;
    }

    public List<IngredientOverviewDTO> getIngredientsByMenuItem(String menuItemId) {
        List<IngredientOverviewDTO> results = menuItemIngredientRepository.findByMenuItemId(menuItemId).stream()
                .map(this::convertToIngredientOverviewDTO)
                .collect(Collectors.toList());
        return results;
    }

    public DocumentOverviewDTO convertToOverviewDTO(Document document) {
        return DocumentOverviewDTO.builder()
                .id(document.getId())
                .title(document.getTitle() != null ? document.getTitle() : "")
                .description(document.getDescription() != null ? document.getDescription() : "")
                .status(document.getStatus())
                .classification(document.getClassification())
                .createdAt(document.getCreatedAt())
                .build();
    }

    public MenuOverviewDTO convertToMenuOverviewDTO(MenuItem menuItem) {
        Restaurant restaurant = menuItem.getRestaurant();
        String restaurantName = (restaurant != null) ? restaurant.getName() : "Unknown";
        String restaurantId = (restaurant != null) ? restaurant.getId() : "";

        return MenuOverviewDTO.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .price(menuItem.getPrice())
                .category(menuItem.getCategory())
                .restaurantId(restaurantId)
                .restaurantName(restaurantName)
                .build();
    }

    public IngredientOverviewDTO convertToIngredientOverviewDTO(MenuItemIngredient ingredient) {
        MenuItem menuItem = menuItemRepository.findById(ingredient.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + ingredient.getMenuItemId()));

        return IngredientOverviewDTO.builder()
                .id(ingredient.getId())
                .menuItemId(ingredient.getMenuItemId())
                .ingredientName(ingredient.getIngredientName())
                .quantity(ingredient.getQuantity())
                .unit(ingredient.getUnit())
                .build();
    }

    public DocumentDetailsDTO convertToDetailsDTO(Document document) {
        return DocumentDetailsDTO.builder()
                .id(document.getId())
                .title(document.getTitle() != null ? document.getTitle() : "")
                .description(document.getDescription() != null ? document.getDescription() : "")
                .status(document.getStatus())
                .classification(document.getClassification())
                .details(document.getDetails())
                .createdAt(document.getCreatedAt())
                .user(document.getUser() != null ? document.getUser().getId() : "")
                .restaurantName(document.getUser() != null && document.getUser().getRestaurant() != null
                        ? document.getUser().getRestaurant().getName() : "")
                .build();
    }
}