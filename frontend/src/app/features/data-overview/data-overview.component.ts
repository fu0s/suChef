import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { DataOverviewService } from './services/data-overview.service';
import { DocumentOverviewDTO } from '../../shared/models/document-overview.model';
import { MenuOverviewDTO } from '../../shared/models/menu-overview.model';
import { IngredientOverviewDTO } from '../../shared/models/ingredient-overview.model';

@Component({
  selector: 'app-data-overview',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './data-overview.component.html',
  styleUrls: ['./data-overview.component.scss']
})
export class DataOverviewComponent implements OnInit {
  documents: DocumentOverviewDTO[] = [];
  menus: MenuOverviewDTO[] = [];
  ingredients: IngredientOverviewDTO[] = [];
  selectedStatus: string = 'PENDING_VALIDATION';
  isLoading: boolean = false;
  toasts: Toast[] = [];

  constructor(private dataOverviewService: DataOverviewService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.dataOverviewService.getDocumentsOverview().subscribe({
      next: (documents) => {
        this.documents = documents;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading documents:', error);
        this.showToast('Error loading documents', 'error');
        this.isLoading = false;
      }
    });

    this.dataOverviewService.getMenusOverview().subscribe({
      next: (menus) => {
        this.menus = menus;
      },
      error: (error) => {
        console.error('Error loading menus:', error);
        this.showToast('Error loading menus', 'error');
      }
    });

    this.dataOverviewService.getIngredientsOverview().subscribe({
      next: (ingredients) => {
        this.ingredients = ingredients;
      },
      error: (error) => {
        console.error('Error loading ingredients:', error);
        this.showToast('Error loading ingredients', 'error');
      }
    });
  }

  setStatus(status: string): void {
    this.selectedStatus = status;
    this.loadData();
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    const toast: Toast = { id: Date.now(), message, type };
    this.toasts.push(toast);
    setTimeout(() => {
      this.toasts = this.toasts.filter(t => t.id !== toast.id);
    }, 3000);
  }
}

interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error';
}