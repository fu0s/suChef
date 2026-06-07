import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { AuthService, User } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthContext {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private isGuestModeSubject = new BehaviorSubject<boolean>(false);
  isGuestMode$ = this.isGuestModeSubject.asObservable();

  private currentRestaurantSubject = new BehaviorSubject<string | null>(null);
  currentRestaurant$ = this.currentRestaurantSubject.asObservable();

  constructor(private authService: AuthService) {
    this.syncFromAuthService();
    this.authService.currentUser$.subscribe(user => {
      this.currentUserSubject.next(user);
      this.isAuthenticatedSubject.next(!!user);
      this.isGuestModeSubject.next(this.authService.isGuestMode());
      this.currentRestaurantSubject.next(user?.restaurantName ?? null);
    });
  }

  private syncFromAuthService(): void {
    const user = this.authService.currentUserValue;
    this.currentUserSubject.next(user);
    this.isAuthenticatedSubject.next(!!user);
    this.isGuestModeSubject.next(this.authService.isGuestMode());
    this.currentRestaurantSubject.next(user?.restaurantName ?? null);
  }

  refresh(): void {
    this.syncFromAuthService();
  }

  isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  isGuestMode(): boolean {
    return this.isGuestModeSubject.value;
  }
}