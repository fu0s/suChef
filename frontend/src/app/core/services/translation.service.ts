import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  constructor(private translateService: TranslateService) {
    console.log('TranslationService initialized');
  }

  /**
   * Set the current language and save to localStorage
   */
  setLanguage(lang: string): void {
    if (['en', 'de'].includes(lang)) {
      this.translateService.use(lang);
      localStorage.setItem('language', lang);
      console.log('Language set to:', lang);
    }
  }

  /**
   * Get the current language
   */
  getLanguage(): string {
    return this.translateService.currentLang || 'en';
  }

  /**
   * Get the current language as an observable (for reactive updates)
   */
  get currentLanguage$(): Observable<string> {
    return this.translateService.onLangChange.pipe(
      // Map the event to just the lang string
      // Initial value is handled by using startWith in components
    ) as any;
  }

  /**
   * Translate a key using ngx-translate
   * @param key The translation key (e.g., 'common.appName')
   * @param params Optional interpolation parameters
   * @returns The translated string
   */
  translate(key: string, params?: any): string {
    const result = this.translateService.instant(key, params);
    console.log(`Translating: ${key} => ${result}`);
    return result;
  }

  /**
   * Get translations as an observable for reactive updates
   */
  get(key: string, params?: any): Observable<string> {
    return this.translateService.get(key, params);
  }

  /**
   * Get instant translation
   */
  instant(key: string, params?: any): string {
    return this.translateService.instant(key, params);
  }
}

