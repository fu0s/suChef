import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface User {
  id: string;
  email: string;
  name: string;
  restaurantName?: string;
}

export interface AuthResponse {
  token: string;
  name: string;
  email: string;
  restaurantName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();
  private apiUrl = `${environment.apiUrl}/api/auth`;

  constructor(private http: HttpClient) {
    // Check for existing session via user info cookie
    const savedUser = localStorage.getItem('current_user');
    if (savedUser) {
      this.currentUserSubject.next(JSON.parse(savedUser));
    }
  }

  register(userData: { name: string; email: string; password: string; restaurantName: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, userData, { withCredentials: true }).pipe(
      tap(response => {
        const user: User = {
          id: response.email,
          email: response.email,
          name: response.name,
          restaurantName: response.restaurantName
        };
        this.handleAuthSuccess(user);
      })
    );
  }

  login(credentials: { email: string; password: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials, { withCredentials: true }).pipe(
      tap(response => {
        const user: User = {
          id: response.email,
          email: response.email,
          name: response.name,
          restaurantName: response.restaurantName
        };
        this.handleAuthSuccess(user);
      })
    );
  }

  setRestaurant(restaurantName: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/restaurant`, { restaurantName }, { withCredentials: true }).pipe(
      tap(response => {
        const user: User = {
          id: response.email,
          email: response.email,
          name: response.name,
          restaurantName: response.restaurantName
        };
        this.handleAuthSuccess(user);
      })
    );
  }

  private handleAuthSuccess(user: User): void {
    localStorage.setItem('current_user', JSON.stringify(user));
    this.currentUserSubject.next(user);
    localStorage.removeItem('guest_mode');
  }

  loginAsGuest(): Observable<User> {
    return new Observable((subscriber) => {
      setTimeout(() => {
        const guestUser: User = {
          id: 'guest_' + Date.now().toString(),
          email: 'guest@suchef.local',
          name: 'Guest User'
        };

        localStorage.setItem('current_user', JSON.stringify(guestUser));
        localStorage.setItem('guest_mode', 'true');
        this.currentUserSubject.next(guestUser);

        subscriber.next(guestUser);
        subscriber.complete();
      }, 500);
    });
  }

  logout(): void {
    localStorage.removeItem('current_user');
    localStorage.removeItem('guest_mode');
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    return this.currentUserSubject.value !== null;
  }

  isGuestMode(): boolean {
    return localStorage.getItem('guest_mode') === 'true';
  }
}