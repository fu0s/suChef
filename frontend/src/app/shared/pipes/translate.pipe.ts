import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Pipe({
  name: 'translate',
  standalone: true,
  pure: false
})
export class TranslatePipe implements PipeTransform {

  constructor(private translateService: TranslateService) {}

  transform(key: string, params?: any): string {
    return this.translateService.instant(key, params);
  }
}
