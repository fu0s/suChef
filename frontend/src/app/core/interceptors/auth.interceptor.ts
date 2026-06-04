import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // JWT is now stored in an HttpOnly cookie (suChef_jwt) set by the backend.
    // The browser automatically includes cookies for same-origin requests.
    // We only need withCredentials for cross-origin requests (e.g., different port in dev).
    const authRequest = request.clone({
      withCredentials: true
    });

    return next.handle(authRequest);
  }
}