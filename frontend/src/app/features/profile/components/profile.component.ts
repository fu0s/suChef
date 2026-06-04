import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardComponent } from '../../../shared/components/card/card.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService, User } from '../../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, CardComponent, ButtonComponent, TranslateModule],
  templateUrl: './profile.component.html'
})
export class ProfileComponent {
  currentUser: User | null = null;
  isEditing = false;
  editRestaurantName = '';
  isLoading = false;
  errorMessage: string | null = null;

  constructor(private authService: AuthService) {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  enableEdit() {
    this.editRestaurantName = this.currentUser?.restaurantName || '';
    this.isEditing = true;
    this.errorMessage = null;
  }

  cancelEdit() {
    this.isEditing = false;
    this.errorMessage = null;
  }

  saveProfile() {
    if (!this.editRestaurantName.trim()) {
      this.errorMessage = 'Restaurant name cannot be empty';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.authService.setRestaurant(this.editRestaurantName).subscribe({
      next: () => {
        this.isLoading = false;
        this.isEditing = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Failed to update restaurant name';
      }
    });
  }
}
/*TEMP*/
/*<div class="container mx-auto px-4 py-8">
      <h1 class="text-3xl font-bold mb-6" style="color: #1c1917;">Profile</h1>
      
      <app-card>
        <div class="space-y-4">
          <div class="flex items-center space-x-4">
            <div class="w-24 h-24 rounded-full flex items-center justify-center text-white font-bold" style="background: var(--gradient-1);">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <div>
              <h2 class="text-xl font-semibold" style="color: #1c1917;">John Doe</h2>
              <p style="color: #78716b;">john.doe@example.com</p>
            </div>
          </div>
          
          <div class="pt-4">
            <h3 class="font-medium" style="color: #1c1917;">Bio</h3>
            <p class="mt-2" style="color: #78716b;">
              Passionate home cook exploring new recipes and flavors. Love to experiment with different cuisines and share my cooking adventures.
            </p>
          </div>
          
          <div class="flex justify-end">
            <app-button>Edit Profile</app-button>
          </div>
        </div>
      </app-card>
    </div>*/