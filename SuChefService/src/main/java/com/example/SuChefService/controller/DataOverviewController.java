package com.example.SuChefService.controller;

import com.example.SuChefService.dto.*;
import com.example.SuChefService.entity.DocumentStatus;
import com.example.SuChefService.service.DataOverviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/data-overview")
public class DataOverviewController {

    private final DataOverviewService dataOverviewService;

    public DataOverviewController(DataOverviewService dataOverviewService) {
        this.dataOverviewService = dataOverviewService;
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentOverviewDTO>> getAllDocumentsOverview() {
        List<DocumentOverviewDTO> documents = dataOverviewService.getDocumentsOverview();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/documents/{status}")
    public ResponseEntity<List<DocumentOverviewDTO>> getDocumentsByStatus(@PathVariable DocumentStatus status) {
        List<DocumentOverviewDTO> documents = dataOverviewService.getDocumentsByStatus(status);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/menus")
    public ResponseEntity<List<MenuOverviewDTO>> getAllMenusOverview() {
        List<MenuOverviewDTO> menus = dataOverviewService.getMenusOverview();
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/menus/{restaurantId}")
    public ResponseEntity<List<MenuOverviewDTO>> getMenusByRestaurant(@PathVariable String restaurantId) {
        List<MenuOverviewDTO> menus = dataOverviewService.getMenusByRestaurant(restaurantId);
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientOverviewDTO>> getAllIngredientsOverview() {
        List<IngredientOverviewDTO> ingredients = dataOverviewService.getIngredientsOverview();
        return ResponseEntity.ok(ingredients);
    }

    @GetMapping("/menus/{menuItemId}/ingredients")
    public ResponseEntity<List<IngredientOverviewDTO>> getIngredientsByMenuItem(@PathVariable String menuItemId) {
        List<IngredientOverviewDTO> ingredients = dataOverviewService.getIngredientsByMenuItem(menuItemId);
        return ResponseEntity.ok(ingredients);
    }

    @GetMapping("/documents/{documentId}/details")
    public ResponseEntity<DocumentDetailsDTO> getDocumentDetails(@PathVariable String documentId) {
        DocumentDetailsDTO details = dataOverviewService.getDocumentDetails(documentId);
        return ResponseEntity.ok(details);
    }

    @PutMapping("/menus/{menuItemId}")
    public ResponseEntity<Void> updateMenuItem(
            @PathVariable String menuItemId,
            @RequestBody EditMenuItemRequest editMenuItemRequest) {
        dataOverviewService.updateMenuItem(menuItemId, editMenuItemRequest);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/menus")
    public ResponseEntity<Void> updateMenuDetails(@RequestBody EditMenuDetailsRequest request) {
        dataOverviewService.updateMenuDetails(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/menus")
    public ResponseEntity<Void> addNewMenu(@RequestBody AddMenuItemRequest request) {
        dataOverviewService.addNewMenuItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}