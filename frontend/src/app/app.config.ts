import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { TranslateModule, TranslateLoader, TranslateService } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';

import { routes } from './app.routes';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';

export function HttpLoaderFactory(http: HttpClient) {
  // Use a path relative to the index.html file which is at the root of the served application
  return new TranslateHttpLoader(http, './i18n/', '.json');
}

export function initializeTranslations(translate: TranslateService) {
  return () => {
    translate.addLangs(['en', 'de']);
    translate.setDefaultLang('en');
    const savedLanguage = localStorage.getItem('language') || 'en';
    return translate.use(savedLanguage).toPromise().then(
      () => console.log(`Translations loaded for language: ${savedLanguage}`),
      (error) => console.error('Failed to load translations:', error)
    );
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    ...(TranslateModule.forRoot({
      defaultLanguage: 'en',
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }).providers || [])
  ]
};
