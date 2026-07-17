package com.example.SuChefService.service;

import com.example.SuChefService.dto.*;
import com.example.SuChefService.entity.Document;
import com.example.SuChefService.entity.DocumentStatus;
import com.example.SuChefService.entity.MenuItem;
import com.example.SuChefService.entity.MenuItemIngredient;
import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.repository.DocumentRepository;
import com.example.SuChefService.repository.MenuItemIngredientRepository;
import com.example.SuChefService.repository.MenuItemRepository;
import com.example.SuChefService.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
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

    public void updateMenuItem(String menuItemId, EditMenuItemRequest editMenuItemRequest) {
        MenuItem existingMenuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + menuItemId));

        existingMenuItem.setName(editMenuItemRequest.getName());
        existingMenuItem.setPrice(editMenuItemRequest.getPrice() != null ? BigDecimal.valueOf(editMenuItemRequest.getPrice()) : BigDecimal.ZERO);
        existingMenuItem.setCategory(editMenuItemRequest.getCategory());

        menuItemRepository.save(existingMenuItem);
    }

    public void updateMenuDetails(EditMenuDetailsRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + request.getRestaurantId()));

        List<MenuItem> menuItems = menuItemRepository.findByRestaurant(restaurant);
        for (MenuItem menuItem : menuItems) {
            menuItem.setName(request.getName());
menuItem.setPrice(request.getPrice() != null ? BigDecimal.valueOf(request.getPrice()) : BigDecimal.ZERO);
            menuItem.setCategory(request.getCategory());
            menuItemRepository.save(menuItem);
        }
    }

    public void addNewMenuItem(AddMenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + request.getRestaurantId()));

        MenuItem menuItem = new MenuItem();
        menuItem.setId(UUID.randomUUID().toString());
        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice() != null ? BigDecimal.valueOf(request.getPrice()) : BigDecimal.ZERO);
        menuItem.setCategory(request.getCategory());
        menuItem.setRestaurant(restaurant);
        menuItemRepository.save(menuItem);

        if (request.getIngredients() != null) {
            for (MenuItemIngredientDto ingredient : request.getIngredients()) {
                MenuItemIngredient menuItemIngredient = new MenuItemIngredient();
                menuItemIngredient.setId(UUID.randomUUID().toString());
                menuItemIngredient.setMenuItemId(menuItem.getId());
                menuItemIngredient.setIngredientName(ingredient.getIngredientName());
                menuItemIngredient.setQuantity(ingredient.getQuantity());
                menuItemIngredient.setUnit(ingredient.getUnit());
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
        DocumentOverviewDTO dto = new DocumentOverviewDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getName() != null ? document.getName() : "");
        dto.setDescription(document.getType() != null ? document.getType() : "");
        dto.setStatus(document.getStatus());
        dto.setClassification(document.getClassification());
        dto.setCreatedAt(document.getUploadedAt() != null ? document.getUploadedAt() : document.getDate());
        return dto;
    }

    public MenuOverviewDTO convertToMenuOverviewDTO(MenuItem menuItem) {
        Restaurant restaurant = menuItem.getRestaurant();
        String restaurantName = (restaurant != null) ? restaurant.getName() : "Unknown";
        String restaurantId = (restaurant != null) ? restaurant.getId() : "";

        MenuOverviewDTO dto = new MenuOverviewDTO();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setPrice(menuItem.getPrice() != null ? menuItem.getPrice().doubleValue() : 0.0);
        dto.setCategory(menuItem.getCategory());
        dto.setRestaurantId(restaurantId);
        dto.setRestaurantName(restaurantName);
        return dto;
    }

    public IngredientOverviewDTO convertToIngredientOverviewDTO(MenuItemIngredient ingredient) {
        IngredientOverviewDTO dto = new IngredientOverviewDTO();
        dto.setId(ingredient.getId());
        dto.setMenuItemId(ingredient.getMenuItemId());
        dto.setIngredientName(ingredient.getIngredientName());
        dto.setQuantity(ingredient.getQuantity());
        dto.setUnit(ingredient.getUnit());
        return dto;
    }

    public DocumentDetailsDTO convertToDetailsDTO(Document document) {
        DocumentDetailsDTO dto = new DocumentDetailsDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getName() != null ? document.getName() : "");
        dto.setDescription(document.getType() != null ? document.getType() : "");
        dto.setStatus(document.getStatus());
        dto.setClassification(document.getClassification());
        dto.setDetails(document.getType() != null ? document.getType() : "");
        dto.setCreatedAt(document.getUploadedAt() != null ? document.getUploadedAt() : document.getDate());
        dto.setUserId(document.getUser() != null ? document.getUser().getId() : "");
        dto.setRestaurantName(document.getUser() != null && document.getUser().getRestaurant() != null
                ? document.getUser().getRestaurant().getName() : "");
        return dto;
    }
}