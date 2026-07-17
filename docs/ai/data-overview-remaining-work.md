# Data Overview Feature - Remaining Work

## Branch: `feat/data-overview-page`
## PR: https://github.com/fu0s/suChef/pull/new/feat/data-overview-page

---

## Completed ✅

### Backend (Spring Boot)
- [x] DocumentStatus enum - PENDING_VALIDATION added
- [x] MenuItemIngredient entity with JPA annotations
- [x] MenuItem entity updated with @OneToMany ingredients
- [x] Liquibase migration (db.changelog-1.5.yaml)
- [x] 20+ DTOs for validation, editing, overview
- [x] DataOverviewService with analytics methods
- [x] DataOverviewController with 10 REST endpoints
- [x] DocumentAnalysisService updated for PENDING_VALIDATION
- [x] MenuItemIngredientRepository
- [x] Backend compiles successfully

### Frontend (Angular 20+)
- [x] Feature module structure created
- [x] DataOverviewComponent (main dashboard)
- [x] DataOverview.routes.ts
- [x] DataOverviewService with RxJS integration
- [x] 9 TypeScript model interfaces
- [x] Feature directory structure complete

---

## Remaining Work 📋

### 1. Frontend UI Components (Priority: HIGH)

#### DataOverviewComponent Template & Styles
- [ ] `data-overview.component.html` - Main dashboard layout with 4 sections
- [ ] `data-overview.component.scss` - Tailwind-based styling

#### Child Components (to be created in `/components/`)
- [ ] `DocumentsListComponent` - Document cards with status badges
- [ ] `MenusListComponent` - Menu items with restaurant grouping
- [ ] `IngredientsListComponent` - Ingredient quantities display
- [ ] `RestaurantMenusComponent` - Restaurant-based menu views

#### Document Details Modal
- [ ] Edit modal for document validation
- [ ] Classification-specific editors (BILL, ORDER, MENU)
- [ ] Form validation with Angular Reactive Forms

### 2. Shared Models (Priority: HIGH)
- [ ] `shared/models/document-status.model.ts` - DocumentStatus enum
- [ ] `shared/models/document-classification.model.ts` - DocumentClassification enum
- [ ] Add to shared barrel export

### 3. Routing Integration (Priority: HIGH)
- [ ] Add DATA_OVERVIEW_ROUTES to `app.routes.ts`
- [ ] Add navigation link in main layout/sidebar
- [ ] Lazy-load feature module

### 4. Validation Banner Feature (Priority: HIGH)
- [ ] Persistent banner showing pending validation count
- [ ] Click to filter documents by PENDING_VALIDATION
- [ ] Real-time count updates via Angular Signals

### 5. Real-time Updates (Priority: MEDIUM)
- [ ] Convert services to use Angular Signals
- [ ] Add WebSocket/SSE for live updates
- [ ] Optimistic UI updates for edits

### 6. Testing (Priority: MEDIUM)
- [ ] Unit tests for DataOverviewService
- [ ] Component tests for all 4 list components
- [ ] Integration tests for API endpoints
- [ ] E2E test for validation workflow

### 7. Backend Enhancements (Priority: MEDIUM)
- [ ] Add validation in controller for DTO constraints
- [ ] Implement bulk validation endpoint
- [ ] Add pagination for large datasets
- [ ] Add search/filter query parameters
- [ ] Add audit logging for validation actions

### 8. Documentation (Priority: LOW)
- [ ] API documentation (OpenAPI/Swagger)
- [ ] Component usage documentation
- [ ] Developer onboarding guide

---

## File Structure to Complete

```
frontend/src/app/features/data-overview/
├── components/
│   ├── documents-list/
│   │   ├── documents-list.component.ts
│   │   ├── documents-list.component.html
│   │   └── documents-list.component.scss
│   ├── menus-list/
│   │   ├── menus-list.component.ts
│   │   ├── menus-list.component.html
│   │   └── menus-list.component.scss
│   ├── ingredients-list/
│   │   ├── ingredients-list.component.ts
│   │   ├── ingredients-list.component.html
│   │   └── ingredients-list.component.scss
│   ├── restaurant-menus/
│   │   ├── restaurant-menus.component.ts
│   │   ├── restaurant-menus.component.html
│   │   └── restaurant-menus.component.scss
│   └── document-details-modal/
│       ├── document-details-modal.component.ts
│       ├── document-details-modal.component.html
│       └── document-details-modal.component.scss
├── data-overview.component.html
├── data-overview.component.scss
└── shared/models/ (to be moved or linked)
    ├── document-status.model.ts
    └── document-classification.model.ts
```

---

## Next Steps Priority Order

1. **Create HTML/SCSS for DataOverviewComponent** - Core dashboard layout
2. **Create DocumentStatus & DocumentClassification enums** - Shared types
3. **Build DocumentsListComponent** - Primary validation interface
4. **Build MenusListComponent** - Menu management
5. **Build IngredientsListComponent** - Recipe/ingredient tracking
6. **Build RestaurantMenusComponent** - Restaurant grouping
7. **Add routing to app.routes.ts** - Navigation integration
8. **Implement validation banner** - Persistent UX element
9. **Add Angular Signals** - Reactive state management
10. **Write tests** - Quality assurance

---

## Technical Notes

- Backend uses Spring Boot 3.x with Java 21
- Frontend uses Angular 20+ with standalone components
- Communication via REST API (consider WebSocket for real-time)
- Database: PostgreSQL with Liquibase migrations
- Styling: Tailwind CSS with glassmorphism design system
- State: RxJS Observables (migrate to Signals)

---

## Acceptance Criteria

- [ ] User sees pending validation banner on all pages
- [ ] Clicking banner navigates to filtered document list
- [ ] Documents show PENDING_VALIDATION status prominently
- [ ] User can edit document details inline
- [ ] Menu items display with ingredient breakdown
- [ ] Real-time count updates without refresh
- [ ] All 4 data sections (Orders, Stock, Menu, Recipes) visible
- [ ] Responsive design works on mobile/desktop