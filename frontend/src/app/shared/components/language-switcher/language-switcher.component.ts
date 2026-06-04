import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './language-switcher.component.html'
})
export class LanguageSwitcherComponent implements OnInit {
  currentLanguage: string = 'en';
  isDropdownOpen: boolean = false;
  
  languages = [
    { code: 'en', name: 'English', nativeName: 'English', flag: '🇬🇧' },
    { code: 'de', name: 'German', nativeName: 'Deutsch', flag: '🇩🇪' }
  ];

  constructor(
    private translateService: TranslateService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) {}

  ngOnInit(): void {
    this.currentLanguage = this.translateService.currentLang || 'en';
    this.translateService.onLangChange.subscribe(event => {
      console.log('Language changed event received:', event.lang);
      this.ngZone.run(() => {
        this.currentLanguage = event.lang;
        this.cdr.markForCheck();
        console.log('Current language updated to:', this.currentLanguage);
      });
    });
  }

  setLanguage(lang: string): void {
    console.log('Setting language to:', lang);
    this.ngZone.run(() => {
      this.translateService.use(lang).subscribe(
        () => {
          console.log('Language successfully switched to:', lang);
          localStorage.setItem('language', lang);
          this.currentLanguage = lang;
          this.isDropdownOpen = false;
          this.cdr.markForCheck();
        },
        (error) => {
          console.error('Failed to set language:', error);
        }
      );
    });
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  onLanguageHover(event: MouseEvent, langCode: string, isEnter: boolean): void {
    const btn = event.currentTarget as HTMLElement;
    if (isEnter) {
      btn.style.background = this.currentLanguage === langCode ? 'var(--primary-600)' : 'rgba(22, 82, 176, 0.12)';
    } else {
      btn.style.background = this.currentLanguage === langCode ? 'var(--primary-600)' : 'transparent';
    }
  }

  getFlagForLanguage(lang: string): string {
    const language = this.languages.find(l => l.code === lang);
    return language ? language.flag : '🌐';
  }
}
